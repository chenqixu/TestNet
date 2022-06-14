package com.cqx.netty.sdtp.client;

import com.cqx.common.utils.system.SleepUtil;
import com.cqx.netty.sdtp.bean.*;
import com.cqx.netty.sdtp.util.MessageUtil;
import com.cqx.netty.util.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * SDTPClient
 *
 * @author chenqixu
 */
public class SDTPClient {
    private static final Logger logger = LoggerFactory.getLogger(SDTPClient.class);
    private String ip;
    private int port;
    private int sendCnt;

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("参数个数不足！需要三个参数");
            System.exit(-1);
        }
        String ip = args[0];
        int port = Integer.valueOf(args[1]);
        int sendCnt = Integer.valueOf(args[2]);

        SDTPClient sdtpClient = new SDTPClient();
        sdtpClient.setIp(ip);
        sdtpClient.setPort(port);
        sdtpClient.setSendCnt(sendCnt);
//        sdtpClient.XDR();
//        sdtpClient.XDRRaw();
        sdtpClient.XDRKeepAlive();
    }

    private void XDRRaw() throws IOException {
        // jdk8语法糖自动释放
        try (SocketClient socketClient = SocketClient.newbuilder()
                .setIp(ip)
                .setPort(port)
                .build()) {
            ClientReceive clientReceive = new ClientReceive();

            // 发送XDRRaw数据
            MessageUtil<SDTPXDRRawDataSend_Req> notifyXDRRawData = new MessageUtil<>(SDTPXDRRawDataSend_Req.class);
            FileInputStream xdr_raw_file = new FileInputStream("d:\\Work\\ETL\\上网日志查询2022\\data\\VoLTE_zh_301661567184062570_3756203956355458672_8613799720280_0_20220315_145842_011052_32_3756201550215243625.cap");
            int fileSize = xdr_raw_file.available();
            byte[] fileDatas = new byte[fileSize];
            int fileReadLen = xdr_raw_file.read(fileDatas);
            notifyXDRRawData.append(fileDatas);
            byte[] bytes = notifyXDRRawData.getMessage();
            socketClient.send(bytes);
            socketClient.receive(clientReceive);

            // 发送链接释放请求
            MessageUtil<SDTPlinkRel_Req> linkRel = new MessageUtil<>(SDTPlinkRel_Req.class);
            linkRel.append(new byte[]{0x01});
            socketClient.send(linkRel.getMessage());
            socketClient.receive(clientReceive);
        }
    }

    private void XDR() throws IOException {
        // jdk8语法糖自动释放
        try (SocketClient socketClient = SocketClient.newbuilder()
                .setIp(ip)
                .setPort(port)
                .build()) {
            ClientReceive clientReceive = new ClientReceive();

            // 发送XDR数据
            String httpRule = "LocalProvince-L2-byte-F,LocalCity-L2-byte-F,OwnerProvince-L2-byte-F,OwnerCity-L2-byte-F,RoamingType-L1-byte-F,Interface-L1-byte-F,ProbeID-L2-byte-F,xDRID-L16-hex-F,xDRType-L1-byte-F,RAT-L1-byte-F,IMSI-L8-byte-F,PEI-L8-byte-F,MSISDN-L16-byte-F,MachineIPAddtype-L1-byte-N1,UPFSGWIPAdd-LIP[MachineIPAddtype-ip-F,gNBeNBIPAdd-LIP[MachineIPAddtype-ip-F,UPFSGWPort-L2-byte-F,gNBeNBPort-L2-byte-F,gNBeNBGTPTEID-L4-byte-F,UPFSGWGTPTEID-L4-byte-F,TAC-L4-byte-F,CellID-L5-byte-F,APN-L100-string-F,S_NSSAI_SST-L1-byte-F,S_NSSAI_SD-L4-byte-F,QosFlow15QI-L1-byte-F,QOSflowID-L1-byte-F,NextExtensionHeaderType-L1-byte-F,PGWAdd-LIP[MachineIPAddtype-ip-F,PGWPort-L2-byte-F,ProcedureStartTime-L8-byte-F,ProcedureEndTime-L8-byte-F,Longitude-L8-string-F,latitude-L8-string-F,Height-L2-byte-F,Coordinatesystem-L1-byte-F,ProtocolType-L2-byte-F,AppType-L2-byte-F,AppSubtype-L4-byte-F,AppContent-L1-byte-F,AppStatus-L1-byte-F,IPaddresstype-L1-byte-F,USER_Ipv4-L4-ip-F,USER_Ipv6-L16-ip-F,UserPort-L4-byte-F,L4protocal-L1-byte-F,AppServerIP_Ipv4-L4-ip-F,AppServerIP_Ipv6-L16-ip-F,AppServerPort-L4-byte-F,ULData-L8-byte-N0,DLData-L8-byte-N0,ULIPPacket-L4-byte-N0,DLIPPacket-L4-byte-N0,updura-L8-byte-F,downdura-L8-byte-F,ULDisorderIPPacket-L4-byte-N0,DLDisorderIPPacket-L4-byte-N0,ULRetransIPPacket-L4-byte-N0,DLRetransIPPacket-L4-byte-N0,TCPResponseTime-L4-byte-N0,TCPACKTime-L4-byte-N0,UL_IP_FRAG_PACKETS-L4-byte-N0,DL_IP_FRAG_PACKETS-L4-byte-N0,FirstReqTime-L4-byte-N0,FirstResponseTime-L4-byte-N0,Window-L4-byte-N0,MSS-L4-byte-N0,TCPSYNNum-L1-byte-N0,TCPStatus-L1-byte-F,SessionEnd-L1-byte-F,TCPSYNACKMum-L1-byte-N0,TCPACKNum-L1-byte-N0,TCP12HandshakeStatus-L1-byte-F,TCP23HandshakeStatus-L1-byte-F,ULProbeID-L2-byte-N0,ULLINKIndex-L2-byte-N0,DLProbeID-L2-byte-N0,DLLINKIndex-L2-byte-N0,TransactionID-L4-byte-N0,UL_AVG_RTT-L8-byte-N0,DW_AVG_RTT-L8-byte-N0,ReferXDRID-L48-byte-F,Rule_source-L1-byte-F,VPN-L1-byte-F,HTTPVersion-L1-byte-F,MessageType-L2-byte-F,MessageStatus-L2-byte-F,FirstHTTPResponseTime-L4-byte-F,LastContentPacketTime-L4-byte-F,LastACKTime-L4-byte-F,HOST-LV-string-F,URI-LV-string-F,XOnlineHost-LV-string-F,UserAgent-LV-string-F,HTTP_content_type-LV-string-F,refer_URI-LV-string-F,Cookie-LV-string-F,ContentLength-L4-byte-F,keyword-LV-string-F,ServiceBehaviorFlag-L1-byte-F,ServiceCompFlag-L1-byte-F,ServiceTime-L4-byte-F,IE-L1-byte-N0,Portal-L1-byte-N0,location-LV-string-F,firstrequest-L1-byte-F,Useraccount-L16-byte-F,URItype-L2-byte-F,URIsubtype-L2-byte-F";
            String httpData = "||||255|8|6149|FE000560ACCA2F0000000200A83A0000|103|10||||2|2409:8034:4040:1300:0:0:0:102|2409:8034:4021:0:0:0:140E:701|2152|2152|1205880630|68217329|4294967295|1099511627775||255|4294967295|255|1|133|FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF|65535|1620581400956000|1620581401188000|0.000000|0.000000|65535|255|1|5|17|3|0|1|255.255.255.255|2409:8934:282:2946:B10D:86BB:5B2B:FB81|55492|0|255.255.255.255|2409:8C54:881:129:0:FF:B026:25AF|80|1331|1323|6|6|225000|211000|0|0|0|1|21000|14000|0|0|5000|27000|243|1312|1|0|1|1|1|0|0|5|0|5|0|1|21|0||1|0|3|6|302|27000|27000|130000|baiducdncmn2.inter.iqiyi.com|http://baiducdncmn2.inter.iqiyi.com/videos/vts/20210130/d0/31/af1a11ad7d34e21070bc89c066986a3c.ts?key=0f7daec203cd0441c4fbeaadc38daacc0&dis_k=44c37d8a19ea8673faaa2096eaee2bfb&dis_t=1612025412&dis_dz=CMNET-FuJian_FuZhou&dis_st=49&src=iqiyi.com&dis_hit=0&dis_tag=02000000&uuid=df683305-60158e44-250&hotlevel=4&sgti=&qd_uid=0&qd_tm=1612025412720&sd=0&start=0&ve=&end=142128&qd_ip=df683305&dfp=&qd_tvid=2984558307645000&qypid=2984558307645000_04000000001000000000_75&qd_p=df683305&qd_k=916afb7be3624ecc3f522f02737a9ee0&qd_src=02029022240000000000&qd_vip=0&contentlength=142128&z=baiducdn2_cmnet||HUAWEI-LIO-AN00__weibo__11.1.3__android__android10_unknown Lavf/57.41.100|text/html|||0||3|0|232000|0|0|http://[2409:8c34:0c00:0006::112.49.48.41]/r/baiducdncmn2.inter.iqiyi.com/videos/vts/20210130/d0/31/af1a11ad7d34e21070bc89c0669|1||65535|65535";
            MessageUtil<SDTPnotifyXDRData> notifyXDRData = new MessageUtil<>(SDTPnotifyXDRData.class, httpRule);
            for (int i = 0; i < 40; i++) {
                notifyXDRData.append(httpData);
            }
            byte[] bytes = notifyXDRData.getMessage();
            logger.info("发送的XDR数据的原始数据长度：{}", bytes.length);
            for (int i = 0; i < sendCnt; i++) {
                socketClient.send(bytes);
                socketClient.receive(clientReceive);
            }

            // 发送链接释放请求
            MessageUtil<SDTPlinkRel_Req> linkRel = new MessageUtil<>(SDTPlinkRel_Req.class);
            linkRel.append(new byte[]{0x01});
            byte[] relBytes = linkRel.getMessage();
            logger.info("发送的链接释放请求的原始数据长度：{}", relBytes.length);
            socketClient.send(relBytes);
            socketClient.receive(clientReceive);
        }
    }

    private void XDRKeepAlive() throws IOException {
        // jdk8语法糖自动释放
        try (SocketClient socketClient = SocketClient.newbuilder()
                .setIp(ip)
                .setPort(port)
                .build()) {
            ClientReceive clientReceive = new ClientReceive();

            // 发送XDR数据
            String httpRule = "LocalProvince-L2-byte-F,LocalCity-L2-byte-F,OwnerProvince-L2-byte-F,OwnerCity-L2-byte-F,RoamingType-L1-byte-F,Interface-L1-byte-F,ProbeID-L2-byte-F,xDRID-L16-hex-F,xDRType-L1-byte-F,RAT-L1-byte-F,IMSI-L8-byte-F,PEI-L8-byte-F,MSISDN-L16-byte-F,MachineIPAddtype-L1-byte-N1,UPFSGWIPAdd-LIP[MachineIPAddtype-ip-F,gNBeNBIPAdd-LIP[MachineIPAddtype-ip-F,UPFSGWPort-L2-byte-F,gNBeNBPort-L2-byte-F,gNBeNBGTPTEID-L4-byte-F,UPFSGWGTPTEID-L4-byte-F,TAC-L4-byte-F,CellID-L5-byte-F,APN-L100-string-F,S_NSSAI_SST-L1-byte-F,S_NSSAI_SD-L4-byte-F,QosFlow15QI-L1-byte-F,QOSflowID-L1-byte-F,NextExtensionHeaderType-L1-byte-F,PGWAdd-LIP[MachineIPAddtype-ip-F,PGWPort-L2-byte-F,ProcedureStartTime-L8-byte-F,ProcedureEndTime-L8-byte-F,Longitude-L8-string-F,latitude-L8-string-F,Height-L2-byte-F,Coordinatesystem-L1-byte-F,ProtocolType-L2-byte-F,AppType-L2-byte-F,AppSubtype-L4-byte-F,AppContent-L1-byte-F,AppStatus-L1-byte-F,IPaddresstype-L1-byte-F,USER_Ipv4-L4-ip-F,USER_Ipv6-L16-ip-F,UserPort-L4-byte-F,L4protocal-L1-byte-F,AppServerIP_Ipv4-L4-ip-F,AppServerIP_Ipv6-L16-ip-F,AppServerPort-L4-byte-F,ULData-L8-byte-N0,DLData-L8-byte-N0,ULIPPacket-L4-byte-N0,DLIPPacket-L4-byte-N0,updura-L8-byte-F,downdura-L8-byte-F,ULDisorderIPPacket-L4-byte-N0,DLDisorderIPPacket-L4-byte-N0,ULRetransIPPacket-L4-byte-N0,DLRetransIPPacket-L4-byte-N0,TCPResponseTime-L4-byte-N0,TCPACKTime-L4-byte-N0,UL_IP_FRAG_PACKETS-L4-byte-N0,DL_IP_FRAG_PACKETS-L4-byte-N0,FirstReqTime-L4-byte-N0,FirstResponseTime-L4-byte-N0,Window-L4-byte-N0,MSS-L4-byte-N0,TCPSYNNum-L1-byte-N0,TCPStatus-L1-byte-F,SessionEnd-L1-byte-F,TCPSYNACKMum-L1-byte-N0,TCPACKNum-L1-byte-N0,TCP12HandshakeStatus-L1-byte-F,TCP23HandshakeStatus-L1-byte-F,ULProbeID-L2-byte-N0,ULLINKIndex-L2-byte-N0,DLProbeID-L2-byte-N0,DLLINKIndex-L2-byte-N0,TransactionID-L4-byte-N0,UL_AVG_RTT-L8-byte-N0,DW_AVG_RTT-L8-byte-N0,ReferXDRID-L48-byte-F,Rule_source-L1-byte-F,VPN-L1-byte-F,HTTPVersion-L1-byte-F,MessageType-L2-byte-F,MessageStatus-L2-byte-F,FirstHTTPResponseTime-L4-byte-F,LastContentPacketTime-L4-byte-F,LastACKTime-L4-byte-F,HOST-LV-string-F,URI-LV-string-F,XOnlineHost-LV-string-F,UserAgent-LV-string-F,HTTP_content_type-LV-string-F,refer_URI-LV-string-F,Cookie-LV-string-F,ContentLength-L4-byte-F,keyword-LV-string-F,ServiceBehaviorFlag-L1-byte-F,ServiceCompFlag-L1-byte-F,ServiceTime-L4-byte-F,IE-L1-byte-N0,Portal-L1-byte-N0,location-LV-string-F,firstrequest-L1-byte-F,Useraccount-L16-byte-F,URItype-L2-byte-F,URIsubtype-L2-byte-F";
            String httpData = "||||255|8|6149|FE000560ACCA2F0000000200A83A0000|103|10||||2|2409:8034:4040:1300:0:0:0:102|2409:8034:4021:0:0:0:140E:701|2152|2152|1205880630|68217329|4294967295|1099511627775||255|4294967295|255|1|133|FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF|65535|1620581400956000|1620581401188000|0.000000|0.000000|65535|255|1|5|17|3|0|1|255.255.255.255|2409:8934:282:2946:B10D:86BB:5B2B:FB81|55492|0|255.255.255.255|2409:8C54:881:129:0:FF:B026:25AF|80|1331|1323|6|6|225000|211000|0|0|0|1|21000|14000|0|0|5000|27000|243|1312|1|0|1|1|1|0|0|5|0|5|0|1|21|0||1|0|3|6|302|27000|27000|130000|baiducdncmn2.inter.iqiyi.com|http://baiducdncmn2.inter.iqiyi.com/videos/vts/20210130/d0/31/af1a11ad7d34e21070bc89c066986a3c.ts?key=0f7daec203cd0441c4fbeaadc38daacc0&dis_k=44c37d8a19ea8673faaa2096eaee2bfb&dis_t=1612025412&dis_dz=CMNET-FuJian_FuZhou&dis_st=49&src=iqiyi.com&dis_hit=0&dis_tag=02000000&uuid=df683305-60158e44-250&hotlevel=4&sgti=&qd_uid=0&qd_tm=1612025412720&sd=0&start=0&ve=&end=142128&qd_ip=df683305&dfp=&qd_tvid=2984558307645000&qypid=2984558307645000_04000000001000000000_75&qd_p=df683305&qd_k=916afb7be3624ecc3f522f02737a9ee0&qd_src=02029022240000000000&qd_vip=0&contentlength=142128&z=baiducdn2_cmnet||HUAWEI-LIO-AN00__weibo__11.1.3__android__android10_unknown Lavf/57.41.100|text/html|||0||3|0|232000|0|0|http://[2409:8c34:0c00:0006::112.49.48.41]/r/baiducdncmn2.inter.iqiyi.com/videos/vts/20210130/d0/31/af1a11ad7d34e21070bc89c0669|1||65535|65535";
            MessageUtil<SDTPnotifyXDRData> notifyXDRData = new MessageUtil<>(SDTPnotifyXDRData.class, httpRule);
            for (int i = 0; i < 40; i++) {
                notifyXDRData.append(httpData);
            }
            byte[] bytes = notifyXDRData.getMessage();
            logger.info("发送的XDR数据的原始数据长度：{}", bytes.length);
            socketClient.send(bytes);
            socketClient.receive(clientReceive);

            // 休眠15秒
            SleepUtil.sleepSecond(15);
            socketClient.send(bytes);
            socketClient.receive(clientReceive);
        }
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSendCnt(int sendCnt) {
        this.sendCnt = sendCnt;
    }

    class ClientReceive implements SocketClient.ReceiveCall {

        @Override
        public void read(InputStream in) throws IOException {
            // 先读SDTP头部，定长12个字节
            byte[] sdtpHeaderBytes = new byte[12];
            int headerReadLen = in.read(sdtpHeaderBytes);
            SDTPMessage sdtpMessage = MessageUtil.parser(sdtpHeaderBytes);
            SDTPHeader sdtpHeader = sdtpMessage.getSdtpHeader();
            // 获取SDTP.Body数据
            int lastLen = Integer.valueOf(sdtpHeader.getTotalLength() + "") - 12;
            byte[] sdtpBodyBytes = new byte[lastLen];
            int bodyReadLen = in.read(sdtpBodyBytes);
            logger.info("SDTPHeader: {}，bodyReadLen：{}", sdtpHeader, bodyReadLen);
        }
    }
}
