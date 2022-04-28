package com.cqx.netty.util;

import com.cqx.netty.example.echo.LinkedQueue;
import com.cqx.netty.example.echo.SDTPLinkedQueue;
import com.cqx.netty.service.redis.MobileboxServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class UtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(UtilsTest.class);
    private Utils utils;

    @Before
    public void setUp() {
        utils = new Utils();
    }

    @Test
    public void readBuf() throws Exception {
        ByteBuf buf = Unpooled.buffer(100);
        buf.writeInt(1);
        buf.writeInt(2);
        buf.writeShort(65535);// 只写低位，高位会抛掉
        // 01111111
        byte signedByteMax = Byte.valueOf("01111111", 2);
        logger.info("有符号byte最大值：{}", ByteUtil.unsignedBytes(new byte[]{signedByteMax}));
        // 11111111 11111111
        byte[] unsignedShortMax = {0x00, (byte) -1, (byte) -1};
        logger.info("无符号short最大值：{}", ByteUtil.unsignedBytes(unsignedShortMax));
        // 01111111 11111111
        byte[] signedShortMax = {(byte) 127, (byte) -1};
        logger.info("有符号short最大值：{}", ByteUtil.unsignedBytes(signedShortMax));
        // 使用ByteBuffer来封装数值对应的byte[]
        ByteBuffer byteBuffer = ByteBuffer.allocate(Short.BYTES);
        short s = 32767;
        byteBuffer.putShort(s);
        for (Byte b : byteBuffer.array()) {
            logger.info("{}", b);
        }

        // unsigned byte，short，int，long转byte[]
        // 先升级，在吐到ByteBuffer，最后再截取，但是比Long大的如何处理？
        // 要么就是使用位运算
        // 比如没有超过限制的，可以位运算，如果超过上限的，代码里可能表述不了，需要升级

        int a = buf.readInt();
        int b = buf.readInt();
        int index = buf.readerIndex();
        int unsignedShort = buf.getUnsignedShort(index);
        logger.info(String.format("a: %s, b: %s, index: %s, unsignedShort: %s"
                , a, b, index, unsignedShort));

        logger.info("0xff：{}", 0xff);

        BigInteger bigInteger = new BigInteger("1000");
        logger.info("源：{}", bigInteger);
        for (byte _b : bigInteger.toByteArray()) {
            logger.info("逐个分解：{}", _b);
        }
        logger.info("还原：{}", ByteUtil.unsignedBytes(bigInteger.toByteArray()));

        // ipv4
        logger.info("ipv4 len : {}", InetAddress.getByName("10.1.2.212").getAddress().length);

        // ipv6
        logger.info("ipv6 len : {}", InetAddress.getByName("234e:3:4567:0::3").getAddress().length);
    }

    @Test
    public void writeBuf() {
        utils.writeBuf();
    }

    @Test
    public void classTest() throws InstantiationException, IllegalAccessException {
        utils.classTest();
    }

    @Test
    public void genrate() throws Exception {
        //非内部类
        logger.info("{}", Utils.genrate(MobileboxServerHandler.class, null));
        //内部类
        logger.info("{}", Utils.genrate(TestIServerHandler.class, null));
    }

    @Test
    public void linked() throws Exception {
        SDTPLinkedQueue sdtpLinkedQueue = new SDTPLinkedQueue(3);
        LinkedQueue currentLQ;
        while ((currentLQ = sdtpLinkedQueue.next()) != null) {
            logger.info("next : {}", currentLQ);
            Thread.sleep(200);
        }
    }

    class TestIServerHandler extends IServerHandler {
        @Override
        protected void init() {
        }

        @Override
        protected ByteBuf dealHandler(ByteBuf buf) {
            return null;
        }
    }
}