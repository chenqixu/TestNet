package com.cqx.netty.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 公共客户端处理接口
 *
 * @author chenqixu
 */
public abstract class IClientHandler<T> extends ChannelInboundHandlerAdapter {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map<String, String> params;
    private CountDownLatch latch;
    private T t;

    private ByteBuf defaultRequest() {
        return Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8);
    }

    protected ByteBuf sendChannelActiveRequest() {
        return defaultRequest();
    }

    protected void dealResponse(ByteBuf buf) {
    }

    protected ByteBuf channelReadSend() {
        return null;
    }

    protected String getParams(String key) {
        return this.params.get(key);
    }

    public void resetSync(CountDownLatch latch) {
        this.latch = latch;
    }

    public void releaseSync() {
        latch.countDown();
    }

    public T getResult() {
        return t;
    }

    public void setResult(T t) {
        this.t = t;
    }

    /**
     * 发送请求到服务器
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("client channelActive..");
        ctx.writeAndFlush(sendChannelActiveRequest());
    }

    /**
     * 解析服务器返回的数据
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg; // (1)
        try {
            logger.debug("Client received:" + ByteBufUtil.hexDump(buf));
            dealResponse(buf);
            ByteBuf send = channelReadSend();
            if (send != null) {
                ctx.writeAndFlush(send.array());
            }
            ctx.close();
        } finally {
            buf.release();
        }
    }

    /**
     * 异常捕获处理
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
