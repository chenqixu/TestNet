package com.cqx.netty.upload.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReUploadServer
 *
 * @author chenqixu
 */
public class ReUploadServer {
    //上报集团
    public static final String UPLOAD_GROUP = "upload_group";
    //上报管局
    public static final String UPLOAD_STORAGE = "upload_storage";
    private static final Logger logger = LoggerFactory.getLogger(ReUploadServer.class);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;
    private ServerBootstrap serverbootstrap;
    private String ip;
    private int port;
    private ReUploadCfgBean reUploadCfgBean;
    private String type;


    public ReUploadServer(String ip, int port, ReUploadCfgBean reUploadCfgBean, String type) {
        this.ip = ip;
        this.port = port;
        this.reUploadCfgBean = reUploadCfgBean;
        this.type = type;
    }

    public static void main(String[] args) throws Exception {
        ReUploadCfgBean reUploadCfgBean = new ReUploadCfgBean();
        // 文件上报源目录
        reUploadCfgBean.setSourceDir("d:\\tmp\\data\\jkreport\\if_upload_hb_netlog\\");
        // 源文件存放的样式 0:无动态目录 1：YYYYMMDD(天目录)  2:YYYYMMDDHH(小时目录)
        reUploadCfgBean.setSourceDirType("0");
        // 文件上报目录
        reUploadCfgBean.setUploadDir("d:\\tmp\\data\\jkreport\\hblog\\");
        reUploadCfgBean.setMsgDir("");

        // 集中存储
        new ReUploadServer("127.0.0.1", 8989, reUploadCfgBean, "upload_storage").start();
    }

    public void start() throws Exception {
        logger.info("ReUploadServer 【{}:{} 】 is starting..", ip, port);
        bossGroup = new NioEventLoopGroup(1);
        workGroup = new NioEventLoopGroup(3);
        try {
            //服务端启动对象
            serverbootstrap = new ServerBootstrap();
            serverbootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //设置POJO解码器对序列化的对象进行解码，设置对象序列化长度为1M防止内存溢出
                            //pipeline.addLast( new ObjectDecoder(1024 * 1024,ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
                            //添加对象编码器，再服务器响应消息的时候自动对序列化的POJO进行编码
                            //pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            //pipeline.addLast(new IdleStateHandler(60,60,60,TimeUnit.SECONDS));

                            //集团
                            if (UPLOAD_GROUP.equals(type)) {
                                pipeline.addLast(new ReUploadGroupServerHandler(reUploadCfgBean));
                            } else {
                                //集中存储
                                pipeline.addLast(new ReUploadServerHandler(reUploadCfgBean));     //业务处理类
                            }
                        }
                    });
            // 绑定端口, 同步等待成功;
            ChannelFuture future = serverbootstrap.bind(ip, port).sync();
            logger.info("重报模块服务端开始提供服务....");
            // 等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("Netty服务端发生异常!!", e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

}
