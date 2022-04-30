package com.cqx.netty.sdtp.bean;

/**
 * XDR数据通知应答，notifyXDRData_Resp
 *
 * @author chenqixu
 */
public class SDTPnotifyXDRData_Resp extends SDTPlinkRel_Resp {

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.notifyXDRData_Resp;
    }
}
