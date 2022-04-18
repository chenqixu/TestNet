package com.cqx.netty.util;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共服务接口
 *
 * @author chenqixu
 */
public class IServer {
    private static final Logger logger = LoggerFactory.getLogger(IServer.class);
    private int port = 0;
    private Map<String, String> params = new HashMap<>();
    private Class<? extends ServerChannel> channelCls;
    private List<IServerHandler> socketChannelList = new ArrayList<>();

    private IServer() {
    }

    public static IServer newbuilder() {
        return new IServer();
    }

    public void start() throws Exception {
        if (!check()) throw new Exception("运行参数不满足！具体参数：" + getRunParams());
        int bossGroup_nThreads = Utils.setValDefault(params, NetConstant.bossGroup_nThreads, 1);
        int workerGroup_nThreads = Utils.setValDefault(params, NetConstant.workerGroup_nThreads, 1);
        if (channelCls == null) channelCls = NioServerSocketChannel.class;
        //================================
        // (1)
        //================================
        // 使用两个NioEventLoopGroup。
        // 第一个通常被称为“boss”，接受传入的连接。
        // 第二个通常被称为“worker”，在boss接受连接并将接受的连接注册到worker之后，处理接受连接的通信量。
        EventLoopGroup bossGroup = new NioEventLoopGroup(bossGroup_nThreads); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerGroup_nThreads);
        try {
            //================================
            // (2)
            //================================
            // ServerBootstrap是一个帮助类，用于设置服务器。可以直接使用通道设置服务器。
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    //================================
                    // (3)
                    //================================
                    // 这里，我们指定使用NioServerSocketChannel类，该类用于实例化新通道以接受传入连接。
                    .channel(channelCls) // (3)
                    //================================
                    // (4)
                    //================================
                    // 此处指定的处理程序将始终由新接受的通道进行评估。
                    // ChannelInitializer是一个特殊的处理程序，旨在帮助用户配置新的频道。
                    // 用户很可能希望通过添加一些处理程序（如DiscardServerHandler）来配置新通道的ChannelPipeline，以实现网络应用程序。
                    // 随着应用程序变得复杂，很可能会向管道中添加更多处理程序，并最终将这个匿名类提取到顶级类中。
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            for (IServerHandler iServerHandler : socketChannelList) {
                                pipeline.addLast(iServerHandler);
                            }
                        }
                    })
                    //================================
                    // (5)
                    //================================
                    // 还可以设置特定于通道实现的参数。
                    // 我们正在编写一个TCP/IP服务器，所以我们可以设置套接字选项，比如tcpNoDelay和keepAlive。
                    // 请参考ChannelOption的apidocs和具体的ChannelConfig实现，以了解受支持的ChannelOptions的概述。
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    //================================
                    // (6)
                    //================================
                    // 你注意到option()和childOption()了吗？
                    // option()用于接受传入连接的NioServerSocketChannel。
                    // childOption()用于父服务器通道接受的通道，在本例中是NioSocketChannel。
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            //================================
            // (7)
            //================================
            // 绑定到端口并启动服务器。
            // 这里，我们绑定到机器中所有NIC（网络接口卡）的端口8080。
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }
    }

    private boolean check() {
        if (port > 0)
            return true;
        return false;
    }

    public IServer setPort(int port) {
        this.port = port;
        return this;
    }

    public IServer setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public IServer addSocketChannel(IServerHandler iServerHandler) {
        this.socketChannelList.add(iServerHandler);
        return this;
    }

    private String getRunParams() {
        return "[port：" + port + " [params：" + params;
    }
}
