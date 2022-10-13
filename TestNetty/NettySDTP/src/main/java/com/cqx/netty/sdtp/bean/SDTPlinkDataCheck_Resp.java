package com.cqx.netty.sdtp.bean;

public class SDTPlinkDataCheck_Resp extends SDTPlinkRel_Resp {

    private byte[] Result;

    @Override
    public int length() {
        return Result.length;
    }

    @Override
    public byte[] getBytes() {
        if (this.Result == null) {
            return new byte[]{0x01};
        } else {
            return Result;
        }
    }

    @Override
    public void setData(byte[] bytes) {
        if (bytes.length > 1) {
            this.Result = bytes;
        } else {
            throw new NullPointerException("传入的数据为空！");
        }
    }

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.linkDataCheck_Resp;
    }
}