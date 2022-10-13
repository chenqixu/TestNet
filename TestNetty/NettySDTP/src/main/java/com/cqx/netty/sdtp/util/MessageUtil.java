package com.cqx.netty.sdtp.util;

import com.cqx.netty.sdtp.bean.SDTP4GHeader;
import com.cqx.netty.sdtp.bean.SDTPBody;
import com.cqx.netty.sdtp.bean.SDTPHeader;
import com.cqx.netty.sdtp.bean.SDTPMessage;
import com.cqx.netty.sdtp.rule.RuleBean;
import com.cqx.netty.sdtp.rule.RuleUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.List;

/**
 * MessageUtil
 *
 * @author chenqixu
 */
public class MessageUtil<T extends SDTPBody> {
    // Header版本
    public static String HEADER_VERSION = SDTP4GHeader.class.getName();

    private String split;
    private RuleUtil ruleUtil;
    private List<RuleBean> ruleBeanList;
    private SDTPMessage<T> sdtpMessage;
    private Class<T> sdtpBodyClass;

    public MessageUtil(Class<T> sdtpBodyClass) {
        this(sdtpBodyClass, "");
    }

    public MessageUtil(Class<T> sdtpBodyClass, String rule_data) {
        this(sdtpBodyClass, "\\|", rule_data);
    }

    public MessageUtil(Class<T> sdtpBodyClass, String split, String rule_data) {
        this.sdtpBodyClass = sdtpBodyClass;
        this.split = split;
        this.ruleUtil = new RuleUtil();
        this.ruleBeanList = ruleUtil.generateRule(rule_data);
        this.sdtpMessage = new SDTPMessage<>(generateBody().getMessageType(), getHeader());
    }

    public static SDTPMessage parser(byte[] bytes) {
        ByteBuf buf = Unpooled.buffer(bytes.length);
        buf.writeBytes(bytes);
        return parser(buf);
    }

    public static SDTPMessage parser(ByteBuf buf) {
        SDTPHeader sdtpHeader = getHeader();
        // 解析包头
        sdtpHeader.parser(buf);
        return new SDTPMessage(sdtpHeader);
    }

    /**
     * 根据配置的类名构造一个类
     *
     * @return
     */
    public static SDTPHeader getHeader() {
        try {
            Class headerCls = Class.forName(HEADER_VERSION);
            return (SDTPHeader) headerCls.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new NullPointerException(String.format("header构造异常！使用的构造类：%s"
                    , HEADER_VERSION));
        }
    }

    public void append(String data) {
        byte[] bytes = ruleUtil.reverse(ruleBeanList, data.split(split, -1));
        append(bytes);
    }

    public void append(byte[] bytes) {
        T t = generateBody();
        t.setData(bytes);
        sdtpMessage.addSdtpBody(t);
    }

    public void clean() {
        sdtpMessage.cleanBody();
    }

    public byte[] getMessage() {
        sdtpMessage.generateHeader();
        return sdtpMessage.getBytes();
    }

    public byte[] getMessage(long sequenceId) {
        sdtpMessage.generateHeader(sequenceId);
        return sdtpMessage.getBytes();
    }

    public ByteBuf getResponse(long sequenceId) {
        byte[] bytes = getMessage(sequenceId);
        ByteBuf response = Unpooled.buffer(bytes.length);
        response.writeBytes(bytes);
        return response;
    }

    public ByteBuf getResponse() {
        byte[] bytes = getMessage(1L);
        ByteBuf response = Unpooled.buffer(bytes.length);
        response.writeBytes(bytes);
        return response;
    }

    private T generateBody() {
        try {
            T t = sdtpBodyClass.newInstance();
            return t;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new NullPointerException("generateBody异常，信息：" + e.getMessage());
        }
    }
}
