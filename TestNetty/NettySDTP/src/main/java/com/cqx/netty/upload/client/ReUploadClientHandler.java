package com.cqx.netty.upload.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Callable;

/**
 * ReUploadClientHandler
 *
 * @author chenqixu
 */
public class ReUploadClientHandler extends ChannelInboundHandlerAdapter implements Callable {

    private ChannelHandlerContext context;
    private String reqJson;
    private String respJson;

    /***
     * @description:  与服务器的连接创建成功后，就被调用
     * @param ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context = ctx;
    }
    /***
     * @description: 收到服务器数据后，被调用
     * @param ctx
     * @param msg
     */
    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//      result = msg.toString();
        respJson = (String) msg;
        notifyAll();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        context.close();
    }

    @Override
    public synchronized Object call() throws Exception {
        context.writeAndFlush(reqJson);
        wait();
        context.close();
        return respJson;
    }

    void setReq(String  reqJson){
        this.reqJson=reqJson;
    }

}
