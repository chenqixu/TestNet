package com.cqx.netty.example.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handler implementation for the echo server.
 */
@Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(EchoServerHandler.class);
    private Random random = new Random();
    private AtomicBoolean isFirst = new AtomicBoolean(true);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf recv = (ByteBuf) msg;
        try {
            int len = recv.readableBytes();

            byte[] reads = new byte[len];
            recv.readBytes(reads);
            logger.info(String.format("【channelRead】read.len: %s, read.msg: %s, ctx: %s", len, new String(reads), ctx));
            ByteBuf serverSend = Unpooled.buffer(1);
            // header : 1
            // body : EchoClient.SIZE
            int header = random.nextInt(1);
            if (isFirst.getAndSet(false)) {
                header = 1;
            }
            serverSend.writeInt(header);
            logger.info(String.format("【channelRead】header: %s, ctx.write(%s).", header, serverSend));
            ctx.write(serverSend);
        } finally {
            logger.info("【channelRead】release msg.");
            ReferenceCountUtil.release(recv);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        logger.info("【channelReadComplete】ctx.flush().");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        logger.info("【exceptionCaught】Close the connection when an exception is raised.");
        cause.printStackTrace();
        ctx.close();
    }
}
