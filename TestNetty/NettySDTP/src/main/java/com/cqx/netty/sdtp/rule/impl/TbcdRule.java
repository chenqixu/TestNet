package com.cqx.netty.sdtp.rule.impl;

import com.cqx.netty.sdtp.rule.AnnoRule;
import com.cqx.netty.sdtp.rule.IDefaultValue;
import com.cqx.common.utils.system.ByteUtil;

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
    protected byte[] unParser(String data, int size) {
        throw new UnsupportedOperationException("不支持TBCD的反向操作！");
    }

    @Override
    public String getName() {
        return "tbcd";
    }
}
