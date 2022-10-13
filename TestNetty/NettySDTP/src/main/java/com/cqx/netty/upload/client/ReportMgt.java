package com.cqx.netty.upload.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * ReportMgt
 *
 * @author chenqixu
 */
public class ReportMgt {
    private static final Logger log = LoggerFactory.getLogger(ReUploadExecutor.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ReportReqBean reqBean = new ReportReqBean();
        reqBean.setId("1");
        reqBean.setType("test");
        reqBean.setBeginFilename("aaa_1_2_3_4_5_6_7.txt");
        reqBean.setEndFilename("aaa_1_2_3_4_5_6_7.txt");
        ReportMgt reportMgt = new ReportMgt();
        for (int i = 0; i < 5; i++) {
            reportMgt.doReport("127.0.0.1:8989,127.0.0.1:8988", reqBean);
        }
    }

    /**
     * 重报
     *
     * @param ip_ports
     * @param reqBean
     */
    private ReportRespBean doReport(String ip_ports, ReportReqBean reqBean) throws InterruptedException, ExecutionException {
        ReportRespBean res = new ReportRespBean();
        //根据文件名解析获取 文件类型  开始时间 开始文件编号 结束时间 结束文件编号
        String beginFilename = reqBean.getBeginFilename();
        String endFilename = reqBean.getEndFilename();
        String type = beginFilename.split("_")[2];

        String[] ipList = ip_ports.split(",");
        //
        if (ipList != null && ipList.length > 0) {
            Map<String, Future<ReportRespBean>> futureMap = new HashMap<>();
            ExecutorService service = Executors.newFixedThreadPool(ipList.length);
            for (String ip_port : ipList) {
                String[] split = ip_port.split(":");
                ReUploadExecutor executor = new ReUploadExecutor(split[0], Integer.parseInt(split[1]), reqBean);
                Future<ReportRespBean> future = service.submit(executor);
                futureMap.put(split[0], future);
            }
            try {
                service.shutdown();
                if (service.awaitTermination(30, TimeUnit.SECONDS)) {    //等待任务执行完
                    int successNum = 0;
                    int failNum = 0;
                    StringBuilder successFileInfo = new StringBuilder();
                    StringBuilder failFileInfo = new StringBuilder();
                    for (Map.Entry<String, Future<ReportRespBean>> entry : futureMap.entrySet()) {
                        String serverIp = entry.getKey();
                        //返回结果值
                        Future<ReportRespBean> future = entry.getValue();
                        //将文件列表列出来
                        ReportRespBean reportRespBean = future.get();
                        if (reportRespBean.isSuccess()) {
                            successFileInfo.append("服务器|" + serverIp + "|成功上报文件数|" + reportRespBean.getUploadNum() + "上报文件名列表|" + reportRespBean.getUploadFileNames())
                                    .append(System.getProperty("line.separator"));
                            successNum++;
                        } else {
                            failNum++;
                            failFileInfo.append("服务器|" + serverIp + "|上报失败原因|" + reportRespBean.getFailReason())
                                    .append(System.getProperty("line.separator"));
                        }
                    }
                    log.info("重报结果记录|id|" + reqBean.getId() + "|上报成功服务器数|" + successNum + "|上报失败服务器数量|" + failNum + System.getProperty("line.separator") +
                            "上报成功信息|" + successFileInfo.toString() + "|" +
                            "上报失败信息|" + failFileInfo + "|" + System.getProperty("line.separator")
                    );
                    if (ipList.length == successNum) {
                        res.success(reqBean.getId());
                    } else {
                        res.fail(reqBean.getId(), "上报失败！");
                    }
                } else {
                    StringBuilder successFileInfo = new StringBuilder();
                    StringBuilder failFileInfo = new StringBuilder();
                    int successNum = 0;
                    int failNum = 0;
                    for (Map.Entry<String, Future<ReportRespBean>> entry : futureMap.entrySet()) {
                        String serverIp = entry.getKey();
                        //返回结果值
                        Future<ReportRespBean> future = entry.getValue();
                        //将文件列表列出来
                        ReportRespBean reportRespBean = null;
                        try {
                            //防止客户端执行异常，没返回导致卡住
                            reportRespBean = future.get(3, TimeUnit.SECONDS);
                            if (reportRespBean.isSuccess()) {
                                successFileInfo.append("服务器|" + serverIp + "|成功上报文件数|" + reportRespBean.getUploadNum() + "上报文件名列表|" + reportRespBean.getUploadFileNames())
                                        .append(System.getProperty("line.separator"));
                                successNum++;
                            } else {
                                failNum++;
                                failFileInfo.append("服务器|" + serverIp + "|上报失败原因|" + reportRespBean.getFailReason())
                                        .append(System.getProperty("line.separator"));
                            }
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                            failNum++;
                            failFileInfo.append("服务器|" + serverIp + "|上报失败原因|" + "获取结果超时")
                                    .append(System.getProperty("line.separator"));
                        }
                    }
                    res.fail(reqBean.getId(), "执行上报超时异常！");
                }
            } catch (Exception e) {
                log.error("上报异常", e);
            } finally {
                if (service != null && !service.isShutdown()) {
                    service.shutdownNow();
                }
            }

        } else {
            log.error("找不到上报主机信息，请检查配置！！！type|" + type + ".jstorm.ip_ports" + "|获取的ip配置信息|" + ipList);
            res.fail(reqBean.getId(), "执行上报异常！");
        }
        return res;
    }
}
