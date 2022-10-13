package com.cqx.netty.sdtp.bean;

import com.cqx.common.utils.system.ByteUtil;

/**
 * 消息类型
 *
 * @author chenqixu
 */
public enum EnumMessageType {
    verNego_Req(0x0001, "版本协商请求"),
    verNego_Resp(0x8001, "版本协商应答"),
    linkAuth_Req(0x0002, "鉴权请求"),
    linkAuth_Resp(0x8002, "鉴权响应"),
    linkCheck_Req(0x0003, "链路检测请求"),
    linkCheck_Resp(0x8003, "链路检测应答"),
    linkRel_Req(0x0004, "连接释放请求"),
    linkRel_Resp(0x8004, "连接释放应答"),
    linkDataCheck_Req(0x0007, "链路数据发送校验请求"),
    linkDataCheck_Resp(0x8007, "链路数据发送校验应答"),
    notifyXDRData_Req(0x0005, "XDR数据通知请求"),
    notifyXDRData_Resp(0x8005, "XDR数据通知应答"),
    XDRRawDataSend_Req(0x0006, "XDR对应原始数据传输请求"),
    XDRRawDataSend_Resp(0x8006, "XDR对应原始数据传输应答"),
    notifyCompressXDRData_Req(0x000B, "XDR压缩数据通知请求"),
    notifyCompressXDRData_Resp(0x800B, "XDR压缩数据通知应答"),
    XDRCompressRawDataSend_Req(0x0106, "XDR对应原始压缩数据传输请求"),
    XDRCompressRawDataSend_Resp(0x8106, "XDR对应原始压缩数据传输应答"),
    CompressNego_Req(0x000E, "压缩协商请求"),
    CompressNego_Resp(0x800E, "压缩协商应答"),
    ;

    private byte[] value;
    private int intValue;
    private String desc;

    EnumMessageType(int value, String desc) {
        this.intValue = value;
        this.value = ByteUtil.intTo2ByteArray(value);
        this.desc = desc;
    }

    public static EnumMessageType ValueOf(int intValue) {
        if (intValue == linkRel_Req.intValue) {
            return linkRel_Req;
        } else if (intValue == linkRel_Resp.intValue) {
            return linkRel_Resp;
        } else if (intValue == notifyXDRData_Req.intValue) {
            return notifyXDRData_Req;
        } else if (intValue == notifyXDRData_Resp.intValue) {
            return notifyXDRData_Resp;
        }else if (intValue == XDRRawDataSend_Req.intValue) {
            return XDRRawDataSend_Req;
        } else if (intValue == XDRRawDataSend_Resp.intValue) {
            return XDRRawDataSend_Resp;
        }
        return null;
    }

    public int intValue() {
        return this.intValue;
    }

    public byte[] getValue() {
        return this.value;
    }

    public String getDesc() {
        return this.desc;
    }
}
