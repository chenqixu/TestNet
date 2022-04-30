package com.cqx.netty.sdtp.rule.impl;

import com.cqx.netty.sdtp.rule.AnnoRule;
import com.cqx.netty.sdtp.rule.IDefaultValue;
import com.cqx.netty.util.ByteUtil;

/**
 * BCD转ASC
 *
 * @author chenqixu
 */
@AnnoRule
public class Bcd2AscRule extends ByteRule {
    @Override
    protected String parser(byte[] data) {
        // BCD转ASC
        return ByteUtil.BCD2ASC(data);
    }

    @Override
    public byte[] reverse(String data, IDefaultValue iDefaultValue, int size) {
        return null;
    }

    @Override
    public String getName() {
        return "bcd2asc";
    }
}
