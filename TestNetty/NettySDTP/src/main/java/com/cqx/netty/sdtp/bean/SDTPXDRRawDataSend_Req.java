package com.cqx.netty.sdtp.bean;

/**
 * XDR对应原始数据传输请求
 *
 * @author chenqixu
 */
public class SDTPXDRRawDataSend_Req implements SDTPBody {
    private byte[] Load;

    @Override
    public int length() {
        return Load.length;
    }

    @Override
    public byte[] getBytes() {
        return Load;
    }

    @Override
    public void setData(byte[] bytes) {
        if (bytes.length > 0) {
            this.Load = bytes;
        } else {
            throw new NullPointerException("传入的数据为空！");
        }
    }

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.XDRRawDataSend_Req;
    }
}
