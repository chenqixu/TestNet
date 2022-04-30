package com.cqx.netty.sdtp.util;

import com.cqx.netty.sdtp.bean.EnumMessageType;
import com.cqx.netty.sdtp.bean.SDTPBody;
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
        this.sdtpMessage = new SDTPMessage<>(generateBody().getMessageType());
    }

    public static SDTPMessage parser(byte[] bytes) {
        ByteBuf buf = Unpooled.buffer(bytes.length);
        buf.writeBytes(bytes);
        return parser(buf);
    }

    public static SDTPMessage parser(ByteBuf buf) {
        // [Message Header]
        // sdtp数据帧长度
        long msgLength = buf.readUnsignedInt();
        // 消息类型
        int msgType = buf.readUnsignedShort();
        // sdtp包头中的交互的流水号
        long sequenceId = buf.readUnsignedInt();
        // sdtp包头中的事件数量
        int totalContents = buf.readUnsignedShort();
        EnumMessageType messageType = EnumMessageType.ValueOf(msgType);
        if (messageType == null) {
            messageType = EnumMessageType.linkRel_Req;
        }
        SDTPMessage sdtpMessage = new SDTPMessage(messageType);
        sdtpMessage.parserHeader(msgLength, messageType, sequenceId, totalContents);
        return sdtpMessage;
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

    public ByteBuf getResponse() {
        byte[] bytes = getMessage();
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
