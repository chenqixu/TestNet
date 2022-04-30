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

    public SDTPMessage(EnumMessageType MessageType) {
        this.MessageType = MessageType;
    }

    public void addSdtpBody(T sdtpBody) {
        this.sdtpBody.add(sdtpBody);
    }

    public void cleanBody() {
        this.sdtpBody.clear();
    }

    public void parserHeader(long TotalLength, EnumMessageType MessageType, long SequenceId, int TotalContents) {
        sdtpHeader = new SDTPHeader();
        sdtpHeader.setTotalLength(TotalLength);
        sdtpHeader.setMessageType(MessageType);
        sdtpHeader.setSequenceId(SequenceId);
        sdtpHeader.setTotalContents(TotalContents);
    }

    public void generateHeader() {
        sdtpHeader = new SDTPHeader();
        sdtpHeader.setMessageType(MessageType);
        // 设置header.SequenceId
        sdtpHeader.setSequenceId(1);
        // 设置header.TotalContents
        sdtpHeader.setTotalContents(sdtpBody.size());
        // 设置header.TotalLength
        // header固定12byte
        // body由多个<XDRType,Load>组成
        // XDRType固定1byte
        // Load不定长
        int TotalLength = 12;
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
