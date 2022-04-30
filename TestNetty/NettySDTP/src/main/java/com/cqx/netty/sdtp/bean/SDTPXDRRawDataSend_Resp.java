package com.cqx.netty.sdtp.bean;

/**
 * XDR对应原始数据传输应答
 *
 * @author chenqixu
 */
public class SDTPXDRRawDataSend_Resp extends SDTPlinkRel_Resp {

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.XDRRawDataSend_Resp;
    }
}
