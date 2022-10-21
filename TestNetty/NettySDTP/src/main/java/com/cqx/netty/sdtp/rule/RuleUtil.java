package com.cqx.netty.sdtp.rule;

import com.cqx.common.utils.system.ByteUtil;
import com.cqx.common.utils.system.ClassUtil;
import com.cqx.netty.util.Constant;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 规则解析
 *
 * @author chenqixu
 */
public class RuleUtil {
    private static final Logger logger = LoggerFactory.getLogger(RuleUtil.class);
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

    public List<MultipleRuleBean> generateMultipleRule(String rule) {
        List<MultipleRuleBean> ruleTypeBeanList = new ArrayList<>();
        if (rule == null || rule.trim().length() == 0) {
            return ruleTypeBeanList;
        }
        String[] ruleArray = rule.split(",", -1);
        MultipleRuleBean groupBean = null;
        for (String singleRule : ruleArray) {
            RuleBean ruleBean = new RuleBean(singleRule);
            ruleBean.setRule(iRuleMap.get(ruleBean.getParserRuleName()));
            ruleBean.setDefaultValue(iDefaultValueMap.get(ruleBean.getDefaultValueRuleName()));
            logger.debug("{}", ruleBean);
            check(ruleBean);
            if (ruleBean.isGroup()) {
                if (ruleBean.isGroupFirst()) {
                    groupBean = new MultipleRuleBean(ruleBean);
                    ruleTypeBeanList.add(groupBean);
                } else if (groupBean != null) {
                    groupBean.addRule(ruleBean);
                }
            } else {
                MultipleRuleBean ruleTypeBean = new MultipleRuleBean();
                ruleTypeBean.addRule(ruleBean);
                ruleTypeBeanList.add(ruleTypeBean);
            }
        }
        return ruleTypeBeanList;
    }

    public List<RuleBean> generateRule(String rule) {
        List<RuleBean> ruleBeanList = new ArrayList<>();
        if (rule == null || rule.trim().length() == 0) {
            return ruleBeanList;
        }
        String[] ruleArray = rule.split(",", -1);
        for (String singleRule : ruleArray) {
            RuleBean ruleBean = new RuleBean(singleRule);
            ruleBean.setRule(iRuleMap.get(ruleBean.getParserRuleName()));
            ruleBean.setDefaultValue(iDefaultValueMap.get(ruleBean.getDefaultValueRuleName()));
            logger.debug("{}", ruleBean);
            check(ruleBean);
            ruleBeanList.add(ruleBean);
        }
        return ruleBeanList;
    }

    public String parserMultiple(List<MultipleRuleBean> multipleRuleBeanList, ByteBuf byteBuf) {
        return parserMultiple(multipleRuleBeanList, byteBuf, Constant.OUTPUT_SEPARATOR, new HashMap<>());
    }

    public String parserMultiple(List<MultipleRuleBean> multipleRuleBeanList, ByteBuf byteBuf, String SEPARATOR) {
        return parserMultiple(multipleRuleBeanList, byteBuf, SEPARATOR, new HashMap<>());
    }

    public String parserMultiple(List<MultipleRuleBean> multipleRuleBeanList, ByteBuf byteBuf, String SEPARATOR, Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (MultipleRuleBean multipleRuleBean : multipleRuleBeanList) {
            // 解析单个字段
            if (multipleRuleBean.getRuleType().equals(EnumRuleType.SINGLE)) {
                stringBuilder.append(parser(multipleRuleBean.getSingleRuleBeanList(), byteBuf, SEPARATOR, map));
            }
            // 解析分组
            else if (multipleRuleBean.getRuleType().equals(EnumRuleType.GROUP)) {
                // 先读循环次数
                String numberStr = parser(multipleRuleBean.getGroupFirstRule(), byteBuf, "", map);
                stringBuilder.append(numberStr).append(SEPARATOR);
                if (numberStr == null || numberStr.equals("")) {
                    // 为空，不解析
                } else {
                    int number = Integer.valueOf(numberStr);
                    // 依次循环解析
                    for (int i = 0; i < number; i++) {
                        stringBuilder.append(parser(multipleRuleBean.getGroupRuleBeanList(), byteBuf, SEPARATOR, map));
                    }
                }
            }
        }
        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
    }

    /**
     * 根据规则解析数据，仅供旧版本使用
     *
     * @param ruleBeanList
     * @param byteBuf
     * @return
     */
    public String parser(List<RuleBean> ruleBeanList, ByteBuf byteBuf) {
        return parser(ruleBeanList, byteBuf, Constant.OUTPUT_SEPARATOR, new HashMap<>());
    }

    public String parser(List<RuleBean> ruleBeanList, ByteBuf byteBuf, Map<String, String> map) {
        return parser(ruleBeanList, byteBuf, Constant.OUTPUT_SEPARATOR, map);
    }

    public String parser(RuleBean ruleBean, ByteBuf byteBuf, String SEPARATOR, Map<String, String> map) {
        List<RuleBean> ruleBeanList = new ArrayList<>();
        ruleBeanList.add(ruleBean);
        return parser(ruleBeanList, byteBuf, SEPARATOR, map);
    }

    /**
     * 根据规则解析数据
     *
     * @param ruleBeanList 解析规则
     * @param byteBuf      原始数据
     * @param SEPARATOR    列分隔符
     * @param map          用于缓存，以便在字段协助的时候使用
     * @return
     */
    public String parser(List<RuleBean> ruleBeanList, ByteBuf byteBuf, String SEPARATOR, Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (RuleBean ruleBean : ruleBeanList) {
            int readLen = ruleBean.getReadlen();
            boolean isTlvLength = false;
            // 解析Tag中的format
            if (ruleBean.isTLV()) {
                // 先读取前面2个字节，Tag+Format
                byte[] data = new byte[2];
                byteBuf.readBytes(data);
//                logger.info("Field：{}，tag：{}", ruleBean.getFieldName(), ByteUtil.unsignedByte(data[0]));
                // 读取Format，如果值是0，则需要再读取2个字节的长度
                // 第二个字节高4位是Tag预留字段索引块的高4位
                // 目前都是0，可以当成0来解析，也可以取低四位
                // 为了不留坑，直接取低四位
                int format = ByteUtil.byteToFormat(ByteUtil.getLowBit(data[1]));
                if (format > 0) {
                    readLen = format;
                } else {
                    isTlvLength = true;
                }
            }
            // 解析变长
            if (isTlvLength || (readLen == 0 && ruleBean.getReadType().equals(RuleBean.ReadType.LV))) {
                // 读取长度，2字节
                byte[] data = new byte[2];
                byteBuf.readBytes(data);
                // 如果为空，这里空是指全F
                if (Arrays.equals(data, Constant.BYTE2_DEFAULT)) {
                    stringBuilder.append(Constant.EMPTY_STRING)
                            .append(SEPARATOR);
                    map.put(ruleBean.getFieldName(), Constant.EMPTY_STRING);
                    readLen = 0;
                } else {
                    // 如果不为空，获取无符号数值
                    readLen = Integer.valueOf(ByteUtil.unsignedBytes(data));
                }
            }
            // 解析：由另一个字段来确定IP长度
            else if (ruleBean.getReadType().equals(RuleBean.ReadType.LIP)) {
                // 判断是ipv4还是ipv6
                if (map.get(ruleBean.getAssistFieldName()).equals("1")) {
                    readLen = 4;
                } else {
                    readLen = 16;
                }
            }
            // 如果有长度
            if (readLen > 0) {
                try {
                    // 根据长度，把数据读到数组
                    byte[] data = new byte[readLen];
                    byteBuf.readBytes(data);
                    // 根据对应类型进行处理
                    String ret = ruleBean.getRule().read(data, ruleBean.getDefaultValue());
                    stringBuilder.append(ret);
                    map.put(ruleBean.getFieldName(), ret);
//                    logger.info("FieldName: {}, ret: {}, data: {}", ruleBean.getFieldName(), ret, Arrays.toString(data));
                } catch (Exception e) {
                    String errorMessge = String.format("解析异常，长度：%s，规则: %s，异常信息: %s", readLen, ruleBean, e.getMessage());
                    logger.error(errorMessge, e);
                    throw e;
                }
            }
            stringBuilder.append(SEPARATOR);
        }
        return stringBuilder.toString();
    }

    private LinkedBlockingQueue<String> arrayToQueue(String[] values) {
        LinkedBlockingQueue<String> valueQueue = new LinkedBlockingQueue<>();
        try {
            for (String val : values) {
                valueQueue.put(val);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return valueQueue;
    }

    private LinkedBlockingQueue<String> toGroupBodyQueue(int size, LinkedBlockingQueue<String> queue) {
        LinkedBlockingQueue<String> bodyQueue = new LinkedBlockingQueue<>();
        try {
            for (int j = 0; j < size; j++) {
                String val = queue.poll();
                if (val != null) bodyQueue.put(val);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return bodyQueue;
    }

    public byte[] reverseMultiple(List<MultipleRuleBean> multipleRuleBeanList, String[] values) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(32 * 2048);
        LinkedBlockingQueue<String> queue = arrayToQueue(values);
        for (MultipleRuleBean multipleRuleBean : multipleRuleBeanList) {
            // 解析单个字段
            if (multipleRuleBean.getRuleType().equals(EnumRuleType.SINGLE)) {
                byteBuffer.put(reverse(multipleRuleBean.getSingleRuleBean(), new String[]{queue.poll()}));
            }
            // 解析分组
            else if (multipleRuleBean.getRuleType().equals(EnumRuleType.GROUP)) {
                // 分组-首字段
                String numberStr = queue.poll();
//                if (numberStr == null) {
//                    throw new NullPointerException(String.format("[分组-首字段]%s值为空！"
//                            , multipleRuleBean.getGroupFirstRule().getFieldName()));
//                }
                if (numberStr == null || numberStr.equals("")) {
                    byteBuffer.put(reverse(multipleRuleBean.getGroupFirstRule(), new String[]{numberStr}));
                    // 为空，不读
//                    LinkedBlockingQueue<String> bodyQueue = toGroupBodyQueue(
//                            multipleRuleBean.getGroupRuleBeanList().size(), queue);
//                    byteBuffer.put(reverse(multipleRuleBean.getGroupRuleBeanList(), bodyQueue));
                } else {
                    // 按个数读取
                    int number = Integer.valueOf(numberStr);
                    byteBuffer.put(reverse(multipleRuleBean.getGroupFirstRule(), new String[]{numberStr}));
                    // 分组-body，依次循环解析
                    for (int i = 0; i < number; i++) {
                        LinkedBlockingQueue<String> bodyQueue = toGroupBodyQueue(
                                multipleRuleBean.getGroupRuleBeanList().size(), queue);
                        byteBuffer.put(reverse(multipleRuleBean.getGroupRuleBeanList(), bodyQueue));
                    }
                }
            }
        }
        // 翻转输出
        int size = byteBuffer.position();
        byteBuffer.flip();
        byte[] ret = new byte[size];
        byteBuffer.get(ret, 0, size);
        return ret;
    }

    public byte[] reverse(RuleBean ruleBean, String[] values) {
        List<RuleBean> ruleBeanList = new ArrayList<>();
        ruleBeanList.add(ruleBean);
        return reverse(ruleBeanList, values);
    }

    /**
     * 逆向工程，供旧版本使用
     *
     * @param ruleBeanList 解析规则
     * @param values       数据，格式：数组
     * @return
     */
    public byte[] reverse(List<RuleBean> ruleBeanList, String[] values) {
        return reverse(ruleBeanList, arrayToQueue(values));
    }

    /**
     * 逆向工程
     *
     * @param ruleBeanList 解析规则
     * @param valueQueue   数据，格式：队列
     * @return
     */
    public byte[] reverse(List<RuleBean> ruleBeanList, LinkedBlockingQueue<String> valueQueue) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(32 * 2048);
        for (int i = 0; i < ruleBeanList.size(); i++) {
            RuleBean rb = ruleBeanList.get(i);
            String tmp = valueQueue.poll();
            byte[] tmpBytes;
            try {
                // 这里只有byte需要readLen
                // ip、string，hex，tbcd都不需要readLen
                // 如果byte遇到LV就有坑了，因为初始的readLen为0
                tmpBytes = rb.getRule().reverse(tmp, rb.getDefaultValue(), rb.getReadlen());
            } catch (Exception e) {
                String errorMessge = String.format("逆向解析异常，规则: %s，数据: %s，异常信息: %s", rb, tmp, e.getMessage());
                logger.error(errorMessge, e);
                throw e;
            }
            // 封装TLV、TV
            if (rb.isTLV()) {
                byteBuffer.put(ByteUtil.buildTLV(rb.getTag(), tmpBytes));
            } else {// 封装V、LV
                if (rb.getReadType().equals(RuleBean.ReadType.LV)) {// 写入变长的长度
                    // 计算内容长度并按2字节写入
                    byteBuffer.putShort((short) tmpBytes.length);
                }
//                logger.info("FieldName: {}, tmpBytes: {}", rb.getFieldName(), Arrays.toString(tmpBytes));
                if (tmpBytes != null) {
                    byteBuffer.put(tmpBytes);
                }
            }
        }
        // 翻转输出
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
