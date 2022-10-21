package com.cqx.netty.sdtp.bean;

import com.cqx.common.utils.system.ArraysUtil;
import com.cqx.common.utils.system.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

/**
 * 鉴权请求
 *
 * @author chenqixu
 */
public class SDTPlinkAuth_Req implements SDTPBody {
    // length 12
    private String LoginID;
    // length 64
    private String Digest;
    // length 4，Unsigned integer
    private long Timestamp;
    // length 2，Unsigned short
    private int RAND;

    private int length = 12 + 64 + 4 + 2;
    private ByteBuf byteBuffer = Unpooled.buffer(length);

    @Override
    public int length() {
        return length;
    }

    @Override
    public byte[] getBytes() {
        byte[] LoginID_byte = LoginID.getBytes(StandardCharsets.UTF_8);
        byte[] Digest_byte = Digest.getBytes(StandardCharsets.ISO_8859_1);
        byte[] newArray = ArraysUtil.arrayAdd(LoginID_byte, Digest_byte, Digest_byte.length);
        byte[] newArray1 = ArraysUtil.arrayAdd(newArray, ByteUtil.longTo4ByteArray(Timestamp), 4);
        return ArraysUtil.arrayAdd(newArray1, ByteUtil.intTo2ByteArray(RAND), 2);
    }

    @Override
    public void setData(byte[] bytes) {
        if (bytes.length == length) {
            byteBuffer.writeBytes(bytes);
            byte[] LoginID_byte = new byte[12];
            byteBuffer.readBytes(LoginID_byte);
            LoginID = new String(LoginID_byte, StandardCharsets.UTF_8);
            byte[] Digest_byte = new byte[64];
            byteBuffer.readBytes(Digest_byte);
            Digest = new String(Digest_byte, StandardCharsets.ISO_8859_1);
            Timestamp = byteBuffer.readUnsignedInt();
            RAND = byteBuffer.readUnsignedShort();
        } else {
            throw new NullPointerException("传入的数据不正确！");
        }
    }

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.linkAuth_Req;
    }
}
