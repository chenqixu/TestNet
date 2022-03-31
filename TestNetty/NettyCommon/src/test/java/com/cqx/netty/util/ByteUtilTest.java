package com.cqx.netty.util;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
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

    public static String parserUnsigned(byte[] bytes) {
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
        System.out.println(parserUnsigned(unsignedInt1));

        // 11111111 11111111
        byte[] unsignedInt2 = {0x00, (byte) -1, (byte) -1};
        System.out.println(parserUnsigned(unsignedInt2));

        // 11111111 11111111 11111111 11111111
        byte[] unsignedInt4 = {0x00, (byte) -1, (byte) -1, (byte) -1, (byte) -1};
        System.out.println(parserUnsigned(unsignedInt4));

        // 11111111 11111111 11111111 11111111 11111111 11111111 11111111 11111111
        byte[] unsignedInt8 = {0x00, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1};
        System.out.println(parserUnsigned(unsignedInt8));
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
                        String value = parserUnsigned(newBytes);
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
}