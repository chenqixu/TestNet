package com.cqx.netty.upload.client;

/**
 * ReportReqBean
 *
 * @author chenqixu
 */
public class ReportReqBean {
    private String id;
    private String type;

    private String beginFilename;
    private String endFilename;


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

    public String getBeginFilename() {
        return beginFilename;
    }

    public void setBeginFilename(String beginFilename) {
        this.beginFilename = beginFilename;
    }

    public String getEndFilename() {
        return endFilename;
    }

    public void setEndFilename(String endFilename) {
        this.endFilename = endFilename;
    }

    @Override
    public String toString() {
        return "ReqBean{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", beginFilename='" + beginFilename + '\'' +
                ", endFilename='" + endFilename + '\'' +
                '}';
    }
}
