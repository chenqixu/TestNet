package com.cqx.netty.util;

import org.junit.Test;
import sun.net.util.IPAddressUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Random;

public class ByteUtilTest {
    private static Random random = new Random();
    private String path = "d:\\tmp\\data\\xdr\\";
    private int[] fieldsLenArray = {2, 8, 8, 8, 8, 2, 1, 1, 1, 3, 1, 4, 5, 1, 1, 1, 1, 1, 1, 2, 1, 4, 5, 2, 5, 1, 2, 1, 4, 2, 2, 1, 2, 4, 4, 5, 3, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1, 4};
    private byte[] head = {0x00};

    /**
     * Byte转Bit
     * <pre>
     *     由于byte是有符号的，所以高位表示符号位，剩下7位用来表示数值
     *     负数，是对原码取反，再加1，叫反码，在计算机中经过反码优化后，可以把减法变成加法
     *     比如2
     *     原码(有符号int)：00000000 00000000 00000000 00000010
     *     >>有符号右移
     *     >>>无符号右移
     *     首先将byte转化为int, 再行运算
     *
     *     (byte) ((b >> 7) & 0x1)
     *     b转成int，然后有符号右移7位，就是将第8位变成了第1位
     *     然后和0x1进行与运算，计算规则：1与1为1，1与0为0，这里的0x1就是 00000001
     *     b >> 7：00000000 00000000 00000000 00000010 变成 00000000 00000000 00000000 00000000
     *     (b >> 7) & 0x1：00000000 00000000 00000000 00000000 变成 00000000 00000000 00000000 00000000
     *     (byte) ((b >> 7) & 0x1)：00000000 00000000 00000000 00000000 变成 00000000
     *
     *     (byte) ((b >> 6) & 0x1)
     *     b转成int，然后有符号右移6位，就是将第7位变成了第1位
     *     然后和0x1进行与运算，计算规则：1与1为1，1与0为0，这里的0x1就是 00000001
     *     b >> 6：00000000 00000000 00000000 00000010 变成 00000000 00000000 00000000 00000000
     *     (b >> 6) & 0x1：00000000 00000000 00000000 00000000 变成 00000000 00000000 00000000 00000000
     *     (byte) ((b >> 6) & 0x1)：00000000 00000000 00000000 00000000 变成 00000000
     *
     *     ……
     *
     *     (byte) ((b >> 1) & 0x1)
     *     b转成int，然后有符号右移1位，就是将第2位变成了第1位
     *     然后和0x1进行与运算，计算规则：1与1为1，1与0为0，这里的0x1就是 00000001
     *     b >> 1：00000000 00000000 00000000 00000010 变成 00000000 00000000 00000000 00000001
     *     (b >> 1) & 0x1：00000000 00000000 00000000 00000001 变成 00000000 00000000 00000000 00000001
     *     (byte) ((b >> 1) & 0x1)：00000000 00000000 00000000 00000000 变成 00000001
     * </pre>
     */
    public static String byteToBit(byte b) {
        return "" + (byte) ((b >> 7) & 0x1) +
                (byte) ((b >> 6) & 0x1) +
                (byte) ((b >> 5) & 0x1) +
                (byte) ((b >> 4) & 0x1) +
                (byte) ((b >> 3) & 0x1) +
                (byte) ((b >> 2) & 0x1) +
                (byte) ((b >> 1) & 0x1) +
                (byte) ((b >> 0) & 0x1);
    }

    public static String bytesToBit(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(byteToBit(b));
        }
        return sb.toString();
    }

    /**
     * byte数值转short
     *
     * @param b
     * @return
     */
    public static short byte2short(byte[] b) {
        short l = 0;
        for (int i = 0; i < 2; i++) {
            l <<= 8; //<<=和我们的 +=是一样的，意思就是 l = l << 8
            // byte进行位运算，会先转成int
            // 所以这里符号位就变成了数值
            l |= (b[i] & 0xff); //和上面也是一样的  l = l | (b[i]&0xff)
        }
        return l;
    }

    /**
     * int到byte[] 由高位到低位
     *
     * @param i 需要转换为byte数组的整行值。
     * @return byte数组
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * 无符号byte
     *
     * @param b
     * @return
     */
    public static String unsignedByte(byte b) {
        byte[] unsignedArray = {0x00, b};
        return unsignedBytes(unsignedArray);
    }

    /**
     * 无符号short
     *
     * @param val
     * @return
     */
    public static String unsignedShort(short val) {
        return String.valueOf(Short.toUnsignedInt(val));
    }

    /**
     * 无符号int
     *
     * @param val
     * @return
     */
    public static String unsignedInt(int val) {
        return String.valueOf(Integer.toUnsignedLong(val));
    }

    /**
     * 字节数组转无符号
     *
     * @param bytes
     * @return
     */
    public static String unsignedBytes(byte[] bytes) {
        return new BigInteger(bytes).toString();
    }

    public static byte[] randomUnsignedNum(int len) {
        byte[] ret = new byte[len];
        random.nextBytes(ret);
        return ret;
    }

    public static byte[] arrayAdd(byte[] b1, byte[] b2, int b2Len) {
        byte[] n1 = new byte[b1.length + b2Len];
        System.arraycopy(b1, 0, n1, 0, b1.length);
        System.arraycopy(b2, 0, n1, b1.length, b2Len);
        return n1;
    }

    /**
     * 功能：判断一个IP是不是在一个网段下的
     * 格式：isInRange("192.168.8.3", "192.168.9.10/22");
     *
     * @param ip
     * @param cidr
     * @return
     */
    public static boolean isInRange(String ip, String cidr) {
        String[] ips = ip.split("\\.");
        int ipAddr = (Integer.parseInt(ips[0]) << 24)
                | (Integer.parseInt(ips[1]) << 16)
                | (Integer.parseInt(ips[2]) << 8)
                | Integer.parseInt(ips[3]);
        int type = Integer.parseInt(cidr.replaceAll(".*/", ""));
        int mask = 0xFFFFFFFF << (32 - type);
        String cidrIp = cidr.replaceAll("/.*", "");
        String[] cidrIps = cidrIp.split("\\.");
        int cidrIpAddr = (Integer.parseInt(cidrIps[0]) << 24)
                | (Integer.parseInt(cidrIps[1]) << 16)
                | (Integer.parseInt(cidrIps[2]) << 8)
                | Integer.parseInt(cidrIps[3]);
        return (ipAddr & mask) == (cidrIpAddr & mask);
    }

    /**
     * 功能：判断一个IP是不是在一个网段下的</br>
     * 格式：isInRange("192.168.8.3", "192.168.9.10", 22);
     *
     * @param srcIp         数据里的IP
     * @param conditionIp   查询条件IP
     * @param conditionMask 查询条件IP的掩码位数
     * @return
     * @throws UnknownHostException
     */
    public static boolean isInRange(String srcIp, String conditionIp, int conditionMask) throws UnknownHostException {
        // 先判断是IPv4还是IPv6
        boolean isIPv4 = IPAddressUtil.isIPv4LiteralAddress(srcIp);
        boolean isIPv6 = IPAddressUtil.isIPv6LiteralAddress(srcIp);
        System.out.println(String.format("isIPv4：%s，isIPv6：%s", isIPv4, isIPv6));
        if (isIPv4) {
            InetAddress query_host = InetAddress.getByName(srcIp);
            InetAddress source_host = InetAddress.getByName(conditionIp);
            // 生成掩码
            int mask = 0xFFFFFFFF << (32 - conditionMask);
            int query = new BigInteger(query_host.getAddress()).intValue();
            int source = new BigInteger(source_host.getAddress()).intValue();
            // 判断和掩码与操作后的两个子网结果是否一致
            return (query & mask) == (source & mask);
        } else if (isIPv6) {
            InetAddress query_host = InetAddress.getByName(srcIp);
            InetAddress source_host = InetAddress.getByName(conditionIp);
            // 生成BitSet
            BitSet qbs = bytesToBitSet(query_host.getAddress());
            BitSet sbs = bytesToBitSet(source_host.getAddress());
            // 生成掩码
            BitSet mask = new BitSet();
            for (int i = 0; i < conditionMask; i++) {
                mask.set(i, true);
            }
            qbs.and(mask);// 和掩码做与操作
            sbs.and(mask);// 和掩码做与操作
            // 判断和掩码与操作后的两个子网结果是否一致
            return qbs.equals(sbs);
        }
        return false;
    }

    public static byte[] bitSet2ByteArray(BitSet bitSet) {
        byte[] bytes = new byte[bitSet.size() / 8];
        for (int i = 0; i < bitSet.size(); i++) {
            int index = i / 8;
            int offset = 7 - i % 8;
            bytes[index] |= (bitSet.get(i) ? 1 : 0) << offset;
        }
        return bytes;
    }

    public static BitSet byteArray2BitSet(byte[] bytes) {
        BitSet bitSet = new BitSet(bytes.length * 8);
        int index = 0;
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 7; j >= 0; j--) {
                bitSet.set(index++, (bytes[i] & (1 << j)) >> j == 1 ? true : false);
            }
        }
        return bitSet;
    }

    /**
     * byte数组转BitSet
     *
     * @param bytes byte数组
     * @return
     */
    public static BitSet bytesToBitSet(byte[] bytes) {
        BitSet bs = new BitSet(bytes.length * 8);
        for (int i = 0; i < bytes.length; i++) {
            bs.set(i * 8, ((byte) ((bytes[i] >> 7) & 0x1)) == 1);
            bs.set(i * 8 + 1, ((byte) ((bytes[i] >> 6) & 0x1)) == 1);
            bs.set(i * 8 + 2, ((byte) ((bytes[i] >> 5) & 0x1)) == 1);
            bs.set(i * 8 + 3, ((byte) ((bytes[i] >> 4) & 0x1)) == 1);
            bs.set(i * 8 + 4, ((byte) ((bytes[i] >> 3) & 0x1)) == 1);
            bs.set(i * 8 + 5, ((byte) ((bytes[i] >> 2) & 0x1)) == 1);
            bs.set(i * 8 + 6, ((byte) ((bytes[i] >> 1) & 0x1)) == 1);
            bs.set(i * 8 + 7, ((byte) ((bytes[i] >> 0) & 0x1)) == 1);
        }
        return bs;
    }

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
        System.out.println(byteToBit((byte) 127));
        System.out.println(byteToBit((byte) -1));

        // 11111111
        // 有符号：去掉符号位，减1，1111110，取反，0000001，原码（补上符号位）：10000001，结果：-1
        // 无符号：变成short int，占16-bit，00000000 11111111，应该是255
        // 0xFF 是 11111111，0x00 是 00000000
        byte[] shortInt = {(byte) 0x00, (byte) -1};
        System.out.println(byte2short(shortInt));
        short i = ((byte) -1) & 0xff;
        System.out.println(i);

        // 11111111 11111111
        // 无符号：65535
        System.out.println(Short.toUnsignedInt((short) -1));
        System.out.println(Integer.parseUnsignedInt(bytesToBit(new byte[]{-1, -1}), 2));
        // 有符号：-1
        // 计算过程：去符号位，减1->1111111 11111110，取反->0000000 00000001，带符号位->10000000 00000001

        // 11111111
        byte[] unsignedInt1 = {0x00, (byte) -1};
        System.out.println(unsignedBytes(unsignedInt1));

        // 11111111 11111111
        byte[] unsignedInt2 = {0x00, (byte) -1, (byte) -1};
        System.out.println(unsignedBytes(unsignedInt2));

        // 11111111 11111111 11111111 11111111
        byte[] unsignedInt4 = {0x00, (byte) -1, (byte) -1, (byte) -1, (byte) -1};
        System.out.println(unsignedBytes(unsignedInt4));

        // 11111111 11111111 11111111 11111111 11111111 11111111 11111111 11111111
        byte[] unsignedInt8 = {0x00, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1};
        System.out.println(unsignedBytes(unsignedInt8));
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
                    fos.write(randomUnsignedNum(fieldLen));
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
                        byte[] newBytes = arrayAdd(head, bytes, readLen);
                        String value = unsignedBytes(newBytes);
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
            System.out.println(unsignedByte(b));
        }
        int query = new BigInteger(query_host.getAddress()).intValue();
        int source = new BigInteger(source_host.getAddress()).intValue();
        System.out.println((query & mask) == (source & mask));
        System.out.println(isInRange(source_ip, query_ip + "/" + query_ip_mask));
    }

    @Test
    public void ipv6Mask() throws UnknownHostException {
        String query_ip = "234e:0:4567::3d";
        int query_ip_mask = 16;
        String source_ip = "234e:3:4567:0::3a";

        System.out.println("判断：" + isInRange(source_ip, query_ip, query_ip_mask));

        InetAddress query_host = InetAddress.getByName(query_ip);
        InetAddress source_host = InetAddress.getByName(source_ip);
        System.out.println(query_host);
        System.out.println(source_host);
//        BitSet qbs = byteArray2BitSet(query_host.getAddress());
//        BitSet sbs = byteArray2BitSet(source_host.getAddress());
        byte[] qbsArray = query_host.getAddress();
        System.out.println("query_host第一个byte：" + byteToBit(qbsArray[0]));
        ByteBuffer bb = ByteBuffer.wrap(qbsArray);
        System.out.println("ByteBuffer bb第一个byte：" + byteToBit(bb.get(0)));
        BitSet qbs = BitSet.valueOf(query_host.getAddress());
        BitSet sbs = BitSet.valueOf(source_host.getAddress());
        BitSet qbs1 = byteArray2BitSet(query_host.getAddress());
        BitSet qbs2 = bytesToBitSet(query_host.getAddress());
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
        System.out.println("无符号：" + unsignedInt(mask));
        String binaryString = Integer.toBinaryString(mask);
        System.out.println("转二进制：" + binaryString);
        System.out.println("二进制转十进制，使用Long来存储：" + Long.parseLong(binaryString, 2));
    }
}