package com.cqx.netty.sdtp.service;

import com.cqx.common.utils.system.ArraysUtil;
import com.cqx.common.utils.system.ByteUtil;
import com.cqx.netty.sdtp.bean.*;
import com.cqx.netty.sdtp.rule.MultipleRuleBean;
import com.cqx.netty.sdtp.rule.RuleUtil;
import com.cqx.netty.sdtp.util.MessageUtil;
import com.cqx.netty.sdtp.util.SdtpUtil;
import com.cqx.netty.util.IServer;
import com.cqx.netty.util.IServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单的SDTP服务器
 *
 * @author chenqixu
 */
public class SimpleSDTPServer {
    private static final String PARAM_RULE = "rule";
    private static final String PARAM_SHAREDSECRET = "sharedSecret";
    private static final String PARAM_LOGINID = "loginID";
    private int port;
    private Map<String, String> params = new HashMap<>();

    public static void main(String[] args) throws Exception {
        // 服务端口
        int port = 9100;
        SimpleSDTPServer simple = new SimpleSDTPServer();
        simple.setPort(port);
        simple.addParam(PARAM_RULE, SdtpUtil.COMMON_RULE + SdtpUtil.N14_RULE);
        simple.addParam(PARAM_LOGINID, SdtpUtil.LoginID);
        simple.addParam(PARAM_SHAREDSECRET, SdtpUtil.sharedSecret);
        simple.startServer();
    }

    public void addParam(String key, String value) {
        params.put(key, value);
    }

    public void setPort(int port) {
        this.port = port;
    }

    private void startServer() throws Exception {
        IServer iServer = IServer.newbuilder();
        iServer.setPort(port)
                .setParams(params)
                .buildBootstrap()
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // 可变：
                        // lengthFieldLength
                        // lengthAdjustment
                        p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(
                                Integer.MAX_VALUE, 0,
                                4, -4,
                                0));
                        p.addLast("sdtpServer", new SimpleHandler(params));
                    }
                });
        iServer.start();
    }

    class SimpleHandler extends IServerHandler {
        Map<String, String> params;
        // 客户端信息
        SocketAddress client;
        boolean hasleVerNego;
        boolean hasLinkAuth;
        // 版本协商通知应答
        MessageUtil<SDTPverNego_Resp> verNego_Resp = new MessageUtil<>(SDTPverNego_Resp.class);
        // 链路鉴权应答
        MessageUtil<SDTPlinkAuth_Resp> linkAuth_Resp = new MessageUtil<>(SDTPlinkAuth_Resp.class);
        // 链路检测通知应答
        MessageUtil<SDTPlinkCheck_Resp> linkCheck_Resp = new MessageUtil<>(SDTPlinkCheck_Resp.class);
        // 链路释放通知应答
        MessageUtil<SDTPlinkRel_Resp> linkRel_Resp = new MessageUtil<>(SDTPlinkRel_Resp.class);
        // XDR数据发送应答
        MessageUtil<SDTPnotifyXDRData_Resp> notifyXDRData_Resp = new MessageUtil<>(SDTPnotifyXDRData_Resp.class);
        // 链路数据发送校验
        MessageUtil<SDTPlinkDataCheck_Resp> linkDataCheck_Resp = new MessageUtil<>(SDTPlinkDataCheck_Resp.class);

        // 数据接收计数器
        private int sdtpDataCount = 0;
        // 解析工具
        private RuleUtil xdrRuleUtil;
        // 解析规则
        private List<MultipleRuleBean> multipleRuleBeanList;

        SimpleHandler(Map<String, String> params) {
            this.params = params;
            this.xdrRuleUtil = new RuleUtil();
            this.multipleRuleBeanList = this.xdrRuleUtil.generateMultipleRule(params.get(PARAM_RULE));
            this.verNego_Resp.append(new byte[]{0x01});
            // todo linkAuth_Resp
//            this.linkAuth_Resp.append(new byte[]{0x01});
            this.linkCheck_Resp.append(new byte[]{0x01});
            this.linkRel_Resp.append(new byte[]{0x01});
            this.notifyXDRData_Resp.append(new byte[]{0x01});
            // todo linkDataCheck_Resp
//            this.linkDataCheck_Resp.append(new byte[]{0x01});
        }

        /**
         * 客户端激活的时候
         *
         * @param ctx
         * @throws Exception
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            client = ctx.channel().remoteAddress();
            logger.info("新连接: {}", client);
        }

        @Override
        protected ByteBuf dealHandler(ByteBuf buf) {
            // 解析
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

            logger.debug("☆☆☆ msgLength: {}, msgType: {}, sequenceId: {}, totalContents: {}, bodyLength: {} ,client: {}"
                    , msgLength, msgType, sequenceId, totalContents, bodyLength, client);

            switch (msgType) {
                // 1:版本协商
                case verNego_Req:
                    logger.info("#####协议处理: 1.版本协商  ,client:{}", client);
                    int version = buf.readByte();
                    int subVersion = buf.readByte();

                    if (version == 1 && subVersion == 0) {
                        logger.info("版本协商成功! ,client {}", client);
                        hasleVerNego = true;
                    } else if (version == 1 && subVersion > 1) {
                        verNego_Resp.clean();
                        verNego_Resp.append(new byte[]{0x02});
                        logger.error("版本协商失败! 返回值 2  ,client: {}", client);
                        // 关闭客户端连接
                        closeClient();
                    } else if (version == 1 && subVersion < 1) {
                        verNego_Resp.clean();
                        verNego_Resp.append(new byte[]{0x03});
                        logger.error("版本协商失败! 返回值 3  ,client: {}", client);
                        // 关闭客户端连接
                        closeClient();
                    } else if (version > 1) {
                        verNego_Resp.clean();
                        verNego_Resp.append(new byte[]{0x02});
                        logger.error("版本协商失败! 返回值 2  ,client: {}", client);
                        // 关闭客户端连接
                        closeClient();
                    } else {
                        verNego_Resp.clean();
                        verNego_Resp.append(new byte[]{0x03});
                        logger.error("版本协商失败! 返回值 3  ,client: {}", client);
                        // 关闭客户端连接
                        closeClient();
                    }
                    // 应答
                    return verNego_Resp.getResponse(sequenceId);
                // 2:链路鉴权
                case linkAuth_Req:
                    logger.info("#####协议处理: 2.链路鉴权 ,client: {}", client);
                    byte[] bytesLoginID = new byte[12];
                    buf.readBytes(bytesLoginID);
                    byte[] bytesDigest = new byte[64];
                    buf.readBytes(bytesDigest);
                    byte[] bytesTimestamp = new byte[4];
                    buf.readBytes(bytesTimestamp);
                    byte[] bytesRand = new byte[2];
                    buf.readBytes(bytesRand);

//        Digest=SHA256(LoginID+SHA256(Shared secret)+Timestamp+"rand=”+RAND)
//        其中Shared secret为与LoginID对应的账户密码，由认证双方实体事先商定；
//        LoginID为本消息带的LoginID字段，12字节，不足12字节以空格“ ”补齐；
//        Timestamp为本消息带的Timestamp字段数值，单位为秒（1970年1月1日0时0分0秒起至当前的偏移总秒数）；
//        计算后结果为32位，后32位补零（二进制0）；

                    String sharedSecret = params.get(PARAM_SHAREDSECRET);
                    String loginID = new String(bytesLoginID);
                    Long timestamp = Long.parseLong(ByteUtil.unsignedBytes(bytesTimestamp));
                    Long rand = Long.parseLong(ByteUtil.unsignedBytes(bytesRand));

                    if (loginID.replace(" ", "").equals(params.get(PARAM_LOGINID))) {
                        byte[] bytesDigests = SdtpUtil.computerDigest(loginID, sharedSecret, timestamp, rand);
                        // 打印
                        String s_result = ByteUtil.bytesToHexStringH(bytesDigests, " ");
                        String s_login = ByteUtil.bytesToHexStringH(bytesDigest, " ");
                        logger.info("☆☆☆ Digest bytes: {} ", s_result);
                        if (s_result.equals(s_login)) {
                            // 代表鉴权通过
                            hasLinkAuth = true;
                            logger.info("☆☆☆ 链路鉴权成功! ,client: {}", client);
                            // 应答
                            linkAuth_Resp.clean();
                            byte[] respbytes = ArraysUtil.arrayAdd(new byte[]{0x01}, bytesDigests, bytesDigests.length);
                            linkAuth_Resp.append(respbytes);
                        } else {
                            // 代表SHA256加密结果出错
                            // 关闭客户端连接
                            closeClient();
                            linkAuth_Resp.clean();
                            linkAuth_Resp.append(new byte[]{0x03});
                            logger.error("链路鉴权失败! 返回值 3 SHA256加密结果出错 ,client: {}", client);
                        }
                    } else {
                        //代表LoginID不存在
                        // 关闭客户端连接
                        closeClient();
                        linkAuth_Resp.clean();
                        linkAuth_Resp.append(new byte[]{0x02});
                        logger.error("链路鉴权失败! 返回值 2 LoginID不存在 ,client: {}", client);
                    }
                    // 应答
                    return linkAuth_Resp.getResponse(sequenceId);
                // 3:链路检测
                case linkCheck_Req:
                    logger.info("#####协议处理: 3.链路检测 client: {}", client);
                    if (check()) {
                        return linkCheck_Resp.getResponse(sequenceId);
                    } else {
                        logger.error("#####链路认证或版本协商不通过！ ,client: {}", client);
                    }
                    break;
                // 4:链路释放
                case linkRel_Req:
                    logger.info("#####协议处理: 4.链路释放 ,client: {}", client);
                    // 关闭客户端连接
                    closeClient();
                    return linkRel_Resp.getResponse(sequenceId);
                // 5.Notify XDR数据
                case notifyXDRData_Req:
                    logger.debug("#####协议处理: 5.XDR数据传输 ,client: {}", client);
                    if (check()) {
                        sdtpDataCount++;
                        try {
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
                                String ret = xdrRuleUtil.parserMultiple(multipleRuleBeanList, byteBuf);
                                logger.info("内容：{}", ret);
                            }
                        } catch (Exception e) {
                            notifyXDRData_Resp.clean();
                            notifyXDRData_Resp.append(new byte[]{0x02});
                            logger.error("#####XDR数据传输，异常 ,client: {}", client);
                            logger.error(e.getMessage(), e);
                        }
                        // 应答
                        return notifyXDRData_Resp.getResponse(sequenceId);
                    } else {
                        logger.error("#####链路认证或版本协商不通过！ ,client: {}", client);
                    }
                    break;
                // 6.XDR数据 原始码流
                case XDRRawDataSend_Req:
                    logger.debug("#####协议处理: 6.XDR原始码流数据传输 ,client: {}，不支持！", client);
                    break;
                // 7.连接数据发送校验
                case linkDataCheck_Req:
                    logger.info("#####协议处理: 7.链路数据发送校验 ,client: {}", client);
                    if (check()) {
                        long sendflag = buf.readUnsignedInt();
                        long sendDataInfo = buf.readUnsignedInt();
                        byte[] respbytes = ByteUtil.longTo4ByteArray(sendflag);
//                    linkDataCheck_Resp.append(ByteUtil.intTo2ByteArray(Integer.parseInt(sendflag.toString())));
                        if (sdtpDataCount == sendDataInfo) {
//                        linkDataCheck_Resp.append(new byte[]{0x00});
                            respbytes = ArraysUtil.arrayAdd(respbytes, new byte[]{0x00}, 1);
                        } else if (sdtpDataCount < sendDataInfo) {
//                        linkDataCheck_Resp.append(new byte[]{0x01});
                            respbytes = ArraysUtil.arrayAdd(respbytes, new byte[]{0x01}, 1);
                        } else {
//                        linkDataCheck_Resp.append(new byte[]{0x02});
                            respbytes = ArraysUtil.arrayAdd(respbytes, new byte[]{0x02}, 1);
                        }
//                    linkDataCheck_Resp.append(ByteUtil.intTo2ByteArray(Integer.parseInt(sendDataInfo.toString())));
//                    linkDataCheck_Resp.append(ByteUtil.intTo2ByteArray(stdpDataCount));
                        respbytes = ArraysUtil.arrayAdd(respbytes, ByteUtil.longTo4ByteArray(sendDataInfo), 4);
                        respbytes = ArraysUtil.arrayAdd(respbytes, ByteUtil.longTo4ByteArray(sdtpDataCount), 4);
                        linkDataCheck_Resp.append(respbytes);
                        logger.info("链路数据校验成功! sendflag = {}, sendDataInfo = {}, receive = {}  ,client = {}"
                                , sendflag, sendDataInfo, sdtpDataCount, client);
                        sdtpDataCount = 0;
                        return linkDataCheck_Resp.getResponse(sequenceId);
                    } else {
                        logger.error("#####链路认证或版本协商不通过！ ,client: {}", client);
                    }
                    break;
                default:
                    logger.error("Error Message Type={} ,client={}", msgType, client);
                    // 关闭客户端连接
                    closeClient();
            }
            return null;
        }

        private boolean check() {
            boolean check_flag = false;
            if (hasleVerNego && hasLinkAuth) {
                check_flag = true;
            } else {
                logger.error("版本协商或链路认证失败 ,版本协商结果：{} ,链路认证结果：{} ,client: {}", hasleVerNego, hasLinkAuth, client);
                // 关闭客户端连接
                closeClient();
            }
            return check_flag;
        }
    }
}
