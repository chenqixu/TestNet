package com.cqx.netty.service.redis;

import com.cqx.netty.util.IServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.HashMap;
import java.util.Map;

/**
 * 魔百盒Redis缓存服务
 * <pre>
 *     1、定时缓存Redis数据到内存
 *     2、提供查询服务
 * </pre>
 *
 * @author chenqixu
 */
public class MobileboxServer {

    public void start(int port) throws Exception {
        Map<String, String> params = new HashMap<>();
        IServer iServer = IServer.newbuilder();
        iServer.setPort(port)
                .setParams(params)
                .buildBootstrap()
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("MobileboxServer", new MobileboxServerHandler());
                    }
                });
        iServer.start();
    }

}
