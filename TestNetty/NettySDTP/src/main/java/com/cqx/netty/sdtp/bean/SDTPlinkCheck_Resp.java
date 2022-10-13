package com.cqx.netty.sdtp.bean;

public class SDTPlinkCheck_Resp extends SDTPlinkRel_Resp {
    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.linkCheck_Resp;
    }
}