package com.cqx.netty.sdtp.bean;

import com.cqx.common.utils.system.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * <h2>链路数据发送校验linkDataCheck</h2>
 * 应答
 * <pre>
 *     本消息与notifyXDRData_Req和XDRRawDataSend_Req消息方向相反。
 *     消息的作用是，告知数据发送方，在两个本消息间隔期内接收了多少个notifyXDRData_Req
 *     和XDRRawDataSend_Req消息包，以便告知发送方是否有丢包情况发生。
 * </pre>
 */
public class SDTPlinkDataCheck_Resp implements SDTPBody {
    // length 4 检测包顺序标签，1小时内不重复即可。（与请求包相同）
    private long Sendflag;
    // length 1 0：数据量正确；1：接收方数据小于发送方数据；2：接收方；数据大于发送方数据。
    private byte Result = 0x00;
    // length 4 距离上次发送间发送的数据包数量。（仅包含notifyXDRData_Req和XDRRawDataSend_Req包的数量）（与请求包相同）
    private long SendDataInfo;
    // length 4 距离上次发送间接收的数据包数量。（仅包含notifyXDRData_Req和XDRRawDataSend_Req包的数量）
    private long RecDataInfo;

    private byte[] body;

    private int length = 4 + 1 + 4 + 4;
    private ByteBuf byteBuffer = Unpooled.buffer(length);

    @Override
    public int length() {
        return length;
    }

    @Override
    public byte[] getBytes() {
        return body;
    }

    @Override
    public void setData(byte[] bytes) {
        if (bytes.length > 0) {
            if (bytes.length == length) {
                body = bytes;
                byteBuffer.clear();
                byteBuffer.writeBytes(bytes);
                this.Sendflag = byteBuffer.readInt();
                this.Result = byteBuffer.readByte();
                this.SendDataInfo = byteBuffer.readInt();
                this.RecDataInfo = byteBuffer.readInt();
            } else {
                throw new NullPointerException(String.format("传入的数据长度不正确！链路数据发送校验linkDataCheck应答长度需要为: %s", length));
            }
        } else {
            throw new NullPointerException("传入的数据为空！");
        }
    }

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.linkDataCheck_Resp;
    }

    public long getSendflag() {
        return Sendflag;
    }

    public long getSendDataInfo() {
        return SendDataInfo;
    }

    public long getRecDataInfo() {
        return RecDataInfo;
    }

    public String getResult() {
        return ByteUtil.unsignedByte(Result);
    }
}