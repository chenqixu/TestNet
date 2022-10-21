package com.cqx.netty.sdtp.client;

import com.cqx.common.utils.system.ArraysUtil;
import com.cqx.common.utils.system.ByteUtil;
import com.cqx.netty.sdtp.bean.*;
import com.cqx.netty.sdtp.util.MessageUtil;
import com.cqx.netty.sdtp.util.SdtpUtil;
import com.cqx.netty.util.SocketClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SimpleSDTPClient
 *
 * @author chenqixu
 */
public class SimpleSDTPClient {
    private static final Logger logger = LoggerFactory.getLogger(SimpleSDTPClient.class);
    private String ip;
    private int port;

    public static void main(String[] args) throws IOException {
        String ip = "127.0.0.1";
        int port = 9100;
        String rule = SdtpUtil.COMMON_NOLENGTH_RULE + SdtpUtil.N14_RULE;
        String data = "|||||47|5179|0000c0003b0827060005e165f127a5ab|10||||100|1666117520415|1666117520431|1666117520440|1666117520443|2|1|404|509|2409:802E:5003:1815:0:0:1101:201|2409:8027:5003:1813:0:0:211:201|11792|80|2||||2|||||1|";
        SimpleSDTPClient client = new SimpleSDTPClient();
        client.setIp(ip);
        client.setPort(port);
        client.XDR(rule, data);
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private void XDR(String rule, String data) throws IOException {
        AtomicInteger seq = new AtomicInteger(1);
        // jdk8语法糖自动释放
        try (SocketClient socketClient = SocketClient.newbuilder()
                .setIp(ip)
                .setPort(port)
                .build()) {
            ClientReceive clientReceive = new ClientReceive();

            // 版本协商
            logger.info("☆☆☆ 版本协商开始");
            MessageUtil<SDTPverNego_Req> verNego_Req = new MessageUtil<>(SDTPverNego_Req.class);
            verNego_Req.append(new byte[]{(byte) 0x01, (byte) 0x00});
            socketClient.send(verNego_Req.getMessage(seq.getAndIncrement()));
            socketClient.receive(clientReceive);
            logger.info("☆☆☆ 版本协商结束");

            // 链路鉴权
            logger.info("☆☆☆ 链路鉴权开始");
            MessageUtil<SDTPlinkAuth_Req> linkAuth_Req = new MessageUtil<>(SDTPlinkAuth_Req.class);
            linkAuth_Req.append(buildAuth(SdtpUtil.LoginID, SdtpUtil.sharedSecret));
            socketClient.send(linkAuth_Req.getMessage(seq.getAndIncrement()));
            socketClient.receive(clientReceive);
            logger.info("☆☆☆ 链路鉴权结束");

            // XDR数据发送
            logger.info("☆☆☆ XDR数据发送开始");
            MessageUtil<SDTPnotifyXDRData_Req> notifyXDRData = new MessageUtil<>(SDTPnotifyXDRData_Req.class, rule);
            for (int i = 0; i < 40; i++) {
                notifyXDRData.append(data);
            }
            socketClient.send(notifyXDRData.getMessage(seq.getAndIncrement()));
            socketClient.receive(clientReceive);
            logger.info("☆☆☆ XDR数据发送结束");

            // 链路检测
            logger.info("☆☆☆ 链路检测开始");
            MessageUtil<SDTPlinkCheck_Req> linkCheck_Req = new MessageUtil<>(SDTPlinkCheck_Req.class);
            socketClient.send(linkCheck_Req.getMessage(seq.getAndIncrement()));
            socketClient.receive(clientReceive);
            logger.info("☆☆☆ 链路检测结束");

            // 链路数据发送校验
            logger.info("☆☆☆ 链路数据发送校验开始");
            MessageUtil<SDTPlinkDataCheck_Req> linkDataCheck_Req = new MessageUtil<>(SDTPlinkDataCheck_Req.class);
            // Sendflag 检测包顺序标签，1小时内不重复即可。
            // SendDataInfo 距离上次发送间发送的数据包数量。
            linkDataCheck_Req.append(ArraysUtil.arrayCopy(ByteUtil.numberToBytes(1001)
                    , ByteUtil.numberToBytes(1)));
            socketClient.send(linkDataCheck_Req.getMessage(seq.getAndIncrement()));
            socketClient.receive(clientReceive);
            logger.info("☆☆☆ 链路数据发送校验结束");

            // 链路释放
            logger.info("☆☆☆ 链路释放开始");
            MessageUtil<SDTPlinkRel_Req> linkRel = new MessageUtil<>(SDTPlinkRel_Req.class);
            linkRel.append(new byte[]{0x01});
            socketClient.send(linkRel.getMessage(seq.getAndIncrement()));
            socketClient.receive(clientReceive);
            logger.info("☆☆☆ 链路释放结束");
        }
    }

    /**
     * 鉴权认证
     *
     * @param LoginID      登录ID
     * @param sharedSecret 密码
     * @return
     */
    private byte[] buildAuth(String LoginID, String sharedSecret) {
        long Timestamp = System.currentTimeMillis() / 1000;
        long Rand = 1;
        String loginWithPad = StringUtils.rightPad(LoginID, 12, " ");
        // 登录ID
        byte[] LoginID_byte = loginWithPad.getBytes(StandardCharsets.UTF_8);
        // 64个字节的摘要
        byte[] bytesDigests = SdtpUtil.computerDigest(LoginID, sharedSecret, Timestamp, Rand);
        // 数据拼接
        byte[] newArray = ArraysUtil.arrayAdd(LoginID_byte, bytesDigests, bytesDigests.length);
        byte[] newArray1 = ArraysUtil.arrayAdd(newArray, ByteUtil.longTo4ByteArray(Timestamp), 4);
        return ArraysUtil.arrayAdd(newArray1, ByteUtil.intTo2ByteArray((int) Rand), 2);
    }

    class ClientReceive implements SocketClient.ReceiveCall {

        @Override
        public byte[] read(InputStream in) throws IOException {
            // 先读SDTP头部，定长，5G是12个字节，4G是9个字节
            int headerBodyLength = MessageUtil.getHeader().getHeaderBodyLength();
            byte[] sdtpHeaderBytes = new byte[headerBodyLength];
            // 读取SDTP头部
            int headerReadLen = in.read(sdtpHeaderBytes);
            // 解析SDTP头部
            SDTPMessage sdtpMessage = MessageUtil.parser(sdtpHeaderBytes);
            SDTPHeader sdtpHeader = sdtpMessage.getSdtpHeader();
            // 获取应答类型
            EnumMessageType msgType = sdtpHeader.getMessageType();
            // 获取SDTP.Body数据
            logger.info("sdtpHeader.getTotalLength() {}", sdtpHeader.getTotalLength());
            int lastLen = Integer.valueOf(sdtpHeader.getTotalLength() + "") - headerBodyLength;
            byte[] sdtpBodyBytes = new byte[lastLen];
            int bodyReadLen = in.read(sdtpBodyBytes);
            switch (msgType) {
                // 1:版本协商应答
                case verNego_Resp:
                    MessageUtil<SDTPverNego_Resp> verNego_Resp = new MessageUtil<>(SDTPverNego_Resp.class);
                    verNego_Resp.append(sdtpBodyBytes);
                    logger.info("版本协商应答: {}", verNego_Resp.getResp().getResult());
                    break;
                // 2:链路鉴权应答
                case linkAuth_Resp:
                    MessageUtil<SDTPlinkAuth_Resp> linkAuth_Resp = new MessageUtil<>(SDTPlinkAuth_Resp.class);
                    linkAuth_Resp.append(sdtpBodyBytes);
                    logger.info("链路鉴权应答: {}", linkAuth_Resp.getResp().getResult());
                    break;
                // 4:xdr数据发送应答
                case notifyXDRData_Resp:
                    MessageUtil<SDTPnotifyXDRData_Resp> notifyXdrData_Resp = new MessageUtil<>(SDTPnotifyXDRData_Resp.class);
                    notifyXdrData_Resp.append(sdtpBodyBytes);
                    logger.info("xdr数据发送应答: {}", notifyXdrData_Resp.getResp().getResult());
                    break;
                // 5:链路检测应答
                case linkCheck_Resp:
                    logger.info("链路检测应答: {}", bodyReadLen);
                    break;
                // 6:链路数据发送校验应答
                case linkDataCheck_Resp:
                    MessageUtil<SDTPlinkDataCheck_Resp> linkDataCheck_Resp = new MessageUtil<>(SDTPlinkDataCheck_Resp.class);
                    linkDataCheck_Resp.append(sdtpBodyBytes);
                    logger.info("链路数据发送校验应答, Sendflag: {}, Result: {}, SendDataInfo: {}, RecDataInfo: {}"
                            , linkDataCheck_Resp.getResp().getSendflag()
                            , linkDataCheck_Resp.getResp().getResult()
                            , linkDataCheck_Resp.getResp().getSendDataInfo()
                            , linkDataCheck_Resp.getResp().getRecDataInfo()
                    );
                    break;
                // 7:链路释放应答
                case linkRel_Resp:
                    MessageUtil<SDTPlinkRel_Resp> linkRel_Resp = new MessageUtil<>(SDTPlinkRel_Resp.class);
                    linkRel_Resp.append(sdtpBodyBytes);
                    logger.info("链路释放应答: {}", linkRel_Resp.getResp().getResult());
                    break;
                default:
                    break;
            }
            return sdtpBodyBytes;
        }
    }
}
