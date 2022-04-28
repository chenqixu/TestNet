package com.cqx.netty.sdtp.rule.impl;

import com.cqx.netty.sdtp.rule.AnnoRule;
import com.cqx.netty.util.ByteUtil;

/**
 * TBCD规则
 *
 * @author chenqixu
 */
@AnnoRule
public class TbcdRule extends ByteRule {
    @Override
    protected String parser(byte[] data) {
        // TBCD解码
        return ByteUtil.getTBCD(data, data.length);
    }

    @Override
    public byte[] reverse(String data) {
        return null;
    }

    @Override
    public String getName() {
        return "tbcd";
    }
}
