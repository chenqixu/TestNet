package com.cqx.netty.sdtp.rule.impl;

import com.cqx.netty.sdtp.rule.AnnoRule;
import com.cqx.netty.util.ByteUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ip规则
 *
 * @author chenqixu
 */
@AnnoRule
public class IpRule extends ByteRule {
    @Override
    protected String parser(byte[] data) {
        // 如果是4字节就是ipv4
        // 如果是16字节就是ipv6
        StringBuilder sb = new StringBuilder(40);
        if (data.length == 4) {
            // 按点十分进制
            sb.append(ByteUtil.bytesToShortH(new byte[]{0x00, data[0]}))
                    .append(".")
                    .append(ByteUtil.bytesToShortH(new byte[]{0x00, data[1]}))
                    .append(".")
                    .append(ByteUtil.bytesToShortH(new byte[]{0x00, data[2]}))
                    .append(".")
                    .append(ByteUtil.bytesToShortH(new byte[]{0x00, data[3]}));
        } else {
            // 按冒分十六进制
            sb.append(ByteUtil.bytesToHexStringH(new byte[]{data[0], data[1]}))
                    .append(":")
                    .append(ByteUtil.bytesToHexStringH(new byte[]{data[2], data[3]}))
                    .append(":")
                    .append(ByteUtil.bytesToHexStringH(new byte[]{data[4], data[5]}))
                    .append(":")
                    .append(ByteUtil.bytesToHexStringH(new byte[]{data[6], data[7]}))
                    .append(":")
                    .append(ByteUtil.bytesToHexStringH(new byte[]{data[8], data[9]}))
                    .append(":")
                    .append(ByteUtil.bytesToHexStringH(new byte[]{data[10], data[11]}))
                    .append(":")
                    .append(ByteUtil.bytesToHexStringH(new byte[]{data[12], data[13]}))
                    .append(":")
                    .append(ByteUtil.bytesToHexStringH(new byte[]{data[14], data[15]}));
        }
        return sb.toString();
    }

    @Override
    public byte[] reverse(String data) {
        try {
            return InetAddress.getByName(data).getAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getName() {
        return "ip";
    }
}
