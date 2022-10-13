package com.cqx.netty.upload.client;

/**
 * ReportRespBean
 *
 * @author chenqixu
 */
public class ReportRespBean {
    public static final String  FAIL_CODE="2";
    public static final String  SUCCESS_CODE="1";
    private static final String  TYPE="query_response";

    private String id;
    private String type;

    private String resultCode;
    private String failReason;

    //上报的文件数
    private long uploadNum;
    //上报的文件列表名称，用于记录日志
    private String uploadFileNames;


    public boolean isSuccess(){
        return SUCCESS_CODE.equals(resultCode);
    }


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
    public String getResultCode() {
        return resultCode;
    }
    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
    public String getFailReason() {
        return failReason;
    }
    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public void fail(String id,String failReason){
        this.id=id;
        this.type =TYPE;
        this.resultCode=FAIL_CODE;
        this.failReason=failReason;
    }

    public void success(String id){
        this.id =id;
        this.resultCode=SUCCESS_CODE;
        this.failReason="";
        this.type =TYPE;
    }

    public long getUploadNum() {
        return uploadNum;
    }

    public void setUploadNum(long uploadNum) {
        this.uploadNum = uploadNum;
    }

    public String getUploadFileNames() {
        return uploadFileNames;
    }

    public void setUploadFileNames(String uploadFileNames) {
        this.uploadFileNames = uploadFileNames;
    }
}
