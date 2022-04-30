package com.cqx.netty.sdtp.bean;

/**
 * SDTPBody
 *
 * @author chenqixu
 */
public interface SDTPBody {

    int length();

    byte[] getBytes();

    void setData(byte[] bytes);

    EnumMessageType getMessageType();
}
