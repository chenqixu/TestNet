package com.cqx.netty.sdtp.rule.impl;

import com.cqx.netty.sdtp.rule.AnnoRule;
import com.cqx.netty.sdtp.rule.IDefaultValue;
import com.cqx.common.utils.system.ByteUtil;

/**
 * 16进制
 *
 * @author chenqixu
 */
@AnnoRule
public class HexRule extends ByteRule {
    @Override
    protected String parser(byte[] data) {
        return ByteUtil.bytesToHexStringH(data);
    }

    @Override
    protected byte[] unParser(String data, int size) {
        return ByteUtil.hexStringToBytes(data);
    }

    @Override
    public String getName() {
        return "hex";
    }
}
