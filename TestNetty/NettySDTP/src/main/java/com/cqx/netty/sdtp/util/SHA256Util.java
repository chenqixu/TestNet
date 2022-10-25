package com.cqx.netty.sdtp.util;

import io.netty.buffer.ByteBufUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SHA256Util {
    private static final Logger logger = LoggerFactory.getLogger(SHA256Util.class);

    public SHA256Util() {
    }

    public static byte[] getSHA256(String s) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(s.getBytes(StandardCharsets.UTF_8));
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException var3) {
            logger.error(var3.getMessage(), var3);
            return null;
        }
    }

    /**
     * 链路认证计算值
     *
     * @param loginID      登录账户
     * @param sharedSecret 密码
     * @param timestamp    时间戳
     * @param rand         随机数
     * @return
     */
    public static byte[] computerDigest(String loginID, String sharedSecret, Long timestamp, Long rand) {
        String loginWithPad = StringUtils.rightPad(loginID, 12, " ");
        byte[] bytesSharedSecret = SHA256Util.getSHA256(sharedSecret);
        if (bytesSharedSecret == null) throw new NullPointerException(sharedSecret + "的SHA256计算异常！");
        String hexSharedSecret = ByteBufUtil.hexDump(bytesSharedSecret);
        logger.info("☆☆☆ SharedSecret Digest: {}", hexSharedSecret);

        String strBeforeDigest = loginWithPad +
                hexSharedSecret +
                timestamp +
                "rand=" +
                rand;
        logger.info("☆☆☆ String Before Digest: {}", strBeforeDigest);

        // 前32个字节的摘要
        byte[] bytesDigestPart = SHA256Util.getSHA256(strBeforeDigest);
        if (bytesDigestPart == null) throw new NullPointerException(strBeforeDigest + "的SHA256计算异常！");
        // 64个字节的摘要
        byte[] bytesDigests = new byte[64];
        Arrays.fill(bytesDigests, (byte) 0x00);
        System.arraycopy(bytesDigestPart, 0, bytesDigests, 0, 32);

        return bytesDigests;
    }
}
