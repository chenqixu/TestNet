package com.cqx.netty.sdtp.rule.impl;

import com.cqx.netty.sdtp.rule.AnnoRule;
import com.cqx.netty.sdtp.rule.IDefaultValue;
import com.cqx.netty.sdtp.rule.IRule;
import com.cqx.netty.util.ByteUtil;

import java.math.BigInteger;

/**
 * byte规则
 *
 * @author chenqixu
 */
@AnnoRule
public class ByteRule implements IRule {
    @Override
    public String read(byte[] data, IDefaultValue iDefaultValue) {
        // 先判断是否为空，为空就走默认值
        if (iDefaultValue.isNull(data)) {
            return iDefaultValue.getDefaultValue();
        }
        // 否则就按无符号byte转换
        return parser(data);
    }

    @Override
    public byte[] reverse(String data) {
        return new BigInteger(data).toByteArray();
    }

    protected String parser(byte[] data) {
        return ByteUtil.unsignedBytes(data);
    }

    @Override
    public String getName() {
        return "byte";
    }
}
