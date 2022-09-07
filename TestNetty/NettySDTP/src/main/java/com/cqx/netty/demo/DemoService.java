package com.cqx.netty.demo;

import com.cqx.netty.util.IServer;
import com.cqx.netty.util.IServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.HashMap;
import java.util.Map;

/**
 * DemoService
 *
 * @author chenqixu
 */
public class DemoService {

    public static void main(String[] args) throws Exception {
        // 服务端口
        int port = Integer.valueOf(args[0]);
        new DemoService().start(port);
    }

    private void start(int port) throws Exception {
        // 空参数
        Map<String, String> params = new HashMap<>();
        IServer iServer = IServer.newbuilder();
        iServer.setPort(port)
                .setParams(params)
                .buildBootstrap()
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("DemoServer", new DemoServerHandler());
                    }
                });
        iServer.start();
    }

    class DemoServerHandler extends IServerHandler {

        @Override
        protected ByteBuf dealHandler(ByteBuf buf) {

            return null;
        }
    }
}
