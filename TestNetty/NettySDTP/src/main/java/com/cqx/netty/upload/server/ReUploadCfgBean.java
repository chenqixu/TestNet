package com.cqx.netty.upload.server;

import java.util.HashMap;
import java.util.Map;

/**
 * ReUploadCfgBean
 *
 * @author chenqixu
 */
public class ReUploadCfgBean {
    public static final String SOURCE_TYPE_NO_CYCLE="0";
    public static final String SOURCE_TYPE_DAY="1";
    public static final String SOURCE_TYPE_HOUR="2";
    private final String IP_PORTS="ip_ports";
    private final String UPLOAD_DIR="upload_dir";
    private final String SOURCE_DIR="source_dir";
    private final String MSG_DIR="msg_dir";

    private final String SOURCE_DIR_TYPE="source_dir_type";
    //JSTORM集群机器的ip和对应的端口，允许各主机的端口不一样
    private String ipPorts;
    //文件上报目录
    private String uploadDir;
    //文件上报目录
    private String sourceDir;
    //源文件存放的样式 0:无动态目录 1：YYYYMMDD(天目录)  2:YYYYMMDDHH(小时目录)
    private String sourceDirType;
    //<ip,端口>
    private Map<String,String> servers = new HashMap<>();


    //文件上报目录
    private String msgDir;



    public void parserMap(Map<String, ?> param) {
        sourceDir = (String) param.get(SOURCE_DIR);
        sourceDirType= (String) param.get(SOURCE_DIR_TYPE);
        uploadDir = (String) param.get(UPLOAD_DIR);
        ipPorts = (String) param.get(IP_PORTS);
        msgDir=(String) param.get(MSG_DIR);
        String[] ipPortList = ipPorts.split(",");
        if(ipPortList.length<1){
            throw new RuntimeException("参数解析异常：jstorm-服务器信息不能为空！");
        }
        for(String ipPorts : ipPortList){
            String[] server = ipPorts.split(":");
            servers.put(server[0],server[1]);
        }

        //sourceDir  ${YYYYMMDD} ${YYYYMMDDHH}
       /* if(sourceDir.contains("${YYYYMMDDHH}")){
            sourceDirType =SOURCE_TYPE_HOUR;
        }else if(sourceDir.contains("${YYYYMMDD}")){
            sourceDirType =SOURCE_TYPE_DAY;
        }else{
            sourceDirType =SOURCE_TYPE_NO_CYCLE;
        }*/
    }

    public String getIpPorts() {
        return ipPorts;
    }

    public void setIpPorts(String ipPorts) {
        this.ipPorts = ipPorts;
    }

    public Map<String, String> getServers() {
        return servers;
    }

    public void setServers(Map<String, String> servers) {
        this.servers = servers;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    public String getSourceDirType() {
        return sourceDirType;
    }

    public void setSourceDirType(String sourceDirType) {
        this.sourceDirType = sourceDirType;
    }

    public String getMsgDir() {
        return msgDir;
    }

    public void setMsgDir(String msgDir) {
        this.msgDir = msgDir;
    }

    @Override
    public String toString() {
        return "ReUploadCfgBean{" +
                " ipPorts='" + ipPorts + '\'' +
                ", uploadDir='" + uploadDir + '\'' +
                ", sourceDir='" + sourceDir + '\'' +
                ", sourceDirType='" + sourceDirType + '\'' +
                ", servers=" + servers +
                ", msgDir='" + msgDir + '\'' +
                '}';
    }
}
