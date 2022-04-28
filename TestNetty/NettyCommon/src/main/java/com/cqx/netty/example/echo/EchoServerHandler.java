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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handler implementation for the echo server.
 */
@Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(EchoServerHandler.class);
    private Random random = new Random();
    private AtomicBoolean isFirst = new AtomicBoolean(true);
    private ThreadLocal<EchoBean> threadLocal;
    private AtomicInteger echoID = new AtomicInteger(1);
    private volatile SDTPLinkedQueue sdtpLinkedQueue;
    private ThreadLocal<LinkedQueue> threadLocalLinkedQueue;

    public EchoServerHandler(SDTPLinkedQueue sdtpLinkedQueue) {
        this.sdtpLinkedQueue = sdtpLinkedQueue;
        this.threadLocal = new ThreadLocal<EchoBean>() {
            @Override
            public EchoBean initialValue() {
                return new EchoBean(echoID.getAndIncrement());
            }
        };
        this.threadLocalLinkedQueue = new ThreadLocal<LinkedQueue>() {
            @Override
            public LinkedQueue initialValue() {
                return sdtpLinkedQueue.next();
            }
        };
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf recv = (ByteBuf) msg;
        try {
            int len = recv.readableBytes();
            byte[] reads = new byte[len];
            recv.readBytes(reads);
            logger.info(String.format("【channelRead】echoID: %s, LinkedQueue: %s, read.len: %s, read.msg: %s, ctx: %s"
                    , threadLocal.get(), threadLocalLinkedQueue.get(),len, new String(reads), ctx));
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
