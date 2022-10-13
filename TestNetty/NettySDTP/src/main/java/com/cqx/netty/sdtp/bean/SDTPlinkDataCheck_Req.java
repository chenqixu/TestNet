package com.cqx.netty.sdtp.bean;

import com.cqx.common.utils.system.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


public class SDTPlinkDataCheck_Req implements SDTPBody {
    private long Sendflag;
    private long SendDataInfo;

    private int length = 4 + 4;

    private ByteBuf byteBuffer = Unpooled.buffer(length);

    @Override
    public int length() {
        return length;
    }

    @Override
    public byte[] getBytes() {
        byte[] newArray = ByteUtil.longTo4ByteArray(Sendflag);
        byte[] newArray1 = ByteUtil.arrayAdd(newArray, ByteUtil.longTo4ByteArray(SendDataInfo), 4);
        return newArray1;
    }

    @Override
    public void setData(byte[] bytes) {
        if (bytes.length == length) {
            byteBuffer.writeBytes(bytes);
            Sendflag = byteBuffer.readUnsignedInt();
            SendDataInfo = byteBuffer.readUnsignedShort();
        } else {
            throw new NullPointerException("传入的数据不正确！");
        }
    }

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.linkDataCheck_Req;
    }
}
