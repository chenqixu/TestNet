package com.cqx.netty.sdtp.service;

import com.cqx.netty.util.IServer;
import com.cqx.netty.util.IServerHandler;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

/**
 * SDTPServer
 *
 * @author chenqixu
 */
public class SDTPServer {

    public static void main(String[] args) throws Exception {
        SDTPServer sdtpServer = new SDTPServer();
        sdtpServer.startServer();
    }

    private void startServer() throws Exception {
        Map<String, String> params = new HashMap<>();
        IServer.newbuilder()
                .setPort(8007)
                .setParams(params)
                .addSocketChannel(getHandler())
                .start();
    }

    private SDTPServerHandler getHandler() {
        return new SDTPServerHandler();
    }

    class SDTPServerHandler extends IServerHandler {

        @Override
        protected void init() {

        }

        @Override
        protected ByteBuf dealHandler(ByteBuf buf) {
            return null;
        }
    }
}
