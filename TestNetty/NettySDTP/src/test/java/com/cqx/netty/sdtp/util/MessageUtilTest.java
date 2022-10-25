package com.cqx.netty.sdtp.util;

import com.cqx.common.utils.system.ByteUtil;
import com.cqx.netty.sdtp.bean.*;
import com.cqx.netty.sdtp.rule.MultipleRuleBean;
import com.cqx.netty.sdtp.rule.RuleUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MessageUtilTest {
    private static final Logger logger = LoggerFactory.getLogger(MessageUtilTest.class);

    @Test
    public void verNego() {
        MessageUtil<SDTPverNego_Req> verNego_Req = new MessageUtil<>(SDTPverNego_Req.class);
        verNego_Req.append(new byte[]{(byte) 0x01, (byte) 0x00});
        byte[] bytes = verNego_Req.getMessage(1);

        // 解析数据
        ByteBuf buf = Unpooled.buffer(bytes.length);
        buf.writeBytes(bytes);
        SDTPMessage sdtpMessage = MessageUtil.parser(buf);
        // [Message Header]
        SDTPHeader sdtpHeader = sdtpMessage.getSdtpHeader();
        // sdtp数据帧长度
        long msgLength = sdtpHeader.getTotalLength();
        // 消息类型
        EnumMessageType msgType = sdtpHeader.getMessageType();
        // sdtp包头中的交互的流水号
        long sequenceId = sdtpHeader.getSequenceId();
        // sdtp包头中的事件数量
        int totalContents = sdtpHeader.getTotalContents();
        // sdtp包体长度，减去包头固定长度即可
        int bodyLength = Integer.valueOf((msgLength - sdtpHeader.getHeaderBodyLength()) + "");

        // 读1个字节
        int xdrType = buf.readByte();

        logger.debug("☆☆☆ bytes.length: {}, msgLength: {}, msgType: {}, sequenceId: {}, totalContents: {}, bodyLength: {}, xdrType: {}"
                , bytes.length, msgLength, msgType, sequenceId, totalContents, bodyLength, xdrType);
    }

    @Test
    public void xdrData() {
        String nolength_rule = Constant.COMMON_NOLENGTH_RULE + Constant.N14_RULE;
        String rule = Constant.COMMON_RULE + Constant.N14_RULE;
        String data = "|||||47|5179|0000c0003b0827060005e165f127a5ab|10||||100|1666117520415|1666117520431|1666117520440|1666117520443|2|1|404|509|2409:802E:5003:1815:0:0:1101:201|2409:8027:5003:1813:0:0:211:201|11792|80|2||||2|||||1|";

        // 发送XDR数据
        MessageUtil<SDTPnotifyXDRData_Req> notifyXDRData = new MessageUtil<>(SDTPnotifyXDRData_Req.class, nolength_rule);
        notifyXDRData.append(data);
        byte[] bytes = notifyXDRData.getMessage(1);

        // 解析数据
        ByteBuf buf = Unpooled.buffer(bytes.length);
        buf.writeBytes(bytes);
        SDTPMessage sdtpMessage = MessageUtil.parser(buf);
        // [Message Header]
        SDTPHeader sdtpHeader = sdtpMessage.getSdtpHeader();
        // sdtp数据帧长度
        long msgLength = sdtpHeader.getTotalLength();
        // 消息类型
        EnumMessageType msgType = sdtpHeader.getMessageType();
        // sdtp包头中的交互的流水号
        long sequenceId = sdtpHeader.getSequenceId();
        // sdtp包头中的事件数量
        int totalContents = sdtpHeader.getTotalContents();
        // sdtp包体长度，减去包头固定长度即可
        int bodyLength = Integer.valueOf((msgLength - sdtpHeader.getHeaderBodyLength()) + "");

        logger.debug("☆☆☆ bytes.length: {}, msgLength: {}, msgType: {}, sequenceId: {}, totalContents: {}, bodyLength: {}"
                , bytes.length, msgLength, msgType, sequenceId, totalContents, bodyLength);

        RuleUtil ruleUtil = new RuleUtil();
        List<MultipleRuleBean> multipleRuleBeans = ruleUtil.generateMultipleRule(rule);

        // 解析XDR数据
        for (int i = 0; i < totalContents; i++) {
            // 读1个字节，索引前进1位
            int xdrType = buf.readByte();
            // 读2 byte，索引不动，Length	V	unsigned int	2	全F	指示整个XDR所占用字节数
            byte[] xdrLengthBytes = new byte[2];
            buf.getBytes(buf.readerIndex(), xdrLengthBytes);
            int xdrLength = Integer.valueOf(ByteUtil.unsignedBytes(xdrLengthBytes));
            logger.info("xdrType：{}，xdrLength：{}，计数：{}", xdrType, xdrLength, i);
            // 读取xdr的内容，按刚才的字节读取内容byte[]
            byte[] bytesXdrData = new byte[xdrLength];
            buf.readBytes(bytesXdrData);
            // 解析数据
            ByteBuf byteBuf = Unpooled.buffer(bytesXdrData.length);
            byteBuf.writeBytes(bytesXdrData);
            String ret = ruleUtil.parserMultiple(multipleRuleBeans, byteBuf);
            logger.info("内容：{}", ret);
        }
    }
}