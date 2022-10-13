package com.cqx.netty.upload.client;

import com.alibaba.fastjson.JSON;
import com.cqx.netty.util.SocketClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;

/**
 * ReUploadExecutor
 *
 * @author chenqixu
 */
public class ReUploadExecutor implements Callable<ReportRespBean> {
    private static final Logger log = LoggerFactory.getLogger(ReUploadExecutor.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private String server_ip;
    private int server_port;
    private ReportReqBean reqBean;

    public ReUploadExecutor(String ip, int port, ReportReqBean reqBean) {
        this.server_ip = ip;
        this.server_port = port;
        this.reqBean = reqBean;
    }

    @Override
    public ReportRespBean call() throws Exception {
        ReportRespBean resp = new ReportRespBean();
        String reqJson = JSON.toJSONString(reqBean);
        log.info("【" + server_ip + ":" + server_port + "】客户端请求参数：" + reqJson);
        String respJson = null;
        try {
//            respJson = socketClinet(reqJson);
            nettyClient(reqJson);
        } catch (Exception e) {
            log.error("【" + server_ip + ":" + server_port + "】客户端连接发生异常！" + e);
            resp.setFailReason(e.getMessage());
            return resp;
        }
        if (StringUtils.isNotBlank(respJson)) {
            byte[] bs = respJson.getBytes();
            respJson = new String(bs, StandardCharsets.UTF_8);
            log.info("【" + server_ip + ":" + server_port + "】-----返回结果：" + respJson);
            resp = JSON.parseObject(respJson, ReportRespBean.class);
            return resp;
        }
        return null;
    }

    private String nettyClient(String reqJson) throws Exception {
        ReUploadClient client = new ReUploadClient(server_ip, server_port);
        return client.run(reqJson);
    }

    private String socketClinet(String reqJson) throws IOException {
        byte[] rets;
        try (SocketClient socketClient = SocketClient.newbuilder()
                .setIp(server_ip)
                .setPort(server_port)
                .build()) {
            ClientReceive clientReceive = new ClientReceive();
            socketClient.send(reqJson.getBytes());
            rets = socketClient.receive(clientReceive);
        }
        return new String(rets);
    }

    class ClientReceive implements SocketClient.ReceiveCall {

        @Override
        public byte[] read(InputStream in) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String tmp = br.readLine();
            if (tmp == null) {
                log.warn("读取到空值！");
                return "".getBytes();
            }
            return tmp.getBytes();
        }
    }
}
