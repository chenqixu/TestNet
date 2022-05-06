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
    protected byte[] unParser(String data, int size) {
        throw new UnsupportedOperationException("不支持BCD转ASC的反向操作！");
    }

    @Override
    public String getName() {
        return "bcd2asc";
    }
}
