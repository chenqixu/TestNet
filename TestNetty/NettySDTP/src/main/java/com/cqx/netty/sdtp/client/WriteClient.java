package com.cqx.netty.sdtp.client;

import com.codahale.metrics.Meter;
import com.cqx.common.metric.MetricUtils;
import com.cqx.common.utils.file.FileUtil;
import com.cqx.common.utils.thread.ThreadTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

/**
 * WriteClient
 *
 * @author chenqixu
 */
public class WriteClient {
    private static final Logger logger = LoggerFactory.getLogger(WriteClient.class);
    private final FileUtil fileUtil;
    private String httpData = "||||255|8|6149|FE000560ACCA2F0000000200A83A0000|103|10||||2|2409:8034:4040:1300:0:0:0:102|2409:8034:4021:0:0:0:140E:701|2152|2152|1205880630|68217329|4294967295|1099511627775||255|4294967295|255|1|133|FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF|65535|1620581400956000|1620581401188000|0.000000|0.000000|65535|255|1|5|17|3|0|1|255.255.255.255|2409:8934:282:2946:B10D:86BB:5B2B:FB81|55492|0|255.255.255.255|2409:8C54:881:129:0:FF:B026:25AF|80|1331|1323|6|6|225000|211000|0|0|0|1|21000|14000|0|0|5000|27000|243|1312|1|0|1|1|1|0|0|5|0|5|0|1|21|0||1|0|3|6|302|27000|27000|130000|baiducdncmn2.inter.iqiyi.com|http://baiducdncmn2.inter.iqiyi.com/videos/vts/20210130/d0/31/af1a11ad7d34e21070bc89c066986a3c.ts?key=0f7daec203cd0441c4fbeaadc38daacc0&dis_k=44c37d8a19ea8673faaa2096eaee2bfb&dis_t=1612025412&dis_dz=CMNET-FuJian_FuZhou&dis_st=49&src=iqiyi.com&dis_hit=0&dis_tag=02000000&uuid=df683305-60158e44-250&hotlevel=4&sgti=&qd_uid=0&qd_tm=1612025412720&sd=0&start=0&ve=&end=142128&qd_ip=df683305&dfp=&qd_tvid=2984558307645000&qypid=2984558307645000_04000000001000000000_75&qd_p=df683305&qd_k=916afb7be3624ecc3f522f02737a9ee0&qd_src=02029022240000000000&qd_vip=0&contentlength=142128&z=baiducdn2_cmnet||HUAWEI-LIO-AN00__weibo__11.1.3__android__android10_unknown Lavf/57.41.100|text/html|||0||3|0|232000|0|0|http://[2409:8c34:0c00:0006::112.49.48.41]/r/baiducdncmn2.inter.iqiyi.com/videos/vts/20210130/d0/31/af1a11ad7d34e21070bc89c0669|1||65535|65535";
    private int sourceCnt;
    private int writeCnt;
    private int flushCnt;
    private Meter write;
    private int size;

    public WriteClient(String filePath, int sourceCnt, int writeCnt, int flushCnt) {
        this.write = MetricUtils.getMeter("write");
        this.size = httpData.getBytes().length;
        this.sourceCnt = sourceCnt;
        this.writeCnt = writeCnt;
        this.flushCnt = flushCnt;
        this.fileUtil = new FileUtil();
        try {
            this.fileUtil.createFile(filePath, "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            logger.error("参数个数至少需要4个才可以！");
            System.exit(1);
        }
        String filePath = args[0];
        int sourceCnt = Integer.valueOf(args[1]);
        logger.info("上游的流：{} 个", sourceCnt);
        int writeCnt = Integer.valueOf(args[2]);
        logger.info("写入的次数：{}", writeCnt);
        int flushCnt = Integer.valueOf(args[3]);
        logger.info("每隔 {} 条进行刷新", flushCnt);
        MetricUtils.getMeter("write");
        MetricUtils.build(5, TimeUnit.SECONDS);
        WriteClient writeClient = new WriteClient(filePath, sourceCnt, writeCnt, flushCnt);
        writeClient.write();
    }

    private void write() {
        ThreadTool threadTool = new ThreadTool(sourceCnt);
        for (int i = 0; i < sourceCnt; i++) {
            threadTool.addTask(new Runnable() {
                @Override
                public void run() {
                    int cnt = 0;
                    while (cnt < writeCnt) {
                        cnt++;
                        synchronized (fileUtil) {
                            fileUtil.write(httpData);
                            fileUtil.newline();
                            write.mark(size);
                        }
                        if (flushCnt > 0 && cnt % flushCnt == 0) {
                            logger.info("cnt: {}, do flush.", cnt);
                            fileUtil.flush();
                        }
                        if (flushCnt == 0 && cnt % 200000 == 0) {
                            logger.info("cnt: {}", cnt);
                        }
                    }
                }
            });
        }
        threadTool.startTask();
        fileUtil.closeWrite();
    }
}
