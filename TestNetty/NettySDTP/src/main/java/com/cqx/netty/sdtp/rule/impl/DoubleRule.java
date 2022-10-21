package com.cqx.netty.sdtp.rule.impl;

import com.cqx.common.utils.system.ByteUtil;
import com.cqx.netty.sdtp.rule.AnnoRule;

/**
 * DoubleRule
 *
 * @author chenqixu
 */
@AnnoRule
public class DoubleRule extends ByteRule {
    @Override
    protected String parser(byte[] data) {
        return String.valueOf(Double.longBitsToDouble(ByteUtil.unsignedBytesToLong((data))));
    }

    @Override
    protected byte[] unParser(String data, int size) {
        return ByteUtil.numberToBytes(String.valueOf(Double.doubleToLongBits(Double.valueOf(data))), size);
    }

    @Override
    public String getName() {
        return "double";
    }
}
