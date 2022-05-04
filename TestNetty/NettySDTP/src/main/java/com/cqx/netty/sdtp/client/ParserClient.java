package com.cqx.netty.sdtp.client;

import com.codahale.metrics.Meter;
import com.cqx.common.metric.MetricUtils;
import com.cqx.common.utils.system.TimeCostUtil;
import com.cqx.common.utils.thread.ThreadTool;
import com.cqx.netty.sdtp.rule.RuleBean;
import com.cqx.netty.sdtp.rule.RuleUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ParserClient
 *
 * @author chenqixu
 */
public class ParserClient {
    private static final Logger logger = LoggerFactory.getLogger(ParserClient.class);
    private Meter before;
    private Meter after;
    private Meter cnt;
    private int execCnt;

    public ParserClient(int seq) {
        before = MetricUtils.getMeter("before" + seq);
        after = MetricUtils.getMeter("after" + seq);
        cnt = MetricUtils.getMeter("cnt" + seq);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            logger.error("参数个数至少需要1个才可以！");
            System.exit(1);
        }
        int execCnt = Integer.valueOf(args[0]);
        logger.info("执行次数：{}", execCnt);
        int parallel_num = 1;
        if (args.length > 1) {
            parallel_num = Integer.valueOf(args[1]);
            logger.info("并发个数：{}", parallel_num);
        }

        AtomicInteger seq = new AtomicInteger(1);
        for (int j = 1; j <= parallel_num; j++) {
            MetricUtils.getMeter("before" + j);
            MetricUtils.getMeter("after" + j);
            MetricUtils.getMeter("cnt" + j);
        }
        MetricUtils.build(5, TimeUnit.SECONDS);

        if (parallel_num == 1) {
            ParserClient parserClient = new ParserClient(seq.getAndIncrement());
            parserClient.setExecCnt(execCnt);
            parserClient.parser();
        } else {
            ThreadTool threadTool = new ThreadTool(parallel_num);
            for (int i = 0; i < parallel_num; i++) {
                threadTool.addTask(new Runnable() {
                    @Override
                    public void run() {
                        ParserClient parserClient = new ParserClient(seq.getAndIncrement());
                        parserClient.setExecCnt(execCnt);
                        parserClient.parser();
                    }
                });
            }
            threadTool.startTask();
        }
    }

    private void parser() {
        String httpRule = "LocalProvince-L2-byte-F,LocalCity-L2-byte-F,OwnerProvince-L2-byte-F,OwnerCity-L2-byte-F,RoamingType-L1-byte-F,Interface-L1-byte-F,ProbeID-L2-byte-F,xDRID-L16-hex-F,xDRType-L1-byte-F,RAT-L1-byte-F,IMSI-L8-byte-F,PEI-L8-byte-F,MSISDN-L16-byte-F,MachineIPAddtype-L1-byte-N1,UPFSGWIPAdd-LIP[MachineIPAddtype-ip-F,gNBeNBIPAdd-LIP[MachineIPAddtype-ip-F,UPFSGWPort-L2-byte-F,gNBeNBPort-L2-byte-F,gNBeNBGTPTEID-L4-byte-F,UPFSGWGTPTEID-L4-byte-F,TAC-L4-byte-F,CellID-L5-byte-F,APN-L100-string-F,S_NSSAI_SST-L1-byte-F,S_NSSAI_SD-L4-byte-F,QosFlow15QI-L1-byte-F,QOSflowID-L1-byte-F,NextExtensionHeaderType-L1-byte-F,PGWAdd-LIP[MachineIPAddtype-ip-F,PGWPort-L2-byte-F,ProcedureStartTime-L8-byte-F,ProcedureEndTime-L8-byte-F,Longitude-L8-string-F,latitude-L8-string-F,Height-L2-byte-F,Coordinatesystem-L1-byte-F,ProtocolType-L2-byte-F,AppType-L2-byte-F,AppSubtype-L4-byte-F,AppContent-L1-byte-F,AppStatus-L1-byte-F,IPaddresstype-L1-byte-F,USER_Ipv4-L4-ip-F,USER_Ipv6-L16-ip-F,UserPort-L4-byte-F,L4protocal-L1-byte-F,AppServerIP_Ipv4-L4-ip-F,AppServerIP_Ipv6-L16-ip-F,AppServerPort-L4-byte-F,ULData-L8-byte-N0,DLData-L8-byte-N0,ULIPPacket-L4-byte-N0,DLIPPacket-L4-byte-N0,updura-L8-byte-F,downdura-L8-byte-F,ULDisorderIPPacket-L4-byte-N0,DLDisorderIPPacket-L4-byte-N0,ULRetransIPPacket-L4-byte-N0,DLRetransIPPacket-L4-byte-N0,TCPResponseTime-L4-byte-N0,TCPACKTime-L4-byte-N0,UL_IP_FRAG_PACKETS-L4-byte-N0,DL_IP_FRAG_PACKETS-L4-byte-N0,FirstReqTime-L4-byte-N0,FirstResponseTime-L4-byte-N0,Window-L4-byte-N0,MSS-L4-byte-N0,TCPSYNNum-L1-byte-N0,TCPStatus-L1-byte-F,SessionEnd-L1-byte-F,TCPSYNACKMum-L1-byte-N0,TCPACKNum-L1-byte-N0,TCP12HandshakeStatus-L1-byte-F,TCP23HandshakeStatus-L1-byte-F,ULProbeID-L2-byte-N0,ULLINKIndex-L2-byte-N0,DLProbeID-L2-byte-N0,DLLINKIndex-L2-byte-N0,TransactionID-L4-byte-N0,UL_AVG_RTT-L8-byte-N0,DW_AVG_RTT-L8-byte-N0,ReferXDRID-L48-byte-F,Rule_source-L1-byte-F,VPN-L1-byte-F,HTTPVersion-L1-byte-F,MessageType-L2-byte-F,MessageStatus-L2-byte-F,FirstHTTPResponseTime-L4-byte-F,LastContentPacketTime-L4-byte-F,LastACKTime-L4-byte-F,HOST-LV-string-F,URI-LV-string-F,XOnlineHost-LV-string-F,UserAgent-LV-string-F,HTTP_content_type-LV-string-F,refer_URI-LV-string-F,Cookie-LV-string-F,ContentLength-L4-byte-F,keyword-LV-string-F,ServiceBehaviorFlag-L1-byte-F,ServiceCompFlag-L1-byte-F,ServiceTime-L4-byte-F,IE-L1-byte-N0,Portal-L1-byte-N0,location-LV-string-F,firstrequest-L1-byte-F,Useraccount-L16-byte-F,URItype-L2-byte-F,URIsubtype-L2-byte-F";
        String httpData = "||||255|8|6149|FE000560ACCA2F0000000200A83A0000|103|10||||2|2409:8034:4040:1300:0:0:0:102|2409:8034:4021:0:0:0:140E:701|2152|2152|1205880630|68217329|4294967295|1099511627775||255|4294967295|255|1|133|FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF|65535|1620581400956000|1620581401188000|0.000000|0.000000|65535|255|1|5|17|3|0|1|255.255.255.255|2409:8934:282:2946:B10D:86BB:5B2B:FB81|55492|0|255.255.255.255|2409:8C54:881:129:0:FF:B026:25AF|80|1331|1323|6|6|225000|211000|0|0|0|1|21000|14000|0|0|5000|27000|243|1312|1|0|1|1|1|0|0|5|0|5|0|1|21|0||1|0|3|6|302|27000|27000|130000|baiducdncmn2.inter.iqiyi.com|http://baiducdncmn2.inter.iqiyi.com/videos/vts/20210130/d0/31/af1a11ad7d34e21070bc89c066986a3c.ts?key=0f7daec203cd0441c4fbeaadc38daacc0&dis_k=44c37d8a19ea8673faaa2096eaee2bfb&dis_t=1612025412&dis_dz=CMNET-FuJian_FuZhou&dis_st=49&src=iqiyi.com&dis_hit=0&dis_tag=02000000&uuid=df683305-60158e44-250&hotlevel=4&sgti=&qd_uid=0&qd_tm=1612025412720&sd=0&start=0&ve=&end=142128&qd_ip=df683305&dfp=&qd_tvid=2984558307645000&qypid=2984558307645000_04000000001000000000_75&qd_p=df683305&qd_k=916afb7be3624ecc3f522f02737a9ee0&qd_src=02029022240000000000&qd_vip=0&contentlength=142128&z=baiducdn2_cmnet||HUAWEI-LIO-AN00__weibo__11.1.3__android__android10_unknown Lavf/57.41.100|text/html|||0||3|0|232000|0|0|http://[2409:8c34:0c00:0006::112.49.48.41]/r/baiducdncmn2.inter.iqiyi.com/videos/vts/20210130/d0/31/af1a11ad7d34e21070bc89c0669|1||65535|65535";
        RuleUtil ruleUtil = new RuleUtil();
        List<RuleBean> ruleBeanList = ruleUtil.generateRule(httpRule);

        // 逆向封装
        String[] columns = httpData.split("\\|");
        byte[] bytes = ruleUtil.reverse(ruleBeanList, columns);
        int srcLength = bytes.length;
        logger.info("source length: {}", srcLength);
        ByteBuf byteBuf = Unpooled.buffer(bytes.length);
        byteBuf.writeBytes(bytes);

        // 解析
        String tmp = ruleUtil.parser(ruleBeanList, byteBuf);
        int dstLength = tmp.getBytes(StandardCharsets.UTF_8).length;
        logger.info("dst length: {}", dstLength);

        TimeCostUtil tc = new TimeCostUtil();
        tc.start();
        for (int i = 0; i < execCnt; i++) {
            cnt.mark();
            before.mark(srcLength);
            byteBuf.resetReaderIndex();
            // 解析
            ruleUtil.parser(ruleBeanList, byteBuf);
            after.mark(dstLength);
        }
        logger.info("tc: {}", tc.stopAndGet());
    }

    public void setExecCnt(int execCnt) {
        this.execCnt = execCnt;
    }
}