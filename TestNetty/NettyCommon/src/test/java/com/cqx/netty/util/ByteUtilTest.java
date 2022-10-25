package com.cqx.netty.util;

import com.cqx.common.utils.system.ArraysUtil;
import com.cqx.common.utils.system.NetUtil;
import org.junit.Test;
import sun.misc.Unsafe;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;
import java.util.Random;

import com.cqx.common.utils.system.ByteUtil;

public class ByteUtilTest {
    private static Random random = new Random();
    private String path = "d:\\tmp\\data\\xdr\\";
    private int[] fieldsLenArray = {2, 8, 8, 8, 8, 2, 1, 1, 1, 3, 1, 4, 5, 1, 1, 1, 1, 1, 1, 2, 1, 4, 5, 2, 5, 1, 2, 1, 4, 2, 2, 1, 2, 4, 4, 5, 3, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, 4};
    private byte[] head = {0x00};

    @Test
    public void test() throws Exception {
        int n = 1234;
        ByteBuffer bf = ByteBuffer.allocate(4);
        bf.putInt(n);
        bf.flip();
        byte[] ret = new byte[4];
        bf.get(ret);
        for (byte b : ret) {
            System.out.println(b);
        }

        String result = Integer.toBinaryString(0x1);
        System.out.println(result);
        //-1234
        //11111111 11111111 11111011 00101110
        //1234
        //00000000 00000000 00000100 11010010

//        byte[] bb = new byte[]{0x00, (byte) 2};
//        byte b = (byte) 2;
//        System.out.println(Integer.toBinaryString(b & 0xFF));
        System.out.println(ByteUtil.byteToBit((byte) 127));
        System.out.println(ByteUtil.byteToBit((byte) -1));

        // 11111111
        // 有符号：去掉符号位，减1，1111110，取反，0000001，原码（补上符号位）：10000001，结果：-1
        // 无符号：变成short int，占16-bit，00000000 11111111，应该是255
        // 0xFF 是 11111111，0x00 是 00000000
        byte[] shortInt = {(byte) 0x00, (byte) -1};
        System.out.println(ByteUtil.byte2short(shortInt));
        short i = ((byte) -1) & 0xff;
        System.out.println(i);

        // 11111111 11111111
        // 无符号：65535
        System.out.println(Short.toUnsignedInt((short) -1));
        System.out.println(Integer.parseUnsignedInt(ByteUtil.bytesToBit(new byte[]{-1, -1}), 2));
        // 有符号：-1
        // 计算过程：去符号位，减1->1111111 11111110，取反->0000000 00000001，带符号位->10000000 00000001

        // 11111111
        byte[] unsignedInt1 = {0x00, (byte) -1};
        System.out.println(ByteUtil.unsignedBytes(unsignedInt1));

        // 11111111 11111111
        byte[] unsignedInt2 = {0x00, (byte) -1, (byte) -1};
        System.out.println(ByteUtil.unsignedBytes(unsignedInt2));

        // 11111111 11111111 11111111 11111111
        byte[] unsignedInt4 = {0x00, (byte) -1, (byte) -1, (byte) -1, (byte) -1};
        System.out.println(ByteUtil.unsignedBytes(unsignedInt4));

        // 11111111 11111111 11111111 11111111 11111111 11111111 11111111 11111111
        byte[] unsignedInt8 = {0x00, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1};
        System.out.println(ByteUtil.unsignedBytes(unsignedInt8));
    }

    /**
     * 随机无符号数值写入文件，不换行
     *
     * @throws Exception
     */
    @Test
    public void unsignedToFile() throws Exception {
        try (FileOutputStream fos = new FileOutputStream(path + "unsigned.byte")) {
            int count = 10000;
            while (count > 0) {
                for (int fieldLen : fieldsLenArray) {
                    // 随机一个byte数组
                    fos.write(ByteUtil.randomUnsignedNum(fieldLen));
                }
                // 回车换行
//                fos.write("\r\n".getBytes());
                count--;
            }
        }
    }

    /**
     * 从文件中按字节读取无符号数值，统计解析效率
     *
     * @throws Exception
     */
    @Test
    public void readUnsignedFile() throws Exception {
        for (int i = 0; i < 10; i++) {
            try (FileInputStream fis = new FileInputStream(path + "unsigned.byte")) {
                int count = 10000;
                long start = System.currentTimeMillis();
                while (count > 0) {
                    for (int fieldLen : fieldsLenArray) {
                        byte[] bytes = new byte[8];
                        int readLen = fis.read(bytes, 0, fieldLen);
                        byte[] newBytes = ArraysUtil.arrayAdd(head, bytes, readLen);
                        String value = ByteUtil.unsignedBytes(newBytes);
//                        System.out.println(value);
                    }
//                    break;
                    count--;
                }
                long end = System.currentTimeMillis();
                System.out.println(String.format("cost : %s ms", end - start));
            }
        }
    }

    @Test
    public void ipv4Mask() throws UnknownHostException {
        // B类网段：255.255.0.0
        // 网络号，主机号
        // 主机号：00000000 00000000
        // 网络号，子网号，主机号
        // 子网号两位
        // 00 01 10 11 = 2的2次方-2 = 2
        // 需要除去全0和全1的情况
        // 子网号三位
        // 000 001 010 011 100 101 110 111 = 2的3次方-2 = 6
        String query_ip = "224.168.1.1";
        int query_ip_mask = 8;
        String source_ip = "224.179.3.0";
        InetAddress query_host = InetAddress.getByName(query_ip);
        InetAddress source_host = InetAddress.getByName(source_ip);
        int mask = 0xFFFFFFFF << (32 - query_ip_mask);
        System.out.println(Integer.toBinaryString(mask));
        for (byte b : query_host.getAddress()) {
            System.out.println(ByteUtil.unsignedByte(b));
        }
        int query = new BigInteger(query_host.getAddress()).intValue();
        int source = new BigInteger(source_host.getAddress()).intValue();
        System.out.println((query & mask) == (source & mask));
        System.out.println(NetUtil.isInRange(source_ip, query_ip + "/" + query_ip_mask));
    }

    @Test
    public void ipv6Mask() throws UnknownHostException {
        String query_ip = "234e:0:4567::3d";
        int query_ip_mask = 16;
        String source_ip = "234e:3:4567:0::3a";

        System.out.println("判断：" + NetUtil.isInRange(source_ip, query_ip, query_ip_mask));

        InetAddress query_host = InetAddress.getByName(query_ip);
        InetAddress source_host = InetAddress.getByName(source_ip);
        System.out.println(query_host);
        System.out.println(source_host);
//        BitSet qbs = byteArray2BitSet(query_host.getAddress());
//        BitSet sbs = byteArray2BitSet(source_host.getAddress());
        byte[] qbsArray = query_host.getAddress();
        System.out.println("query_host第一个byte：" + ByteUtil.byteToBit(qbsArray[0]));
        ByteBuffer bb = ByteBuffer.wrap(qbsArray);
        System.out.println("ByteBuffer bb第一个byte：" + ByteUtil.byteToBit(bb.get(0)));
        BitSet qbs = BitSet.valueOf(query_host.getAddress());
        BitSet sbs = BitSet.valueOf(source_host.getAddress());
        BitSet qbs1 = ByteUtil.byteArray2BitSet(query_host.getAddress());
        BitSet qbs2 = ByteUtil.bytesToBitSet(query_host.getAddress());
        System.out.println("qbs1：" + qbs1 + "，" + qbs1.size());
        System.out.println("qbs2：" + qbs2 + "，" + qbs2.size());
        System.out.println("qbs：" + qbs + "，" + qbs.size());
        System.out.println("qbs1.equals(qbs)：" + qbs1.equals(qbs));
        System.out.print("qbs1第一个byte：");
        for (int i = 0; i < 8; i++) {
            System.out.print(qbs1.get(i) ? 1 : 0);
        }
        System.out.println();
        System.out.print("qbs2第一个byte：");
        for (int i = 0; i < 8; i++) {
            System.out.print(qbs2.get(i) ? 1 : 0);
        }
        System.out.println();
        System.out.print("qbs第一个byte：");
        for (int i = 0; i < 8; i++) {
            System.out.print(qbs.get(i) ? 1 : 0);
        }
        System.out.println();

        System.out.println("sbs：" + sbs + "，" + sbs.size());
        BitSet mask = new BitSet();
        for (int i = 0; i < query_ip_mask; i++) {
            mask.set(i, true);
        }
        qbs.and(mask);
        sbs.and(mask);
        System.out.println("qbs.equals(sbs)：" + qbs.equals(sbs));

        for (int i = 0; i < 64; i++) {
            System.out.print(1);
        }
        for (int i = 0; i < 64; i++) {
            System.out.print(0);
        }
    }

    @Test
    public void mask() {
        int mask = 0xFFFFFFFF << (32 - 8);
        System.out.println("有符号：" + mask);
        System.out.println("无符号：" + ByteUtil.unsignedInt(mask));
        String binaryString = Integer.toBinaryString(mask);
        System.out.println("转二进制：" + binaryString);
        System.out.println("二进制转十进制，使用Long来存储：" + Long.parseLong(binaryString, 2));
    }

    /**
     * 字节序查看
     */
    @Test
    public void byteOrderTest() throws Exception {
        // 方式1
        System.out.println(String.format("本机字节序：%s", ByteOrder.nativeOrder()));
        // 方式2
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Unsafe unsafe = (Unsafe) field.get(null);
        long a = unsafe.allocateMemory(8);
        try {
            unsafe.putLong(a, 0x0102030405060708L);
            byte b = unsafe.getByte(a);
            switch (b) {
                case 0x01:
                    System.out.println("本机字节序：大端字节序");
                    break;
                case 0x08:
                    System.out.println("本机字节序：小端字节序");
                    break;
                default:
                    assert false;
            }
        } finally {
            unsafe.freeMemory(a);
        }
    }

    /**
     * 小孩上楼梯，一次可能可以走1层、2层或者3层，请问，针对n层楼梯，可能有几种走法？<br>
     * 思路：最后一步可能走n-1、n-2、n-3
     */
    @Test
    public void getWayTest() {
        System.out.println(getWay(3));
    }

    private int getWay(int n) {
        if (n < 0) {
            return 0;
        } else if (n == 0) {
            return 1;
        } else {
            return getWay(n - 1) + getWay(n - 2) + getWay(n - 3);
        }
    }
}