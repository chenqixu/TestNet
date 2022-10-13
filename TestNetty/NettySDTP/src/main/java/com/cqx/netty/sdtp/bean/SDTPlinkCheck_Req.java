package com.cqx.netty.sdtp.bean;

public class SDTPlinkCheck_Req implements SDTPBody {

    private byte[] Reason;

    @Override
    public int length() {
        return 0;
    }

    @Override
    public byte[] getBytes() {
        return null;
    }

    @Override
    public void setData(byte[] bytes) {
        if (bytes.length > 1) {
            this.Reason = bytes;
        } else {
            throw new NullPointerException("传入的数据为空！");
        }
    }

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.linkCheck_Req;
    }

}
