package com.cqx.netty.sdtp.bean;

/**
 * 当信道上没有数据传输时，客户端应每隔时间C发送链路检测包以维持此连接，<p>
 * 当链路检测包发出超过时间T后未收到响应，应立即再发送链路检测包。<p>
 * 服务端收到链路检测请求包后，立即回复链路检测应答。<p>
 * 参数：<p>
 * &nbsp;&nbsp;&nbsp;&nbsp;请求--无参数<p>
 * &nbsp;&nbsp;&nbsp;&nbsp;应答--无参数
 */
public class SDTPlinkCheck_Resp extends SDTPlinkCheck_Req {

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.linkCheck_Resp;
    }
}