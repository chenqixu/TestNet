package com.cqx.netty.sdtp.client;

import com.cqx.common.utils.system.ArraysUtil;
import com.cqx.common.utils.system.ByteUtil;
import com.cqx.netty.sdtp.bean.*;
import com.cqx.netty.sdtp.util.Constant;
import com.cqx.netty.sdtp.util.MessageUtil;
import com.cqx.netty.sdtp.util.SHA256Util;
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
    private String LoginID;
    private String sharedSecret;
    private StringBuilder result = new StringBuilder();

    public static void main(String[] args) throws IOException {
        String ip = "127.0.0.1";
        int port = 9100;
        String rule = Constant.COMMON_RULE + Constant.N14_RULE;
        String data = "151||||||47|5179|0000c0003b0827060005e165f127a5ab|10||||100|1666117520415|1666117520431|1666117520440|1666117520443|2|1|404|509|2409:802E:5003:1815:0:0:1101:201|2409:8027:5003:1813:0:0:211:201|11792|80|2||||2|||||1|";
        SimpleSDTPClient client = new SimpleSDTPClient();
        client.setIp(ip);
        client.setPort(port);
        client.setLoginID(Constant.LoginID);
        client.setSharedSecret(Constant.sharedSecret);
        if (args.length == 1 && args[0].equals("check")) {
            // 校验
            String ret = client.check(rule, data);
            logger.info("ret: {}", ret);
        } else {
            // 需要把第一个规则和第一个数据去掉(因为数据里的长度不正确)
            int firstRule = rule.indexOf(",");
            rule = rule.substring(firstRule + 1);
            int firstData = data.indexOf("|");
            data = data.substring(firstData + 1);
            // 发送xdr数据
            client.xdr(rule, data);
        }
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setLoginID(String loginID) {
        LoginID = loginID;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public String check(String rule, String data) throws IOException {
        return check(rule, data, false);
    }

    public String check(String rule, String data, boolean lengthCheck) throws IOException {
        try {
            MessageUtil<SDTPnotifyXDRData_Req> notifyXDRData = new MessageUtil<>(SDTPnotifyXDRData_Req.class, rule);
            notifyXDRData.append(data);
            String[] datas = data.split("\\|", -1);
            if (datas.length > 0 && lengthCheck) {
                String dataLength = datas[0];
                String dataRealLength = ByteUtil.unsignedBytes(notifyXDRData.getResp().getLoadLength());
                if (!dataLength.equals(dataRealLength)) {
                    return String.format("校验失败, 源数据长度和实际数据长度不一致, 源数据长度: %s, 实际数据长度: %s", dataLength, dataRealLength);
                }
            }
            return "校验成功";
        } catch (Exception e) {
            return String.format("校验失败, 原因: %s", e.getMessage());
        }
    }

    public String xdr(String rule, String data) throws IOException {
        return xdr("5G", rule, data);
    }

    public String xdr(String header, String rule, String data) throws IOException {
        result.delete(0, result.length());
        AtomicInteger seq = new AtomicInteger(1);
        // 默认走5G包头
        if (header.equals("4G")) {
            MessageUtil.HEADER_VERSION = SDTP4GHeader.class.getName();
        }
        // jdk8语法糖自动释放
        try (SocketClient socketClient = SocketClient.newbuilder()
                .setIp(ip)
                .setPort(port)
                .build()) {
            ClientReceive clientReceive = new ClientReceive();

            // 版本协商
            logger.info("======版本协商开始======");
            result.append("======版本协商开始======").append(Constant.LineENd);
            MessageUtil<SDTPverNego_Req> verNego_Req = new MessageUtil<>(SDTPverNego_Req.class);
            verNego_Req.append(new byte[]{(byte) 0x01, (byte) 0x00});
            socketClient.send(verNego_Req.getMessage(seq.getAndIncrement()));
            socketClient.receive(clientReceive);
            logger.info("======版本协商结束======");
            result.append("======版本协商结束======").append(Constant.LineENd);

            // 链路鉴权
            logger.info("======链路鉴权开始======");
            result.append("======链路鉴权开始======").append(Constant.LineENd);
            MessageUtil<SDTPlinkAuth_Req> linkAuth_Req = new MessageUtil<>(SDTPlinkAuth_Req.class);
            linkAuth_Req.append(buildAuth(this.LoginID, this.sharedSecret));
            socketClient.send(linkAuth_Req.getMessage(seq.getAndIncrement()));
            socketClient.receive(clientReceive);
            logger.info("======链路鉴权结束======");
            result.append("======链路鉴权结束======").append(Constant.LineENd);

            // XDR数据发送
            logger.info("======XDR数据发送开始======");
            result.append("======XDR数据发送开始======").append(Constant.LineENd);
            MessageUtil<SDTPnotifyXDRData_Req> notifyXDRData = new MessageUtil<>(SDTPnotifyXDRData_Req.class, rule);
            for (int i = 0; i < 40; i++) {
                notifyXDRData.append(data);
            }
            socketClient.send(notifyXDRData.getMessage(seq.getAndIncrement()));
            socketClient.receive(clientReceive);
            logger.info("======XDR数据发送结束======");
            result.append("======XDR数据发送结束======").append(Constant.LineENd);

            // 链路检测
            logger.info("======链路检测开始======");
            result.append("======链路检测开始======").append(Constant.LineENd);
            MessageUtil<SDTPlinkCheck_Req> linkCheck_Req = new MessageUtil<>(SDTPlinkCheck_Req.class);
            socketClient.send(linkCheck_Req.getMessage(seq.getAndIncrement()));
            socketClient.receive(clientReceive);
            logger.info("======链路检测结束======");
            result.append("======链路检测结束======").append(Constant.LineENd);

            // 链路数据发送校验
            logger.info("======链路数据发送校验开始======");
            result.append("======链路数据发送校验开始======").append(Constant.LineENd);
            MessageUtil<SDTPlinkDataCheck_Req> linkDataCheck_Req = new MessageUtil<>(SDTPlinkDataCheck_Req.class);
            // Sendflag 检测包顺序标签，1小时内不重复即可。
            // SendDataInfo 距离上次发送间发送的数据包数量。
            linkDataCheck_Req.append(ArraysUtil.arrayCopy(ByteUtil.numberToBytes(1001)
                    , ByteUtil.numberToBytes(1)));
            socketClient.send(linkDataCheck_Req.getMessage(seq.getAndIncrement()));
            socketClient.receive(clientReceive);
            logger.info("======链路数据发送校验结束======");
            result.append("======链路数据发送校验结束======").append(Constant.LineENd);

            // 链路释放
            logger.info("======链路释放开始======");
            result.append("======链路释放开始======").append(Constant.LineENd);
            MessageUtil<SDTPlinkRel_Req> linkRel = new MessageUtil<>(SDTPlinkRel_Req.class);
            linkRel.append(new byte[]{0x01});
            socketClient.send(linkRel.getMessage(seq.getAndIncrement()));
            socketClient.receive(clientReceive);
            logger.info("======链路释放结束======");
            result.append("======链路释放结束======").append(Constant.LineENd);
        }
        return result.toString();
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
        byte[] bytesDigests = SHA256Util.computerDigest(LoginID, sharedSecret, Timestamp, Rand);
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
            // SDTP头部各个字段转Hex
            sdtpHeader.parserToHex();
            logger.info(String.format("[SDTP头部] 长度: %s, [hexStr] %s", headerReadLen, sdtpHeader.getHexStr()));
            result.append(String.format("[SDTP头部] 长度: %s, [hexStr] %s", headerReadLen, sdtpHeader.getHexStr()));
            // 获取应答类型
            EnumMessageType msgType = sdtpHeader.getMessageType();
            // 获取SDTP.Body数据
            int lastLen = Integer.valueOf(sdtpHeader.getTotalLength() + "") - headerBodyLength;
            byte[] sdtpBodyBytes = new byte[lastLen];
            int bodyReadLen = in.read(sdtpBodyBytes);
            logger.info(String.format("[SDTP包体] 长度: %s, [hexStr] %s", lastLen, ByteUtil.bytesToHexStringH(sdtpBodyBytes)));
            result.append(String.format("[SDTP包体] 长度: %s, [hexStr] %s", lastLen, ByteUtil.bytesToHexStringH(sdtpBodyBytes))).append(Constant.LineENd);
            switch (msgType) {
                // 1:版本协商应答
                case verNego_Resp:
                    MessageUtil<SDTPverNego_Resp> verNego_Resp = new MessageUtil<>(SDTPverNego_Resp.class);
                    verNego_Resp.append(sdtpBodyBytes);
                    logger.info("版本协商应答: {}", verNego_Resp.getResp().getResult());
                    result.append("版本协商应答: ").append(verNego_Resp.getResp().getResult()).append(Constant.LineENd);
                    break;
                // 2:链路鉴权应答
                case linkAuth_Resp:
                    MessageUtil<SDTPlinkAuth_Resp> linkAuth_Resp = new MessageUtil<>(SDTPlinkAuth_Resp.class);
                    linkAuth_Resp.append(sdtpBodyBytes);
                    logger.info(String.format("链路鉴权应答, Result: %s, Digest: %s"
                            , linkAuth_Resp.getResp().getResult(), linkAuth_Resp.getResp().getDigest()));
                    result.append(String.format("链路鉴权应答, Result: %s, Digest: %s"
                            , linkAuth_Resp.getResp().getResult(), linkAuth_Resp.getResp().getDigest())).append(Constant.LineENd);
                    break;
                // 4:xdr数据发送应答
                case notifyXDRData_Resp:
                    MessageUtil<SDTPnotifyXDRData_Resp> notifyXdrData_Resp = new MessageUtil<>(SDTPnotifyXDRData_Resp.class);
                    notifyXdrData_Resp.append(sdtpBodyBytes);
                    logger.info("xdr数据发送应答: {}", notifyXdrData_Resp.getResp().getResult());
                    result.append("xdr数据发送应答: ").append(notifyXdrData_Resp.getResp().getResult()).append(Constant.LineENd);
                    break;
                // 5:链路检测应答
                case linkCheck_Resp:
                    logger.info("链路检测应答: {}", bodyReadLen);
                    result.append("链路检测应答: ").append(bodyReadLen).append(Constant.LineENd);
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
                    result.append(String.format("链路数据发送校验应答, Sendflag: %s, Result: %s, SendDataInfo: %s, RecDataInfo: %s"
                            , linkDataCheck_Resp.getResp().getSendflag()
                            , linkDataCheck_Resp.getResp().getResult()
                            , linkDataCheck_Resp.getResp().getSendDataInfo()
                            , linkDataCheck_Resp.getResp().getRecDataInfo())).append(Constant.LineENd);
                    break;
                // 7:链路释放应答
                case linkRel_Resp:
                    MessageUtil<SDTPlinkRel_Resp> linkRel_Resp = new MessageUtil<>(SDTPlinkRel_Resp.class);
                    linkRel_Resp.append(sdtpBodyBytes);
                    logger.info("链路释放应答: {}", linkRel_Resp.getResp().getResult());
                    result.append("链路释放应答: ").append(linkRel_Resp.getResp().getResult()).append(Constant.LineENd);
                    break;
                default:
                    break;
            }
            return sdtpBodyBytes;
        }
    }
}
