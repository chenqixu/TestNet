package com.cqx.netty.sdtp.bean;

import com.cqx.common.utils.system.ArraysUtil;
import com.cqx.common.utils.system.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * <h2>链路数据发送校验linkDataCheck</h2>
 * 请求
 * <pre>
 *     本消息与notifyXDRData_Req和XDRRawDataSend_Req消息同方向。
 *     消息的作用是，告知数据接收方，在两个本消息间隔期内发送的notifyXDRData_Req
 *     和XDRRawDataSend_Req消息包数量，以便接收方校验是否有丢包情况发生。
 *     若有丢包发生时，notifyXDRData_Req
 *     和XDRRawDataSend_Req消息的接收方应立即通过返回消息告知发送方有数据丢失。
 *     本消息建议5分钟进行数据校验，数据量大时为避免过多的传输缓存，
 *     可减少时间间隔，数据接收方应不受校验周期影响。
 * </pre>
 */
public class SDTPlinkDataCheck_Req implements SDTPBody {
    // length 4 检测包顺序标签，1小时内不重复即可。本参数目的是为发现校验包的丢失情况发生。
    private long Sendflag;
    // length 4 距离上次发送间发送的数据包数量。（仅仅包含notifyXDRData_Req和XDRRawDataSend_Req包的数量）
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
        return ArraysUtil.arrayAdd(newArray, ByteUtil.longTo4ByteArray(SendDataInfo), 4);
    }

    @Override
    public void setData(byte[] bytes) {
        if (bytes.length == length) {
            byteBuffer.writeBytes(bytes);
            Sendflag = byteBuffer.readUnsignedInt();
            SendDataInfo = byteBuffer.readUnsignedInt();
        } else {
            throw new NullPointerException("传入的数据不正确！");
        }
    }

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.linkDataCheck_Req;
    }
}
