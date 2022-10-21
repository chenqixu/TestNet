package com.cqx.netty.sdtp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
}
