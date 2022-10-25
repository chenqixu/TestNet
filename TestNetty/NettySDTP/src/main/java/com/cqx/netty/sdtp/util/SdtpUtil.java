package com.cqx.netty.sdtp.util;

import io.netty.buffer.ByteBufUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * SdtpUtil
 *
 * @author chenqixu
 */
public class SdtpUtil {
    public static final String LineENd = "\r\n";
    public static final String LoginID = "simple";
    public static final String sharedSecret = "123456";
    public static final String COMMON_RULE = "Length-L2-byte-F,LocalProvince-L2-byte-F,LocalCity-L2-byte-F,OwnerProvince-L2-byte-F,OwnerCity-L2-byte-F,RoamingType-L1-byte-F,Interface-L1-byte-F,ProbeID-L2-byte-F,XDRID-L16-hex-F,RAT-L1-byte-F,IMSI-L8-byte-F,PEI-L8-byte-F,MSISDN-L16-byte-F,";
    public static final String COMMON_NOLENGTH_RULE = "LocalProvince-L2-byte-F,LocalCity-L2-byte-F,OwnerProvince-L2-byte-F,OwnerCity-L2-byte-F,RoamingType-L1-byte-F,Interface-L1-byte-F,ProbeID-L2-byte-F,XDRID-L16-hex-F,RAT-L1-byte-F,IMSI-L8-byte-F,PEI-L8-byte-F,MSISDN-L16-byte-F,";
    public static final String N14_RULE = "ProcedureType-L1-byte-F,procedurestarttimeonForwardingGW-L8-byte-N0,procedureendtimeonForwardingGW-L8-byte-N0,procedurestarttimeonProbe-L8-byte-N0,procedureendtimeonProbe-L8-byte-N0,ProcedureStatus-L1-byte-F,httpreqtype-L1-byte-F,statuscode-L2-byte-F,FailureCause-L2-byte-F,SourceAMFAddress-LV-ip-F,DestinationAMFAddress-LV-ip-F,SourceAMFPort-L2-byte-F,DestinationAMFPort-L2-byte-F,Subproceduretype-L1-byte-F,RequestCause-L1-byte-F,USER_IPv4-T0L4-ip-F,USER_IPv6-T1L16-ip-F,TransferReason-T2L2-byte-F,pdusessionid-T3L1-byte-F,DNN-T4LV-string-F,TAC-T5L2-byte-F,CellID-T6L4-byte-F,accesstype-T7L1-byte-F,locationtype-T8L1-byte-F";
    private static final Logger logger = LoggerFactory.getLogger(SdtpUtil.class);

    /**
     * 链路认证计算值
     *
     * @param loginID
     * @param sharedSecret
     * @param timestamp
     * @param rand
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
