package com.cqx.netty.sdtp.rule.impl;

import com.cqx.netty.sdtp.rule.AnnoRule;
import com.cqx.netty.sdtp.rule.IDefaultValue;
import com.cqx.netty.util.ByteUtil;

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
    public byte[] reverse(String data, IDefaultValue iDefaultValue, int size) {
        // 先判断是否为空，为空就走默认值
        if (iDefaultValue.isNull(data)) {
            return iDefaultValue.getDefaultByteValue(size);
        }
        return ByteUtil.hexStringToBytes(data);
    }

    @Override
    public String getName() {
        return "hex";
    }
}
