package com.cqx.netty.util;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap serverBootstrap;

    private IServer() {
    }

    public static IServer newbuilder() {
        return new IServer();
    }

    public ServerBootstrap buildBootstrap() throws Exception {
        if (!check()) throw new NullPointerException("运行参数不满足！具体参数：" + getRunParams());
        int bossGroup_nThreads = Utils.setValDefault(params, NetConstant.bossGroup_nThreads, 1);
        int workerGroup_nThreads = Utils.setValDefault(params, NetConstant.workerGroup_nThreads, 1);
        int SO_BACKLOG = Utils.setValDefault(params, NetConstant.SO_BACKLOG, 128);
        boolean SO_KEEPALIVE = Utils.setValDefault(params, NetConstant.SO_KEEPALIVE, true);
        boolean TCP_NODELAY = Utils.setValDefault(params, NetConstant.TCP_NODELAY, false);
        if (channelCls == null) channelCls = NioServerSocketChannel.class;
        //================================
        // (1)
        //================================
        // 使用两个NioEventLoopGroup。
        // 第一个通常被称为“boss”，接受传入的连接。
        // 第二个通常被称为“worker”，在boss接受连接并将接受的连接注册到worker之后，处理接受连接的通信量。
        bossGroup = new NioEventLoopGroup(bossGroup_nThreads); // (1)
        workerGroup = new NioEventLoopGroup(workerGroup_nThreads);
        //================================
        // (2)
        //================================
        // ServerBootstrap是一个帮助类，用于设置服务器。可以直接使用通道设置服务器。
        serverBootstrap = new ServerBootstrap(); // (2)
        serverBootstrap.group(bossGroup, workerGroup)
                //================================
                // (3)
                //================================
                // 这里，我们指定使用NioServerSocketChannel类，该类用于实例化新通道以接受传入连接。
                .channel(channelCls) // (3)
                //================================
                // (5)
                //================================
                // 还可以设置特定于通道实现的参数。
                // 我们正在编写一个TCP/IP服务器，所以我们可以设置套接字选项，比如tcpNoDelay和keepAlive。
                // 请参考ChannelOption的apidocs和具体的ChannelConfig实现，以了解受支持的ChannelOptions的概述。
                .option(ChannelOption.SO_BACKLOG, SO_BACKLOG) // (5)
                //================================
                // (6)
                //================================
                // 你注意到option()和childOption()了吗？
                // option()用于接受传入连接的NioServerSocketChannel。
                // childOption()用于父服务器通道接受的通道，在本例中是NioSocketChannel。
                .childOption(ChannelOption.SO_KEEPALIVE, SO_KEEPALIVE) // (6)
                //================================
                // (7)
                //================================
                // Nagle算法试图减少TCP包的数量和结构性开销, 将多个较小的包组合成较大的包进行发送
                // 这个算法受TCP延迟确认影响, 会导致相继两次向连接发送请求包,读数据时会有一个最多达500毫秒的延时.
                // TCP/IP协议中，无论发送多少数据，总是要在数据前面加上协议头，
                // 同时，对方接收到数据，也需要发送ACK表示确认。为了尽可能的利用网络带宽，
                // TCP总是希望尽可能的发送足够大的数据。（一个连接会设置MSS参数，
                // 因此，TCP/IP希望每次都能够以MSS尺寸的数据块来发送数据）。
                // Nagle算法就是为了尽可能发送大块数据，避免网络中充斥着许多小数据块。
                .childOption(ChannelOption.TCP_NODELAY, TCP_NODELAY) // (7)
        ;
        return serverBootstrap;
    }

    public void start() throws Exception {
        if (serverBootstrap != null && bossGroup != null && workerGroup != null) {
            try {
                //================================
                // (7)
                //================================
                // 绑定到端口并启动服务器。
                // 这里，我们绑定到机器中所有NIC（网络接口卡）的端口8080。
                // Bind and start to accept incoming connections.
                ChannelFuture f = serverBootstrap.bind(port).sync(); // (7)

                // Wait until the server socket is closed.
                // In this example, this does not happen, but you can do that to gracefully
                // shut down your server.
                f.channel().closeFuture().sync();
            } finally {
                workerGroup.shutdownGracefully().sync();
                bossGroup.shutdownGracefully().sync();
            }
        } else {
            if (serverBootstrap == null) {
                throw new NullPointerException("serverBootstrap未初始化，请检查！");
            } else if (bossGroup == null) {
                throw new NullPointerException("bossGroup未初始化，请检查！");
            } else {
                throw new NullPointerException("workerGroup未初始化，请检查！");
            }
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

    private String getRunParams() {
        return "[port：" + port + " [params：" + params;
    }
}
