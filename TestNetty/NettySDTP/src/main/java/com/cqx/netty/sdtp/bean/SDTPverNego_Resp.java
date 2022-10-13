package com.cqx.netty.sdtp.bean;

/**
 * XDR版本协商传输应答
 */
public class SDTPverNego_Resp extends SDTPlinkRel_Resp {

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.verNego_Resp;
    }
}
