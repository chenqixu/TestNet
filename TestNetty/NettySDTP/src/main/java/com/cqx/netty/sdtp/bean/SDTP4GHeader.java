package com.cqx.netty.sdtp.bean;

import com.cqx.common.utils.system.ByteUtil;
import com.cqx.netty.sdtp.util.Constant;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * SDTP4GHeader
 * <table>
 * <tr>
 * <th align="left">字段名</th>
 * <th align="left">字节数</th>
 * <th align="left">类型</th>
 * <th align="left">描述</th>
 * </tr>
 * <tr>
 * <td align="left">TotalLength</td>
 * <td align="left">2</td>
 * <td align="left">Unsigned integer</td>
 * <td align="left">消息总长度(含消息头及消息体)</td>
 * </tr>
 * <tr>
 * <td>MessageType</td>
 * <td>2</td>
 * <td>Unsigned integer</td>
 * <td>消息类型</td>
 * </tr>
 * <tr>
 * <td>SequenceId</td>
 * <td>4</td>
 * <td>Unsigned integer</td>
 * <td>交互的流水号，顺序累加，步长为1，循环使用（一个交互的一对请求和应答消息的流水号必须相同）</td>
 * </tr>
 * <tr>
 * <td>TotalContents</td>
 * <td>1</td>
 * <td>Unsigned integer</td>
 * <td>消息体中的事件数量（最多40条）<br>
 * 若考虑实时性要求，可每次只填一个事件。<br>
 * 对于notifyXDRData请求消息，一个事件是指5.2.5.2中所定义的完整结构，即XDRType+Load</td>
 * </tr>
 * </table>
 *
 * @author chenqixu
 */
public class SDTP4GHeader implements SDTPHeader {
    private int TotalLength;
    private EnumMessageType MessageType;
    private long SequenceId;
    private int TotalContents;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(getHeaderBodyLength());
    private ByteBuf parserBuffer;
    private String hexStr;

    public byte[] getBytes() {
        byteBuffer.clear();
        byteBuffer.put(ByteUtil.intTo2ByteArray(TotalLength));
        byteBuffer.put(MessageType.getValue());
        byteBuffer.put(ByteUtil.longTo4ByteArray(SequenceId));
        byteBuffer.put((byte) (TotalContents & 0xFF));
        byteBuffer.flip();
        byte[] bytes = new byte[getHeaderBodyLength()];
        byteBuffer.get(bytes, 0, getHeaderBodyLength());
        return bytes;
    }

    public long getTotalLength() {
        return TotalLength;
    }

    public void setTotalLength(long totalLength) {
        TotalLength = Integer.valueOf(Long.valueOf(totalLength).toString());
    }

    public EnumMessageType getMessageType() {
        return MessageType;
    }

    public void setMessageType(EnumMessageType messageType) {
        MessageType = messageType;
    }

    public long getSequenceId() {
        return SequenceId;
    }

    public void setSequenceId(long sequenceId) {
        SequenceId = sequenceId;
    }

    public int getTotalContents() {
        return TotalContents;
    }

    public void setTotalContents(int totalContents) {
        TotalContents = totalContents;
    }

    @Override
    public int getHeaderBodyLength() {
        return 9;
    }

    @Override
    public void parser(ByteBuf buf) {
        // 解析Hex专用
        this.parserBuffer = buf;
        // [Message Header]
        // sdtp数据帧长度
        int msgLength = buf.readUnsignedShort();
        // 消息类型
        int msgType = buf.readUnsignedShort();
        // sdtp包头中的交互的流水号
        long sequenceId = buf.readUnsignedInt();
        // sdtp包头中的事件数量
        int totalContents = buf.readUnsignedByte();
        EnumMessageType messageType = EnumMessageType.ValueOf(msgType);
        if (messageType == null) {
            messageType = EnumMessageType.linkRel_Req;
        }
        // 设置值
        setTotalLength(msgLength);
        setMessageType(messageType);
        setSequenceId(sequenceId);
        setTotalContents(totalContents);
    }

    @Override
    public void parserToHex() {
        StringBuilder sb = new StringBuilder();
        parserBuffer.resetReaderIndex();
        byte[] all_array = new byte[getHeaderBodyLength()];
        parserBuffer.getBytes(0, all_array);
        sb.append(String.format("AllHex: %s", ByteUtil.bytesToHexStringH(all_array))).append(Constant.LineENd);
        // [Message Header]
        // sdtp数据帧长度
        byte[] msgLength_array = new byte[2];
        parserBuffer.getBytes(0, msgLength_array);
        long msgLength = parserBuffer.readUnsignedShort();
        sb.append(String.format("msgLength: %s, HEX: %s", msgLength, ByteUtil.bytesToHexStringH(msgLength_array))).append(Constant.LineENd);
        // 消息类型
        byte[] msgType_array = new byte[2];
        parserBuffer.getBytes(2, msgType_array);
        int msgType = parserBuffer.readUnsignedShort();
        sb.append(String.format("msgType: %s, HEX: %s", msgType, ByteUtil.bytesToHexStringH(msgType_array))).append(Constant.LineENd);
        // sdtp包头中的交互的流水号
        byte[] sequenceId_array = new byte[4];
        parserBuffer.getBytes(2 + 2, sequenceId_array);
        long sequenceId = parserBuffer.readUnsignedInt();
        sb.append(String.format("sequenceId: %s, HEX: %s", sequenceId, ByteUtil.bytesToHexStringH(sequenceId_array))).append(Constant.LineENd);
        // sdtp包头中的事件数量
        byte[] totalContents_array = new byte[1];
        parserBuffer.getBytes(2 + 2 + 4, totalContents_array);
        int totalContents = parserBuffer.readUnsignedByte();
        sb.append(String.format("totalContents: %s, HEX: %s", totalContents, ByteUtil.bytesToHexStringH(totalContents_array))).append(Constant.LineENd);
        hexStr = sb.toString();
    }

    @Override
    public String getHexStr() {
        return hexStr;
    }

    @Override
    public String toString() {
        return String.format("TotalLength: %s, MessageType: %s, SequenceId: %s, TotalContents: %s"
                , TotalLength, MessageType, SequenceId, TotalContents);
    }
}
