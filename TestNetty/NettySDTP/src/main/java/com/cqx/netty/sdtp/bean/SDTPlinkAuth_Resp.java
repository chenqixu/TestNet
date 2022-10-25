package com.cqx.netty.sdtp.bean;

import com.cqx.common.utils.system.ByteUtil;

public class SDTPlinkAuth_Resp implements SDTPBody {
    // 鉴权的返回结果。各个值的含义如下定义：
    // 1 代表鉴权通过。
    // 2 代表LoginID不存在。
    // 3 代表SHA256加密结果出错。
    private byte Result = (byte) 0x01;
    // 用于对端对本端进行鉴权，其值通过SHA256计算得出。
    // 当对端使用相同方式加密之后与接收的值比较，如果计算出来的值相同，则通过校验，否则出错。
    private String Digest;
    private byte[] Results;

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.linkAuth_Resp;
    }

    @Override
    public int length() {
        return Results.length;
    }

    @Override
    public byte[] getBytes() {
        if (this.Results == null) {
            Results = new byte[]{0x01};
            return Results;
        } else {
            return Results;
        }
    }

    @Override
    public void setData(byte[] bytes) {
        if (bytes.length == 65) {
            this.Results = bytes;
            this.Result = bytes[0];
            byte[] DigestArray = new byte[64];
            System.arraycopy(bytes, 1, DigestArray, 0, 64);
            this.Digest = ByteUtil.bytesToHexStringH(DigestArray, " ");
        } else {
            throw new NullPointerException("传入的数据为空！");
        }
    }

    public String getResult() {
        return ByteUtil.unsignedByte(this.Result);
    }

    public String getDigest() {
        return this.Digest;
    }
}
