package com.cqx.netty.upload.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * ReUploadClient
 *
 * @author chenqixu
 */
public class ReUploadClient {
    private static final Logger log = LoggerFactory.getLogger(ReUploadClient.class);

    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private ReUploadClientHandler client;


    public ReUploadClient(String hostIp,int port) throws InterruptedException {
        log.info("----------准备连接【"+hostIp+"："+port+"】");
        initClient(hostIp,port);
    }

    public String run(String req ) throws Exception {
        client.setReq(req);
        Future future = executor.submit(client);

        return (String) future.get();
    }

    //初始化客户端
    private void initClient(String hostIp,int port) throws InterruptedException {
        client = new ReUploadClientHandler();
        final NioEventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //设置POJO解码器对序列化的对象进行解码，设置对象序列化长度为1M防止内存溢出
//                        pipeline.addLast( new ObjectDecoder(1024 * 1024,ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
                        //添加对象编码器，再服务器响应消息的时候自动对序列化的POJO进行编码
//                        pipeline.addLast(new ObjectEncoder());
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(client);
                    }
                });

        try {
            ChannelFuture future = bootstrap.connect(hostIp,port).sync();
            if (future.isSuccess()){
                log.info("-------连接【"+hostIp+"："+port+"】成功！");
            }
            //在监听器中释放资源
            future.channel().closeFuture().addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    log.info(channelFuture.channel().toString()+"----------链路关闭");
                    //关闭线程组，这样线程就不会阻塞
                    group.shutdownGracefully();
                }
            }); //.sync(); // 异步等待关闭连接channel
            log.info("closed.."); // 关闭完成
//            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("连接失败！"+e.getMessage());
            e.printStackTrace();
//        } finally {
//            group.shutdownGracefully().sync(); // 释放线程池资源
        }
    }
}
