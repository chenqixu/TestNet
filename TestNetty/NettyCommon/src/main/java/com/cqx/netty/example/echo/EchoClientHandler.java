package com.cqx.netty.example.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handler implementation for the echo client.  It initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class EchoClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(EchoClientHandler.class);
    private volatile boolean isCLose = false;
    private AtomicInteger atomicInteger = new AtomicInteger(1);

    /**
     * Creates a client-side handler.
     */
    public EchoClientHandler() {
    }

    private ByteBuf buildSendBuf(String msg) {
        ByteBuf send = Unpooled.buffer(msg.getBytes().length);
        send.writeBytes(msg.getBytes());
        return send;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ByteBuf send = buildSendBuf("1-channelActive");
        logger.info(String.format("【channelActive】ctx.writeAndFlush(%s).", send));
        ctx.writeAndFlush(send);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf recv = (ByteBuf) msg;
        int header = recv.readInt();
        logger.info(String.format("【channelRead】read(%s), header: %s.", msg, header));
        if (header == 1) {
            ByteBuf send = buildSendBuf(atomicInteger.incrementAndGet() + "-channelRead");
            logger.info(String.format("【channelRead】ctx.write(%s).", send));
            ctx.write(send);
        } else {
            logger.info("【channelRead】ctx not write, to close ctx.");
            isCLose = true;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        logger.info("【channelReadComplete】ctx.flush().");
        ctx.flush();
        if (isCLose) {
            logger.info("【channelReadComplete】ctx.close().");
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        logger.info("【exceptionCaught】Close the connection when an exception is raised.");
        cause.printStackTrace();
        ctx.close();
    }
}