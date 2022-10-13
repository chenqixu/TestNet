package com.cqx.netty.upload.server;

/**
 * ReportGroupReqBean
 *
 * @author chenqixu
 */
public class ReportGroupReqBean {
    private String id;
    private String type;

    private String endTime;
    private String begainTime;
    private String fileName;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getBegainTime() {
        return begainTime;
    }

    public void setBegainTime(String begainTime) {
        this.begainTime = begainTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "ReportGroupReqBean{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", endTime='" + endTime + '\'' +
                ", begainTime='" + begainTime + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
