package com.cqx.netty.sdtp.rule;

import com.cqx.common.utils.system.ClassUtil;
import com.cqx.netty.util.ByteUtil;
import com.cqx.netty.util.Constant;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * 规则解析
 *
 * @author chenqixu
 */
public class RuleUtil {
    private static final Logger logger = LoggerFactory.getLogger(RuleUtil.class);
    private static final String FixedLength = "L";
    private static final String VariableLength = "LV";
    private static final String LengthByOtherTypes = "LIP[";
    private static Map<String, IRule> iRuleMap = new HashMap<>();
    private static Map<String, IDefaultValue> iDefaultValueMap = new HashMap<>();

    static {
        try {
            ClassUtil<AnnoRule, IRule> classUtil = new ClassUtil<>();
            //扫描所有有AnnoRule注解的类
            Set<Class<?>> classSet = classUtil.getClassSet("com.cqx.netty.sdtp.rule.impl", AnnoRule.class);
            for (Class<?> cls : classSet) {
                //构造
                IRule iRule = classUtil.generate(cls);
                //加入map
                iRuleMap.put(iRule.getName(), iRule);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {
            ClassUtil<AnnoRule, IDefaultValue> classUtil = new ClassUtil<>();
            //扫描所有有AnnoRule注解的类
            Set<Class<?>> classSet = classUtil.getClassSet("com.cqx.netty.sdtp.rule.dvimpl", AnnoRule.class);
            for (Class<?> cls : classSet) {
                //构造
                IDefaultValue iDefaultValue = classUtil.generate(cls);
                //加入map
                iDefaultValueMap.put(iDefaultValue.getName(), iDefaultValue);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public List<RuleBean> generateRule(String rule) {
        List<RuleBean> ruleBeanList = new ArrayList<>();
        String[] ruleArray = rule.split(",", -1);
        for (String singleRule : ruleArray) {
            String[] singleRuleArray = singleRule.split("-", -1);
            // 读取字段名称
            String fieldName = singleRuleArray[0];
            // 读取规则(定长、变长)
            String readLengthRule = singleRuleArray[1];
            // 转换规则(无符号、有符号、字符、16进制等)
            String parserRule = singleRuleArray[2];
            // 默认值规则(默认全F、默认0等)
            String defaultValueRule = singleRuleArray[3];

            RuleBean ruleBean = new RuleBean();
            ruleBean.setFieldName(fieldName);
            // 判断是定长还是变长
            if (readLengthRule.equals(VariableLength)) {// 变长
                ruleBean.setReadType(RuleBean.ReadType.LV);
            } else if (readLengthRule.startsWith(LengthByOtherTypes)) {// 由另一个字段来确定
                ruleBean.setReadType(RuleBean.ReadType.LIP);
                // 设置协助字段
                ruleBean.setAssistFieldName(readLengthRule.replace(LengthByOtherTypes, ""));
            } else {// 定长
                int readlen = Integer.valueOf(readLengthRule.replace(FixedLength, ""));
                ruleBean.setReadType(RuleBean.ReadType.LN);
                ruleBean.setReadlen(readlen);
            }
            ruleBean.setRule(iRuleMap.get(parserRule));
            ruleBean.setDefaultValue(iDefaultValueMap.get(defaultValueRule));
            logger.info("{}", ruleBean);
            check(ruleBean);
            ruleBeanList.add(ruleBean);
        }
        return ruleBeanList;
    }

    /**
     * 根据规则解析数据
     *
     * @param ruleBeanList
     * @param byteBuf
     * @return
     */
    public String parser(List<RuleBean> ruleBeanList, ByteBuf byteBuf) {
        StringBuilder stringBuilder = new StringBuilder();
        // 用于缓存，以便在字段协助的时候使用
        Map<String, String> map = new HashMap<>();
        for (RuleBean ruleBean : ruleBeanList) {
            if (ruleBean.getReadType() == RuleBean.ReadType.LV) {// 变长
                // 读取长度，2字节
                byte[] data = new byte[2];
                byteBuf.readBytes(data);
                // 如果为空，这里空是指全F
                if (Arrays.equals(data, Constant.BYTE2_DEFAULT)) {
                    stringBuilder.append(Constant.EMPTY_STRING)
                            .append(Constant.OUTPUT_SEPARATOR);
                    map.put(ruleBean.getFieldName(), Constant.EMPTY_STRING);
                    ruleBean.setReadlen(0);
                } else {
                    // 如果不为空，获取无符号数值
                    ruleBean.setReadlen(Integer.valueOf(ByteUtil.unsignedBytes(data)));
                }
            } else if (ruleBean.getReadType() == RuleBean.ReadType.LIP) {// 由另一个字段来确定
                // 判断是ipv4还是ipv6
                if (map.get(ruleBean.getAssistFieldName()).equals("1")) {
                    ruleBean.setReadlen(4);
                } else {
                    ruleBean.setReadlen(16);
                }
            }
            // 如果有长度
            if (ruleBean.getReadlen() > 0) {
                // 根据长度，把数据读到数组
                byte[] data = new byte[ruleBean.getReadlen()];
                byteBuf.readBytes(data);
                // 根据对应类型进行处理
                String ret = ruleBean.getRule().read(data, ruleBean.getDefaultValue());
                stringBuilder.append(ret).append(Constant.OUTPUT_SEPARATOR);
                map.put(ruleBean.getFieldName(), ret);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 逆向工程
     *
     * @param ruleBeanList
     * @param values
     * @return
     */
    public byte[] reverse(List<RuleBean> ruleBeanList, String[] values) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        for (int i = 0; i < ruleBeanList.size(); i++) {
            RuleBean rb = ruleBeanList.get(i);
            String tmp = values[i];
            byte[] tmpBytes;
            try {
                tmpBytes = rb.getRule().reverse(tmp);
            } catch (Exception e) {
                String errorMessge = String.format("逆向解析异常，规则: %s，数据: %s，异常信息: %s", rb, tmp, e.getMessage());
                logger.error(errorMessge, e);
                throw e;
            }
            if (rb.getReadType() == RuleBean.ReadType.LV) {// 写入变长的长度
                // 计算内容长度并按2字节写入
                byteBuffer.putShort((short) tmpBytes.length);
            }
            if (tmpBytes != null) byteBuffer.put(tmpBytes);
        }
        int size = byteBuffer.position();
        byteBuffer.flip();
        byte[] ret = new byte[size];
        byteBuffer.get(ret, 0, size);
        return ret;
    }

    private void check(RuleBean ruleBean) {
        // 名称
        String fieldName = ruleBean.getFieldName();
        // 校验规则
        if (ruleBean.getRule() == null) throw new NullPointerException(String.format("%s 的规则为空！", fieldName));
        // 校验默认值
        if (ruleBean.getDefaultValue() == null) throw new NullPointerException(String.format("%s 的默认值为空！", fieldName));
    }
}
