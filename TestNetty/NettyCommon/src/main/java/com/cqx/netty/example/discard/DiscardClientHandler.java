package com.cqx.netty.example.discard;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles a client-side channel.
 */
public class DiscardClientHandler extends SimpleChannelInboundHandler<Object> {

    long counter;
    private ByteBuf content;
    private ChannelHandlerContext ctx;
    private final ChannelFutureListener trafficGenerator = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) {
            if (future.isSuccess()) {
                // 原先的示例代码，这里会导致一直递归，所以会一直发消息，这里替换成channel.close
//                generateTraffic();
                future.channel().close();
            } else {
                future.cause().printStackTrace();
                future.channel().close();
            }
        }
    };

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;

        // Initialize the message.
        content = ctx.alloc().directBuffer(DiscardClient.SIZE).writeZero(DiscardClient.SIZE);

        // Send the initial messages.
        System.out.println("【channelActive】Send the initial messages.");
        generateTraffic();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("【channelInactive】content.release.");
        content.release();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Server is supposed to send nothing, but if it sends something, discard it.
        System.out.println("【channelRead0】Server is supposed to send nothing, but if it sends something, discard it.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        System.out.println("【exceptionCaught】Close the connection when an exception is raised.");
        cause.printStackTrace();
        ctx.close();
    }

    private void generateTraffic() {
        // Flush the outbound buffer to the socket.
        // Once flushed, generate the same amount of traffic again.
        System.out.println("【generateTraffic】Flush the outbound buffer to the socket.");
        ctx.writeAndFlush(content.retainedDuplicate()).addListener(trafficGenerator);
    }
}
