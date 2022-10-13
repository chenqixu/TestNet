package com.cqx.netty.sdtp.bean;

/**
 * 版本协商请求
 *
 * @author chenqixu
 */
public class SDTPverNego_Req implements SDTPBody {
    private byte Version = (byte) 0x01;
    private byte SubVersion = (byte) 0x00;

    @Override
    public int length() {
        return 2;
    }

    @Override
    public byte[] getBytes() {
        return new byte[]{Version, SubVersion};
    }

    @Override
    public void setData(byte[] bytes) {
        if (bytes.length > 1) {
            this.Version = bytes[0];
            this.SubVersion = bytes[1];
        } else {
            throw new NullPointerException("传入的数据为空！");
        }
    }

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.verNego_Req;
    }
}
