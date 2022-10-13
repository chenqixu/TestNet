package com.cqx.netty.sdtp.bean;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * SDTPMessage
 *
 * @author chenqixu
 */
public class SDTPMessage<T extends SDTPBody> {
    private SDTPHeader sdtpHeader;
    private List<T> sdtpBody = new ArrayList<>();
    private EnumMessageType MessageType;
    private ByteBuffer byteBuffer;

    public SDTPMessage(SDTPHeader sdtpHeader) {
        this.MessageType = sdtpHeader.getMessageType();
        this.sdtpHeader = sdtpHeader;
    }

    public SDTPMessage(EnumMessageType MessageType, SDTPHeader sdtpHeader) {
        this.MessageType = MessageType;
        this.sdtpHeader = sdtpHeader;
    }

    public void addSdtpBody(T sdtpBody) {
        this.sdtpBody.add(sdtpBody);
    }

    public void cleanBody() {
        this.sdtpBody.clear();
    }

    public void parserHeader(int TotalLength, EnumMessageType MessageType, long SequenceId, int TotalContents) {
        sdtpHeader.setTotalLength(TotalLength);
        sdtpHeader.setMessageType(MessageType);
        sdtpHeader.setSequenceId(SequenceId);
        sdtpHeader.setTotalContents(TotalContents);
    }

    // todo 在复用上可能有点问题
    public void generateHeader() {
        generateHeader(1L);
    }

    public void generateHeader(long sequenceId) {
        sdtpHeader.setMessageType(MessageType);
        // 设置header.SequenceId
        sdtpHeader.setSequenceId(sequenceId);
        // 设置header.TotalContents
        sdtpHeader.setTotalContents(sdtpBody.size());
        // 设置header.TotalLength
        // header固定，4G是9byte，5G是12byte
        // body由多个<XDRType,Load>组成
        // XDRType固定1byte
        // Load不定长
        int TotalLength = sdtpHeader.getHeaderBodyLength();
        for (SDTPBody sdtpBody : sdtpBody) {
            TotalLength += sdtpBody.length();
        }
        sdtpHeader.setTotalLength(TotalLength);

        byteBuffer = ByteBuffer.allocate(TotalLength);
        byteBuffer.put(sdtpHeader.getBytes());
        for (SDTPBody sdtpBody : sdtpBody) {
            byteBuffer.put(sdtpBody.getBytes());
        }
        byteBuffer.flip();
    }

    public byte[] getBytes() {
        int size = byteBuffer.limit();
        byte[] ret = new byte[size];
        byteBuffer.get(ret, 0, size);
        return ret;
    }

    public SDTPHeader getSdtpHeader() {
        return sdtpHeader;
    }

    public List<T> getSdtpBody() {
        return sdtpBody;
    }
}
