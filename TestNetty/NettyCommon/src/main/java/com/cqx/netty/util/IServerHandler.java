package com.cqx.netty.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 公共服务处理接口
 *
 * @author chenqixu
 */
public abstract class IServerHandler extends ChannelInboundHandlerAdapter {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean isClose = false;

    /**
     * 数据处理
     *
     * @param buf
     * @return
     */
    protected abstract ByteBuf dealHandler(ByteBuf buf);

    /**
     * 资源释放
     */
    protected void release() {
    }

    /**
     * 异常资源释放
     */
    protected void exceptionRelease() {
    }

    /**
     * 这里我们覆盖了chanelRead()事件处理方法。 每当从客户端收到新的数据时，这个方法会在收到消息时被调用，
     * 这个例子中，收到的消息的类型是ByteBuf
     *
     * @param ctx 通道处理的上下文信息
     * @param msg 接收的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        logger.debug("Server received:" + buf);
        try {
            /**
             * 解析包类型，分别处理
             */
            ByteBuf result = dealHandler(buf);
            if (result != null) {
                logger.debug("返回结果给客户端:" + result);
                // 返回结果给客户端，并刷新缓冲
                ctx.writeAndFlush(result);
//                ctx.write(result);
            } else {
                logger.debug("不返回结果给客户端");
            }
        } finally {
            // 释放
            ReferenceCountUtil.release(buf);
        }
    }

    /**
     * 客户端消息读取完成之后的操作<br>
     * 总共有3种实现
     * <ur>
     * <li>第一种方法：写一个空的buf，并刷新写出区域。完成后关闭sock channel连接。<br>
     * ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
     * </li>
     * <li>第二种方法：在client端关闭channel连接，这样的话，会触发两次channelReadComplete方法。<br>
     * ctx.flush();</li>
     * <li>第三种：改成这种写法也可以，但是这种写法，没有第一种方法的好。<br>
     * ctx.flush().close().sync();</li>
     * </ur>
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.debug("server channelReadComplete..");
        if (this.isClose) {
            logger.debug("{} 通道准备关闭.", this);
            // 关闭通道，添加监听事件
            // 因为netty是异步的，需要等待线程执行完成，所以使用监听方式来进行资源释放比较合理
            ctx.close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    // 资源释放
                    release();
                }
            });
        } else {
            super.channelReadComplete(ctx);
        }
    }

    /***
     * 这个方法会在发生异常时触发
     *
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        /**
         * exceptionCaught() 事件处理方法是当出现 Throwable 对象才会被调用，即当 Netty 由于 IO
         * 错误或者处理器在处理事件时抛出的异常时。在大部分情况下，捕获的异常应该被记录下来 并且把关联的 channel
         * 给关闭掉。然而这个方法的处理方式会在遇到不同异常的情况下有不 同的实现，比如你可能想在关闭连接之前发送一个错误码的响应消息。
         */
        logger.info("server occur exception:" + cause.getMessage());
        // 出现异常就关闭
        cause.printStackTrace();
        ctx.close().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                // 异常资源释放
                exceptionRelease();
            }
        });
    }

    /**
     * 关闭客户端
     */
    protected void closeClient() {
        this.isClose = true;
    }

    /**
     * 获取关闭的状态
     *
     * @return isClose
     */
    public boolean isClose() {
        return isClose;
    }
}
