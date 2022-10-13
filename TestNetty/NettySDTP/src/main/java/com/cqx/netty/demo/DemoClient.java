package com.cqx.netty.demo;

import com.cqx.netty.util.SocketClient;

import java.io.IOException;
import java.io.InputStream;

/**
 * DemoClient
 *
 * @author chenqixu
 */
public class DemoClient {

    public static void main(String[] args) {
        // 服务ip
        String ip = args[0];
        // 服务端口
        int port = Integer.valueOf(args[1]);

    }

    private void start(String ip, int port) throws IOException {
        // jdk8语法糖自动释放
        try (SocketClient socketClient = SocketClient.newbuilder()
                .setIp(ip)
                .setPort(port)
                .build()) {
            ClientReceive clientReceive = new ClientReceive();
            socketClient.send(new byte[]{});
            socketClient.receive(clientReceive);
        }
    }

    class ClientReceive implements SocketClient.ReceiveCall {

        @Override
        public byte[] read(InputStream in) throws IOException {
            return new byte[]{};
        }
    }
}
