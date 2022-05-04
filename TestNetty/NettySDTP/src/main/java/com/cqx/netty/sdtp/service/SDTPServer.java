package com.cqx.netty.sdtp.service;

import com.codahale.metrics.Meter;
import com.cqx.common.metric.MetricUtils;
import com.cqx.common.utils.file.FileUtil;
import com.cqx.common.utils.system.SleepUtil;
import com.cqx.common.utils.thread.ThreadTool;
import com.cqx.netty.sdtp.bean.EnumMessageType;
import com.cqx.netty.sdtp.bean.SDTPXDRRawDataSend_Resp;
import com.cqx.netty.sdtp.bean.SDTPlinkRel_Resp;
import com.cqx.netty.sdtp.bean.SDTPnotifyXDRData_Resp;
import com.cqx.netty.sdtp.rule.RuleBean;
import com.cqx.netty.sdtp.rule.RuleUtil;
import com.cqx.netty.sdtp.util.MessageUtil;
import com.cqx.netty.util.IServer;
import com.cqx.netty.util.IServerHandler;
import com.cqx.netty.util.NetConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SDTPServer
 *
 * @author chenqixu
 */
public class SDTPServer {
    private Meter before = MetricUtils.getMeter("before");
    private Meter after = MetricUtils.getMeter("after");
    private Meter dealCnt = MetricUtils.getMeter("dealCnt");
    private String filePath;
    private int port;
    private boolean isWrite = false;
    private boolean isParser = false;
    private int parallel_num = 1;
    private Map<String, String> params = new HashMap<>();
    private AtomicInteger fileNum = new AtomicInteger(1);

    public SDTPServer() {
        MetricUtils.build(15L, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("参数个数不足！需要两个参数");
            System.exit(-1);
        }
        String filePath = args[0];
        int port = Integer.valueOf(args[1]);
        String work_num = "1";
        if (args.length >= 3) {
            work_num = args[2];
        }
        boolean isWrite = false;
        if (args.length >= 4) {
            isWrite = Boolean.valueOf(args[3]);
        }
        boolean isParser = false;
        if (args.length >= 5) {
            isParser = Boolean.valueOf(args[4]);
        }
        int parallel_num = 1;
        if (args.length >= 6) {
            parallel_num = Integer.valueOf(args[5]);
        }

        SDTPServer sdtpServer = new SDTPServer();
        sdtpServer.setFilePath(filePath);
        sdtpServer.setPort(port);
        sdtpServer.addParam(NetConstant.workerGroup_nThreads, work_num);
        sdtpServer.setWrite(isWrite);
        sdtpServer.setParser(isParser);
        sdtpServer.setParallel_num(parallel_num);
        sdtpServer.startServer();
    }

    public void addParam(String key, String value) {
        params.put(key, value);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private void startServer() throws Exception {
//        final String xdrRule = "Length-L2-byte-F,p1-L1-byte-N1,p2-L2-byte-F,p6-LV-string-F,p9-LIP[p1-ip-F";
        final String xdrRule = "Length-L2-byte-F,LocalProvince-L2-byte-F,LocalCity-L2-byte-F,OwnerProvince-L2-byte-F,OwnerCity-L2-byte-F,RoamingType-L1-byte-F,Interface-L1-byte-F,ProbeID-L2-byte-F,xDRID-L16-hex-F,xDRType-L1-byte-F,RAT-L1-byte-F,IMSI-L8-byte-F,PEI-L8-byte-F,MSISDN-L16-byte-F,MachineIPAddtype-L1-byte-N1,UPFSGWIPAdd-LIP[MachineIPAddtype-ip-F,gNBeNBIPAdd-LIP[MachineIPAddtype-ip-F,UPFSGWPort-L2-byte-F,gNBeNBPort-L2-byte-F,gNBeNBGTPTEID-L4-byte-F,UPFSGWGTPTEID-L4-byte-F,TAC-L4-byte-F,CellID-L5-byte-F,APN-L100-string-F,S_NSSAI_SST-L1-byte-F,S_NSSAI_SD-L4-byte-F,QosFlow15QI-L1-byte-F,QOSflowID-L1-byte-F,NextExtensionHeaderType-L1-byte-F,PGWAdd-LIP[MachineIPAddtype-ip-F,PGWPort-L2-byte-F,ProcedureStartTime-L8-byte-F,ProcedureEndTime-L8-byte-F,Longitude-L8-string-F,latitude-L8-string-F,Height-L2-byte-F,Coordinatesystem-L1-byte-F,ProtocolType-L2-byte-F,AppType-L2-byte-F,AppSubtype-L4-byte-F,AppContent-L1-byte-F,AppStatus-L1-byte-F,IPaddresstype-L1-byte-F,USER_Ipv4-L4-ip-F,USER_Ipv6-L16-ip-F,UserPort-L4-byte-F,L4protocal-L1-byte-F,AppServerIP_Ipv4-L4-ip-F,AppServerIP_Ipv6-L16-ip-F,AppServerPort-L4-byte-F,ULData-L8-byte-N0,DLData-L8-byte-N0,ULIPPacket-L4-byte-N0,DLIPPacket-L4-byte-N0,updura-L8-byte-F,downdura-L8-byte-F,ULDisorderIPPacket-L4-byte-N0,DLDisorderIPPacket-L4-byte-N0,ULRetransIPPacket-L4-byte-N0,DLRetransIPPacket-L4-byte-N0,TCPResponseTime-L4-byte-N0,TCPACKTime-L4-byte-N0,UL_IP_FRAG_PACKETS-L4-byte-N0,DL_IP_FRAG_PACKETS-L4-byte-N0,FirstReqTime-L4-byte-N0,FirstResponseTime-L4-byte-N0,Window-L4-byte-N0,MSS-L4-byte-N0,TCPSYNNum-L1-byte-N0,TCPStatus-L1-byte-F,SessionEnd-L1-byte-F,TCPSYNACKMum-L1-byte-N0,TCPACKNum-L1-byte-N0,TCP12HandshakeStatus-L1-byte-F,TCP23HandshakeStatus-L1-byte-F,ULProbeID-L2-byte-N0,ULLINKIndex-L2-byte-N0,DLProbeID-L2-byte-N0,DLLINKIndex-L2-byte-N0,TransactionID-L4-byte-N0,UL_AVG_RTT-L8-byte-N0,DW_AVG_RTT-L8-byte-N0,ReferXDRID-L48-byte-F,Rule_source-L1-byte-F,VPN-L1-byte-F,HTTPVersion-L1-byte-F,MessageType-L2-byte-F,MessageStatus-L2-byte-F,FirstHTTPResponseTime-L4-byte-F,LastContentPacketTime-L4-byte-F,LastACKTime-L4-byte-F,HOST-LV-string-F,URI-LV-string-F,XOnlineHost-LV-string-F,UserAgent-LV-string-F,HTTP_content_type-LV-string-F,refer_URI-LV-string-F,Cookie-LV-string-F,ContentLength-L4-byte-F,keyword-LV-string-F,ServiceBehaviorFlag-L1-byte-F,ServiceCompFlag-L1-byte-F,ServiceTime-L4-byte-F,IE-L1-byte-N0,Portal-L1-byte-N0,location-LV-string-F,firstrequest-L1-byte-F,Useraccount-L16-byte-F,URItype-L2-byte-F,URIsubtype-L2-byte-F";
        final String noLengthXdrRule = "LocalProvince-L2-byte-F,LocalCity-L2-byte-F,OwnerProvince-L2-byte-F,OwnerCity-L2-byte-F,RoamingType-L1-byte-F,Interface-L1-byte-F,ProbeID-L2-byte-F,xDRID-L16-hex-F,xDRType-L1-byte-F,RAT-L1-byte-F,IMSI-L8-byte-F,PEI-L8-byte-F,MSISDN-L16-byte-F,MachineIPAddtype-L1-byte-N1,UPFSGWIPAdd-LIP[MachineIPAddtype-ip-F,gNBeNBIPAdd-LIP[MachineIPAddtype-ip-F,UPFSGWPort-L2-byte-F,gNBeNBPort-L2-byte-F,gNBeNBGTPTEID-L4-byte-F,UPFSGWGTPTEID-L4-byte-F,TAC-L4-byte-F,CellID-L5-byte-F,APN-L100-string-F,S_NSSAI_SST-L1-byte-F,S_NSSAI_SD-L4-byte-F,QosFlow15QI-L1-byte-F,QOSflowID-L1-byte-F,NextExtensionHeaderType-L1-byte-F,PGWAdd-LIP[MachineIPAddtype-ip-F,PGWPort-L2-byte-F,ProcedureStartTime-L8-byte-F,ProcedureEndTime-L8-byte-F,Longitude-L8-string-F,latitude-L8-string-F,Height-L2-byte-F,Coordinatesystem-L1-byte-F,ProtocolType-L2-byte-F,AppType-L2-byte-F,AppSubtype-L4-byte-F,AppContent-L1-byte-F,AppStatus-L1-byte-F,IPaddresstype-L1-byte-F,USER_Ipv4-L4-ip-F,USER_Ipv6-L16-ip-F,UserPort-L4-byte-F,L4protocal-L1-byte-F,AppServerIP_Ipv4-L4-ip-F,AppServerIP_Ipv6-L16-ip-F,AppServerPort-L4-byte-F,ULData-L8-byte-N0,DLData-L8-byte-N0,ULIPPacket-L4-byte-N0,DLIPPacket-L4-byte-N0,updura-L8-byte-F,downdura-L8-byte-F,ULDisorderIPPacket-L4-byte-N0,DLDisorderIPPacket-L4-byte-N0,ULRetransIPPacket-L4-byte-N0,DLRetransIPPacket-L4-byte-N0,TCPResponseTime-L4-byte-N0,TCPACKTime-L4-byte-N0,UL_IP_FRAG_PACKETS-L4-byte-N0,DL_IP_FRAG_PACKETS-L4-byte-N0,FirstReqTime-L4-byte-N0,FirstResponseTime-L4-byte-N0,Window-L4-byte-N0,MSS-L4-byte-N0,TCPSYNNum-L1-byte-N0,TCPStatus-L1-byte-F,SessionEnd-L1-byte-F,TCPSYNACKMum-L1-byte-N0,TCPACKNum-L1-byte-N0,TCP12HandshakeStatus-L1-byte-F,TCP23HandshakeStatus-L1-byte-F,ULProbeID-L2-byte-N0,ULLINKIndex-L2-byte-N0,DLProbeID-L2-byte-N0,DLLINKIndex-L2-byte-N0,TransactionID-L4-byte-N0,UL_AVG_RTT-L8-byte-N0,DW_AVG_RTT-L8-byte-N0,ReferXDRID-L48-byte-F,Rule_source-L1-byte-F,VPN-L1-byte-F,HTTPVersion-L1-byte-F,MessageType-L2-byte-F,MessageStatus-L2-byte-F,FirstHTTPResponseTime-L4-byte-F,LastContentPacketTime-L4-byte-F,LastACKTime-L4-byte-F,HOST-LV-string-F,URI-LV-string-F,XOnlineHost-LV-string-F,UserAgent-LV-string-F,HTTP_content_type-LV-string-F,refer_URI-LV-string-F,Cookie-LV-string-F,ContentLength-L4-byte-F,keyword-LV-string-F,ServiceBehaviorFlag-L1-byte-F,ServiceCompFlag-L1-byte-F,ServiceTime-L4-byte-F,IE-L1-byte-N0,Portal-L1-byte-N0,location-LV-string-F,firstrequest-L1-byte-F,Useraccount-L16-byte-F,URItype-L2-byte-F,URIsubtype-L2-byte-F";
        IServer iServer = IServer.newbuilder();
        iServer.setPort(port)
                .setParams(params)
                .buildBootstrap()
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, -4, 0));
                        p.addLast("sdtpServer", new SDTPServerHandler(xdrRule, noLengthXdrRule));
                    }
                });
        iServer.start();
    }

    public void setWrite(boolean write) {
        isWrite = write;
    }

    public void setParser(boolean parser) {
        isParser = parser;
    }

    public void setParallel_num(int parallel_num) {
        this.parallel_num = parallel_num;
    }

    class SDTPServerHandler extends IServerHandler {
        RuleUtil xdrRuleUtil;
        List<RuleBean> xdrRuleBeanList;
        RuleUtil noLengthXdrRuleUtil;
        List<RuleBean> noLengthXdrRuleBeanList;
        FileUtil fileUtil;
        long count = 0L;
        LinkedBlockingQueue<byte[]> queue;
        volatile boolean parallel_close = false;

        // linkRel_Resp：连接释放应答
        MessageUtil<SDTPlinkRel_Resp> linkRel_Resp = new MessageUtil<>(SDTPlinkRel_Resp.class);
        // XDR数据通知应答
        MessageUtil<SDTPnotifyXDRData_Resp> notifyXDRData_Resp = new MessageUtil<>(SDTPnotifyXDRData_Resp.class);
        // XDR原始码流通知应答
        MessageUtil<SDTPXDRRawDataSend_Resp> XDRRawDataSend_Resp = new MessageUtil<>(SDTPXDRRawDataSend_Resp.class);

        SDTPServerHandler(String xdrRule, String noLengthXdrRule) {
            if (parallel_num == 1) {
                this.fileUtil = new FileUtil();
                try {
                    this.fileUtil.createFile(filePath + fileNum.getAndIncrement(), "UTF-8");
                } catch (FileNotFoundException | UnsupportedEncodingException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            this.xdrRuleUtil = new RuleUtil();
            this.xdrRuleBeanList = xdrRuleUtil.generateRule(xdrRule);
            this.noLengthXdrRuleUtil = new RuleUtil();
            this.noLengthXdrRuleBeanList = noLengthXdrRuleUtil.generateRule(noLengthXdrRule);
            this.linkRel_Resp.append(new byte[]{0x01});
            this.notifyXDRData_Resp.append(new byte[]{0x01});
            this.XDRRawDataSend_Resp.append(new byte[]{0x01});
            this.queue = new LinkedBlockingQueue<>(100000);
            if (parallel_num > 1) {
                ThreadTool threadTool = new ThreadTool(parallel_num);
                for (int i = 0; i < parallel_num; i++) {
                    threadTool.addTask(new Runnable() {
                        @Override
                        public void run() {
                            FileUtil fileUtil = new FileUtil();
                            try {
                                fileUtil.createFile(filePath + fileNum.getAndIncrement(), "UTF-8");
                            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                                logger.error(e.getMessage(), e);
                            }
                            while (true) {
                                byte[] datas;
                                while ((datas = queue.poll()) != null) {
                                    // 解析数据
                                    ByteBuf byteBuf = Unpooled.buffer(datas.length);
                                    byteBuf.writeBytes(datas);
                                    String ret = noLengthXdrRuleUtil.parser(noLengthXdrRuleBeanList, byteBuf);
                                    logger.debug("ret: {}", ret);
                                    after.mark(ret.getBytes().length);
                                    if (isWrite) {
                                        fileUtil.write(ret);
                                        fileUtil.newline();
                                    }
                                }
                                if (parallel_close) {
                                    break;
                                }
                                SleepUtil.sleepMilliSecond(1);
                            }
                            fileUtil.closeWrite();
                        }
                    });
                }
                threadTool.startTaskNotWait();
            }
        }

        @Override
        protected ByteBuf dealHandler(ByteBuf buf) {
            before.mark(buf.readableBytes());
            // [Message Header]
            // sdtp数据帧长度
            long msgLength = buf.readUnsignedInt();
            // 消息类型
            int msgType = buf.readUnsignedShort();
            // sdtp包头中的交互的流水号
            long sequenceId = buf.readUnsignedInt();
            // sdtp包头中的事件数量
            int totalContents = buf.readUnsignedShort();
            dealCnt.mark(totalContents);
            // sdtp包体长度，减去包头固定长度12即可
            int bodyLength = Integer.valueOf((msgLength - 12) + "");
            EnumMessageType messageType = EnumMessageType.ValueOf(msgType);
            logger.debug("msgLength: {}, msgType: {}, sequenceId: {}, totalContents: {}, messageType: {}, bodyLength: {}"
                    , msgLength, msgType, sequenceId, totalContents, messageType, bodyLength);

            if (messageType == null) {
                messageType = EnumMessageType.linkRel_Req;
            }
            switch (messageType) {
                case notifyXDRData_Req:
                    try {
                        for (int i = 0; i < totalContents; i++) {
                            // 读1个字节
                            int xdrType = buf.readByte();
                            String ret;
                            if (isParser) {
                                if (parallel_num > 1) {
                                    // 读2 byte，Length	V	unsigned int	2	全F	指示整个XDR所占用字节数
                                    int xdrLength = buf.readUnsignedShort();
                                    // 读取xdr的内容，按刚才的字节读取内容byte[]
                                    byte[] bytesXdrData = new byte[xdrLength];
                                    buf.readBytes(bytesXdrData);
                                    // 写入队列
                                    // 在该队列的尾部插入指定的元素，如有必要，等待空间变为可用
                                    queue.put(bytesXdrData);
                                } else {
                                    // 解析数据
                                    ret = xdrRuleUtil.parser(xdrRuleBeanList, buf);
                                    logger.debug("xdrType: {}, ret: {}", xdrType, ret);
                                    after.mark(ret.getBytes().length);
                                    if (isWrite) {
                                        fileUtil.write(ret);
                                        fileUtil.newline();
                                    }
                                }
                            } else {
                                if (isWrite) {
                                    // 读2 byte，Length	V	unsigned int	2	全F	指示整个XDR所占用字节数
                                    int xdrLength = buf.readUnsignedShort();
                                    // 读取xdr的内容，按刚才的字节读取内容byte[]
                                    byte[] bytesXdrData = new byte[xdrLength];
                                    buf.readBytes(bytesXdrData);
                                    after.mark(bytesXdrData.length);
                                    fileUtil.write(new String(bytesXdrData, StandardCharsets.ISO_8859_1));
                                    fileUtil.newline();
                                }
                            }
                            count++;
                            if (count % 20000 == 0) {
                                logger.info("count: {}", count);
                            }
                        }
                    } catch (Exception e) {
                        notifyXDRData_Resp.clean();
                        notifyXDRData_Resp.append(new byte[]{0x02});
                        logger.error(e.getMessage(), e);
                    }
                    // 应答
                    return notifyXDRData_Resp.getResponse();
                case XDRRawDataSend_Req:
                    // 读取数据并保存到本地
                    byte[] dst = new byte[bodyLength];
                    buf.readBytes(dst);
                    try (FileOutputStream xdrRaw = new FileOutputStream("d:\\tmp\\data\\xdr\\20220429.cap")) {
                        xdrRaw.write(dst);
                    } catch (IOException e) {
                        XDRRawDataSend_Resp.clean();
                        XDRRawDataSend_Resp.append(new byte[]{0x02});
                        logger.error(e.getMessage(), e);
                    }
                    return XDRRawDataSend_Resp.getResponse();
                case linkRel_Req:
                default:
                    if (fileUtil != null) fileUtil.closeWrite();
                    if (parallel_num > 1) parallel_close = true;
                    // 关闭客户端连接
                    closeClient();
                    // 应答
                    return linkRel_Resp.getResponse();
            }
        }
    }
}
