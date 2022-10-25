package com.cqx.netty.sdtp.rule;

import com.cqx.common.utils.system.ByteUtil;
import com.cqx.common.utils.system.TimeCostUtil;
import com.cqx.netty.sdtp.bean.*;
import com.cqx.netty.sdtp.util.MessageUtil;
import com.cqx.netty.sdtp.util.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class RuleUtilTest {
    private static final Logger logger = LoggerFactory.getLogger(RuleUtilTest.class);
    private static final String COMMON_RULE = "Length-L2-byte-F,LocalProvince-L2-byte-F,LocalCity-L2-byte-F,OwnerProvince-L2-byte-F,OwnerCity-L2-byte-F,RoamingType-L1-byte-F,Interface-L1-byte-F,ProbeID-L2-byte-F,XDRID-L16-hex-F,RAT-L1-byte-F,IMSI-L8-byte-F,PEI-L8-byte-F,MSISDN-L16-byte-F,";
    private static String[] http = new String[]{"1235|||||255|8|6149|FE000560ACCA2F0000000200A83A0000|103|10||||2|2409:8034:4040:1300:0:0:0:102|2409:8034:4021:0:0:0:140E:701|2152|2152|1205880630|68217329|4294967295|1099511627775||255|4294967295|255|1|133|FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF|65535|1620581400956000|1620581401188000|0.000000|0.000000|65535|255|1|5|17|3|0|1|255.255.255.255|2409:8934:282:2946:B10D:86BB:5B2B:FB81|55492|0|255.255.255.255|2409:8C54:881:129:0:FF:B026:25AF|80|1331|1323|6|6|225000|211000|0|0|0|1|21000|14000|0|0|5000|27000|243|1312|1|0|1|1|1|0|0|5|0|5|0|1|21|0||1|0|3|6|302|27000|27000|130000|baiducdncmn2.inter.iqiyi.com|http://baiducdncmn2.inter.iqiyi.com/videos/vts/20210130/d0/31/af1a11ad7d34e21070bc89c066986a3c.ts?key=0f7daec203cd0441c4fbeaadc38daacc0&dis_k=44c37d8a19ea8673faaa2096eaee2bfb&dis_t=1612025412&dis_dz=CMNET-FuJian_FuZhou&dis_st=49&src=iqiyi.com&dis_hit=0&dis_tag=02000000&uuid=df683305-60158e44-250&hotlevel=4&sgti=&qd_uid=0&qd_tm=1612025412720&sd=0&start=0&ve=&end=142128&qd_ip=df683305&dfp=&qd_tvid=2984558307645000&qypid=2984558307645000_04000000001000000000_75&qd_p=df683305&qd_k=916afb7be3624ecc3f522f02737a9ee0&qd_src=02029022240000000000&qd_vip=0&contentlength=142128&z=baiducdn2_cmnet||HUAWEI-LIO-AN00__weibo__11.1.3__android__android10_unknown Lavf/57.41.100|text/html|||0||3|0|232000|0|0|http://[2409:8c34:0c00:0006::112.49.48.41]/r/baiducdncmn2.inter.iqiyi.com/videos/vts/20210130/d0/31/af1a11ad7d34e21070bc89c0669|1||65535|65535"};

    @Test
    public void newParser() throws Exception {
        // fieldName-readLengthRule-parserRule-defaultValueRule-groupRule
        // V：数据的二进制形式
        // LV：L表示长度，占两个byte，V表示数据的二进制形式
        // TLV（TV）：需要按Tag进行包装
        // TLV：T和F占前面两个byte，L占中间两个byte，V在最后，反向解析的时候需要读取L
        // TV：T和F占前面两个byte，V在最后，反向解析的时候需要读取F
        // readLengthRule：Lx，LV，LIP，
        // GROUP：g1，gp
        String rule1 = "p1-L1-byte-N1,p2-L2-byte-F,p6-LV-string-F,p9-LIP[p1-ip-F,p10-T1L1-byte-N1-G1,p11-T2L1-byte-N1-GP,p12-T3LV-string-F-GP";
        // Ln
        // LV
        // LIP[
        // TxL0，因为length由format决定
        // TxLV
        // TxLIP[
        // p12-T11L5-byte-F
        // p13-T3LV-string-F
        RuleUtil ruleUtil = new RuleUtil();
        List<MultipleRuleBean> multipleRuleBeans = ruleUtil.generateMultipleRule(rule1);
        for (MultipleRuleBean multipleRuleBean : multipleRuleBeans) {
            logger.info("{}", multipleRuleBean, multipleRuleBean.getGroupFirstRule());
            if (multipleRuleBean.getRuleType().equals(EnumRuleType.SINGLE)) {
                logger.info("SINGLE：{}", multipleRuleBean.getSingleRuleBean());
            } else if (multipleRuleBean.getRuleType().equals(EnumRuleType.GROUP)) {
                logger.info("GROUP_First：{}", multipleRuleBean.getGroupFirstRule());
                logger.info("GROUP_Body：{}", multipleRuleBean.getGroupRuleBeanList());
            }
        }
        ByteBuf data = Unpooled.buffer(3);
        // p1，因为全F，所以触发了N1默认值
        data.writeByte(0xff);
        // p2
        data.writeShort(255);
        // p6
        data.writeShort(6);
        String p6 = "测试";
        data.writeBytes(p6.getBytes(StandardCharsets.UTF_8));
        // p9
        String ip = "10.1.8.203";//"234e:3:4567:0::3a";//"10.1.8.203";
        InetAddress host = InetAddress.getByName(ip);
        data.writeBytes(host.getAddress());
        // p10，TV格式，tag1
        data.writeByte(0x01); // 快索引低2位0，tag：1
        data.writeByte(0x01); // 块索引高4位0，format：1
        data.writeByte(0x02); // V值
        // =========================
        // p11，TV格式，tag2
        data.writeByte(0x02); // 快索引低2位0，tag：2
        data.writeByte(0x01); // 块索引高4位0，format：1
        data.writeByte(0x05); // V值
        // p12，TLV格式，tag3
        data.writeByte(0x03); // 快索引低2位0，tag：3
        data.writeByte(0x06); // 块索引高4位0，format：6
        data.writeBytes("你好".getBytes(StandardCharsets.UTF_8)); // V值，UTF-8下，一个中文3个字节
        // =========================
        // p11，TV格式，tag2
        data.writeByte(0x02); // 快索引低2位0，tag：2
        data.writeByte(0x01); // 块索引高4位0，format：1
        data.writeByte(0x06); // V值
        // p12，TLV格式，tag3
        data.writeByte(0x03); // 快索引低2位0，tag：3
        data.writeByte(0x00); // 块索引高4位0，format：0
        data.writeByte(0x00); // 长度
        data.writeByte("天黑了".getBytes(StandardCharsets.UTF_8).length); // 长度
        data.writeBytes("天黑了".getBytes(StandardCharsets.UTF_8)); // V值，UTF-8下，一个中文3个字节
        // 解析
        String ret = ruleUtil.parserMultiple(multipleRuleBeans, data);
        logger.info(String.format("结果：%s", ret));
    }

    @Test
    public void reverseNew() {
        String datas = "1|255|测试|10.1.8.203|2|5|你好|6|天黑了";
        String rule2 = "p1-L1-byte-N1,p2-L2-byte-F,p6-LV-string-F,p9-LIP[p1-ip-F,p10-T1L1-byte-N1-G1,p11-T2L1-byte-N1-GP,p12-T3LV-string-F-GP";
        reverseAll(rule2, datas);
    }

    private void reverseAll(String rule, String datas) {
        String[] data = datas.split("\\|", -1);
        RuleUtil ruleUtil = new RuleUtil();
        List<MultipleRuleBean> multipleRuleBeans = ruleUtil.generateMultipleRule(rule);
        // 除了反向解析，还要做规则和字段个数校验
        byte[] bytes = ruleUtil.reverseMultiple(multipleRuleBeans, data);
        logger.info("反向解析出来的数据长度：{}", bytes.length);
        ByteBuf byteBuf = Unpooled.buffer(bytes.length);
        byteBuf.writeBytes(bytes);
        String ret = ruleUtil.parserMultiple(multipleRuleBeans, byteBuf, "|");
        logger.info(String.format("结果：%s", ret));
    }

    @Test
    public void N1N2Test() {
        String datas = "215|591|0599|591|0599|4|39|5179|000040003b042c040005e165ead4a589|10|460026509653255|8621540687538907|8618250965100|22|1666146899390|1666146899457|1666146899650|1666146899705|||||7|1|1|20||||||||||||||||||||||||||||||1|||||||||||||||||||||2409:802F:5015:1815:0:0:1101:301|2409:8034:4029:7C:0:0:0:1|584168781063|||||6|768|3|3530167559|1|3299742|8760914441||||||||||||||||||||||||||||||||||||||||||||||||||||||||1|8|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||5248306||||||||1";
        String n1n2_rule = COMMON_RULE + "proceduretype-L2-byte-F,procedurestarttimeonForwardingGW-L8-byte-N0,procedureendtimeonForwardingGW-L8-byte-N0,procedurestarttimeonProbe-L8-byte-N0,procedureendtimeonProbe-L8-byte-N0,startlocationlongitude-L8-double-F,startlocationlatitude-L8-double-F,StartLocationaltitude-L2-byte-F,locationsource-L1-byte-F,Msgflag-L1-byte-F,procedurestatus-L1-byte-F,requestcausegroup-L1-byte-F,requestcausespecific-L1-byte-F,failurecausegroup-L1-byte-F,failurecausespecific-L1-byte-F,Registrationtype-T0L1-byte-F,Registrationresult-T1L1-byte-F,Deregistrationtype-T2L1-byte-F,Deregistrationdirection-T3L1-byte-F,Reregistrationflag-T4L1-byte-F,Servicetype-T5L1-byte-F,pagingtacnumber-T6L1-byte-F,PagingOrigin-T7L1-byte-F,Accesstype-T8L1-byte-F,AuthType-T9L1-byte-F,AuthFailuredirection-T10L1-byte-F,SelectedNAScipheringalgorithm-T11L1-byte-F,SelectedNASintegrityprotectionalgorithm-T12L1-byte-F,UE'susagesetting-T13L1-byte-F,S1modesupport-T14L1-byte-F,IMSVoPS-T15L1-byte-F,IWKN26-T16L1-byte-F,MulithomedIpv6support-T17L1-byte-F,T3512-T18L4-byte-F,Backofftimer-T19L1-byte-F,MICOindication-T20L1-byte-F,NegotiatedDRXparameters-T21L1-byte-F,ULSESSIONAMBR-T22L8-byte-F,DLSESSIONAMBR-T23L8-byte-F,S_NSSAI_SST-T24L1-byte-F,S_NSSAI_SD-T25L4-byte-F,HandoverType-T26L1-byte-F,Releasedirection-T27L1-byte-F,DLIntraMaxCCNumber-T28L1-byte-F,DLInterMaxCCNumber-T29L1-byte-F,ULIntraMaxCCNumber-T30L1-byte-F,ULInterMaxCCNumber-T31L1-byte-F,SMSindication-T32L1-byte-F,SMStype-T33L1-byte-F,peernumber-T34L13-byte-F,smcnumber-T35L13-byte-F,ResetType-T36L1-byte-F,AMFtrafficloadreductionindication-T37L1-byte-F,firstfailtransprot-T38L1-byte-F,firstfailtrans-T39L1-byte-F,firstfaildirection-T40L1-byte-F,firstfailcausetype-T41L1-byte-F,firstfailcause-T42L1-byte-F,E2Efirstfailtransprot-T43L1-byte-F,E2Efirstfailtrans-T44L2-byte-F,E2Efirstfailcausetype-T45L2-byte-F,E2Efirstfailcause-T46L2-byte-F,SUCI-T47LV-byte-F,amfipadd-LV-ip-F,gnbipadd-LV-ip-F,amfuengapid-T50L5-byte-F,oldamfregionid-T51L1-byte-F,oldamfsetid-T52L2-byte-F,oldamfpointer-T53L1-byte-F,old5gtmsi-T54L4-byte-F,amfregionid-T55L1-byte-F,amfsetid-T56L2-byte-F,amfpointer-T57L1-byte-F,5gtmsi-T58L4-byte-F,locationtype-T59L1-byte-F,Tac-T60L4-byte-F,cellid-T61L5-byte-F,otherlocationtype-T62L1-byte-F,othertac-T63L4-byte-F,othercellid-T64L5-byte-F,rannodeidlength-T65L1-byte-F,rannodeid-T66L4-byte-F,AllowedNSSAInumber-T100L1-byte-F-G1,AllowedNSSAI1SST-T101L1-byte-F-GP,AllowedNSSAI1SD-T102L3-byte-F-GP,AllowedNSSAI1MappedHPLMNSST-T103L1-byte-F-GP,AllowedNSSAI1MappedHPLMNSD-T104L3-byte-F-GP,RejectedNSSAInumber-T105L1-byte-F-G1,RejectedNSSAI1SST-T106L1-byte-F-GP,RejectedNSSAI1SD-T107L3-byte-F-GP,pdusessionnumber-T108L1-byte-F-G1,pdusession1id-T109L1-byte-F-GP,OldPDUsession1ID-T110L1-byte-F-GP,pdusession1reqtype-T111L1-byte-F-GP,pdusession1type-T112L1-byte-F-GP,pdusession1dnn-T113LV-string-F-GP,PDUSession1DNNtype-T114L1-byte-F-GP,PDUSession1direction-T115L1-byte-F-GP,pdusession1useripv4-T116L4-ip-F-GP,pdusession1useripv6-T117L16-ip-F-GP,PDUSession1AlwaysonPDUsessionindication-T118L1-byte-F-GP,pdusession1reqsscmode-T119L1-byte-F-GP,pdusession1sscmode-T120L1-byte-F-GP,pdusession1n3anIPv4Addr-T121L4-ip-F-GP,pdusession1n3anIPv6Addr-T147L16-ip-F-GP,pdusession1n3upfIPv4Addr-T122L4-ip-F-GP,pdusession1n3upfIPv6Addr-T148L16-ip-F-GP,pdusession1n3anteid-T123L4-byte-F-GP,pdusession1n3upfteid-T124L4-byte-F-GP,pdusession1status-T125L1-byte-F-GP,pdusession1failurecausegroup-T126L1-byte-F-GP,pdusession1failurecausespecific-T127L1-byte-F-GP,qosflownumber-T128L1-byte-F-G1,qosflow1id-T129L1-byte-F-GP,qosflow1relatedpsi-T130L1-byte-F-GP,epsbearerid-T131L1-byte-F-GP,qosflow15qi-T132L1-byte-F-GP,qosflow1prioritylevel-T133L1-byte-F-GP,qosflow1ulgbr-T134L4-byte-F-GP,qosflow1dlgbr-T135L4-byte-F-GP,qosflow1ulmbr-T136L4-byte-F-GP,qosflow1dlmbr-T137L4-byte-F-GP,qosflow1status-T149L1-byte-F-GP";
        reverseAll(n1n2_rule, datas);
    }

    @Test
    public void N14Test() {
        String datas = "|||||47|5179|0000c0003b0827060005e165f127a5ab|10||||100|1666117520415|1666117520431|1666117520440|1666117520443|2|1|404|509|2409:802E:5003:1815:0:0:1101:201|2409:8027:5003:1813:0:0:211:201|11792|80|2||||2|||||1|";
        String n14_rule = Constant.COMMON_NOLENGTH_RULE + "ProcedureType-L1-byte-F,procedurestarttimeonForwardingGW-L8-byte-N0,procedureendtimeonForwardingGW-L8-byte-N0,procedurestarttimeonProbe-L8-byte-N0,procedureendtimeonProbe-L8-byte-N0,ProcedureStatus-L1-byte-F,httpreqtype-L1-byte-F,statuscode-L2-byte-F,FailureCause-L2-byte-F,SourceAMFAddress-LV-ip-F,DestinationAMFAddress-LV-ip-F,SourceAMFPort-L2-byte-F,DestinationAMFPort-L2-byte-F,Subproceduretype-L1-byte-F,RequestCause-L1-byte-F,USER_IPv4-T0L4-ip-F,USER_IPv6-T1L16-ip-F,TransferReason-T2L2-byte-F,pdusessionid-T3L1-byte-F,DNN-T4LV-string-F,TAC-T5L2-byte-F,CellID-T6L4-byte-F,accesstype-T7L1-byte-F,locationtype-T8L1-byte-F";
        reverseAll(n14_rule, datas);
    }

    @Test
    public void parser() throws Exception {
        String rule1 = "p1-L1-byte-F,p2-L2-byte-F,p3-L2-byte-F,p4-L4-int-F,p5-L4-int-F,p6-LV-string-NULL,p7-L1-byte-0,p8-L16-hex-F";
        String rule2 = "p1-L1-byte-N1,p2-L2-byte-F,p6-LV-string-F,p9-LIP[p1-ip-F";
        RuleUtil ruleUtil = new RuleUtil();
        List<RuleBean> ruleBeanList = ruleUtil.generateRule(rule2);
        ByteBuf data = Unpooled.buffer(3);
        // p1
        data.writeByte(0xff);
        // p2
        data.writeShort(255);
        // p6
        data.writeShort(6);
        String p6 = "测试";
        data.writeBytes(p6.getBytes(StandardCharsets.UTF_8));
        // p9
        String ip = "10.1.8.203";//"234e:3:4567:0::3a";//"10.1.8.203";
        InetAddress host = InetAddress.getByName(ip);
        data.writeBytes(host.getAddress());
        // 解析
        String ret = ruleUtil.parser(ruleBeanList, data);
//        data.resetReaderIndex();
//        Map<String, String> mapRet = ruleUtil.parserToMap(ruleBeanList, data);
        logger.info(String.format("结果：%s", ret));
    }

    @Test
    public void reverse() {
        String datas = "1|255|测试|10.1.8.203";
        String[] data = datas.split("\\|", -1);
        String rule2 = "p1-L1-byte-N1,p2-L2-byte-F,p6-LV-string-F,p9-LIP[p1-ip-F";
        RuleUtil ruleUtil = new RuleUtil();
        List<RuleBean> ruleBeanList = ruleUtil.generateRule(rule2);
        byte[] bytes = ruleUtil.reverse(ruleBeanList, data);
        ByteBuf byteBuf = Unpooled.buffer(bytes.length);
        byteBuf.writeBytes(bytes);
        String ret = ruleUtil.parser(ruleBeanList, byteBuf);
        logger.info(String.format("结果：%s", ret));
    }

    @Test
    public void hexTest() {
        String data = "FE000560ACCA2F0000000200A83A0000";
        logger.info("source: {}", data);
        logger.info("to byte: {}", Arrays.toString(ByteUtil.hexStringToBytes(data)));
        logger.info("byte to str: {}", ByteUtil.bytesToHexStringH(ByteUtil.hexStringToBytes(data)).toUpperCase());
    }

    @Test
    public void httpTest() {
        String rulehttp = "Length-L2-byte-F,LocalProvince-L2-byte-F,LocalCity-L2-byte-F,OwnerProvince-L2-byte-F,OwnerCity-L2-byte-F,RoamingType-L1-byte-F,Interface-L1-byte-F,ProbeID-L2-byte-F,xDRID-L16-hex-F,xDRType-L1-byte-F,RAT-L1-byte-F,IMSI-L8-byte-F,PEI-L8-byte-F,MSISDN-L16-byte-F,MachineIPAddtype-L1-byte-N1,UPFSGWIPAdd-LIP[MachineIPAddtype-ip-F,gNBeNBIPAdd-LIP[MachineIPAddtype-ip-F,UPFSGWPort-L2-byte-F,gNBeNBPort-L2-byte-F,gNBeNBGTPTEID-L4-byte-F,UPFSGWGTPTEID-L4-byte-F,TAC-L4-byte-F,CellID-L5-byte-F,APN-L100-string-F,S_NSSAI_SST-L1-byte-F,S_NSSAI_SD-L4-byte-F,QosFlow15QI-L1-byte-F,QOSflowID-L1-byte-F,NextExtensionHeaderType-L1-byte-F,PGWAdd-LIP[MachineIPAddtype-ip-F,PGWPort-L2-byte-F,ProcedureStartTime-L8-byte-F,ProcedureEndTime-L8-byte-F,Longitude-L8-string-F,latitude-L8-string-F,Height-L2-byte-F,Coordinatesystem-L1-byte-F,ProtocolType-L2-byte-F,AppType-L2-byte-F,AppSubtype-L4-byte-F,AppContent-L1-byte-F,AppStatus-L1-byte-F,IPaddresstype-L1-byte-F,USER_Ipv4-L4-ip-F,USER_Ipv6-L16-ip-F,UserPort-L4-byte-F,L4protocal-L1-byte-F,AppServerIP_Ipv4-L4-ip-F,AppServerIP_Ipv6-L16-ip-F,AppServerPort-L4-byte-F,ULData-L8-byte-N0,DLData-L8-byte-N0,ULIPPacket-L4-byte-N0,DLIPPacket-L4-byte-N0,updura-L8-byte-F,downdura-L8-byte-F,ULDisorderIPPacket-L4-byte-N0,DLDisorderIPPacket-L4-byte-N0,ULRetransIPPacket-L4-byte-N0,DLRetransIPPacket-L4-byte-N0,TCPResponseTime-L4-byte-N0,TCPACKTime-L4-byte-N0,UL_IP_FRAG_PACKETS-L4-byte-N0,DL_IP_FRAG_PACKETS-L4-byte-N0,FirstReqTime-L4-byte-N0,FirstResponseTime-L4-byte-N0,Window-L4-byte-N0,MSS-L4-byte-N0,TCPSYNNum-L1-byte-N0,TCPStatus-L1-byte-F,SessionEnd-L1-byte-F,TCPSYNACKMum-L1-byte-N0,TCPACKNum-L1-byte-N0,TCP12HandshakeStatus-L1-byte-F,TCP23HandshakeStatus-L1-byte-F,ULProbeID-L2-byte-N0,ULLINKIndex-L2-byte-N0,DLProbeID-L2-byte-N0,DLLINKIndex-L2-byte-N0,TransactionID-L4-byte-N0,UL_AVG_RTT-L8-byte-N0,DW_AVG_RTT-L8-byte-N0,ReferXDRID-L48-byte-F,Rule_source-L1-byte-F,VPN-L1-byte-F,HTTPVersion-L1-byte-F,MessageType-L2-byte-F,MessageStatus-L2-byte-F,FirstHTTPResponseTime-L4-byte-F,LastContentPacketTime-L4-byte-F,LastACKTime-L4-byte-F,HOST-LV-string-F,URI-LV-string-F,XOnlineHost-LV-string-F,UserAgent-LV-string-F,HTTP_content_type-LV-string-F,refer_URI-LV-string-F,Cookie-LV-string-F,ContentLength-L4-byte-F,keyword-LV-string-F,ServiceBehaviorFlag-L1-byte-F,ServiceCompFlag-L1-byte-F,ServiceTime-L4-byte-F,IE-L1-byte-N0,Portal-L1-byte-N0,location-LV-string-F,firstrequest-L1-byte-F,Useraccount-L16-byte-F,URItype-L2-byte-F,URIsubtype-L2-byte-F";
        RuleUtil ruleUtil = new RuleUtil();
        List<RuleBean> ruleBeanList = ruleUtil.generateRule(rulehttp);

        TimeCostUtil tc = new TimeCostUtil();
        tc.start();
        for (int i = 0; i < 1; i++) {
            // 逆向封装
            String[] columns = http[i].split("\\|");
            byte[] bytes = ruleUtil.reverse(ruleBeanList, columns);
            logger.info("length: {}", bytes.length);
            ByteBuf byteBuf = Unpooled.buffer(bytes.length);
            byteBuf.writeBytes(bytes);

            // 解析
            String ret = ruleUtil.parser(ruleBeanList, byteBuf);
            logger.info("ret: {}", ret);
        }
        logger.info("tc: {}", tc.stopAndGet());
    }

    @Test
    public void notifyXDRData() {
        // XDR数据传输
        String rule_data = "p1-L1-byte-N1,p2-L2-byte-F,p6-LV-string-F,p9-LIP[p1-ip-F";
        MessageUtil<SDTPnotifyXDRData_Req> notifyXDRData = new MessageUtil<>(SDTPnotifyXDRData_Req.class, rule_data);
        notifyXDRData.append("1|255|测试1|10.1.8.203");
        notifyXDRData.append("1|255|测试2|10.1.8.204");
        notifyXDRData.append("1|255|测试3|10.1.8.205");
        readHeader(notifyXDRData.getMessage());

        // XDR数据通知应答
        MessageUtil<SDTPnotifyXDRData_Resp> notifyXDRData_Resp = new MessageUtil<>(SDTPnotifyXDRData_Resp.class);
        notifyXDRData_Resp.append(new byte[]{0x01});
        readHeader(notifyXDRData_Resp.getMessage());
    }

    @Test
    public void linkRel() {
        // linkRel_Req：连接释放请求
        MessageUtil<SDTPlinkRel_Req> linkRel_Req = new MessageUtil<>(SDTPlinkRel_Req.class);
        linkRel_Req.append(new byte[]{0x01});
        readHeader(linkRel_Req.getMessage());

        // linkRel_Resp：连接释放应答
        MessageUtil<SDTPlinkRel_Resp> linkRel_Resp = new MessageUtil<>(SDTPlinkRel_Resp.class);
        linkRel_Resp.clean();
        linkRel_Resp.append(new byte[]{0x01});
        readHeader(linkRel_Resp.getResponse());

        linkRel_Resp.clean();
        linkRel_Resp.append(new byte[]{0x02});
        readHeader(linkRel_Resp.getResponse());
    }

    @Test
    public void messageType() {
        EnumMessageType messageType = EnumMessageType.ValueOf(5);
        logger.info("messageType: {}", messageType);
    }

    private void readHeader(byte[] bytes) {
        ByteBuf buf = Unpooled.buffer(bytes.length);
        buf.writeBytes(bytes);
        readHeader(buf);
    }

    private void readHeader(ByteBuf buf) {
        logger.info("readableBytes: {}", buf.readableBytes());

        // [Message Header]
        // sdtp数据帧长度
        long msgLength = buf.readUnsignedInt();
        // 消息类型
        int msgType = buf.readUnsignedShort();
        // sdtp包头中的交互的流水号
        long sequenceId = buf.readUnsignedInt();
        // sdtp包头中的事件数量
        int totalContents = buf.readUnsignedShort();
        logger.info("msgLength: {}, msgType: {}, sequenceId: {}, totalContents: {}"
                , msgLength, msgType, sequenceId, totalContents);

        buf.release();
    }
}