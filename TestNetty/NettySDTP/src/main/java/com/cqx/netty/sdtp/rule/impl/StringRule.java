package com.cqx.netty.sdtp.rule.impl;

import com.cqx.netty.sdtp.rule.AnnoRule;
import com.cqx.netty.sdtp.rule.IDefaultValue;

import java.nio.charset.StandardCharsets;

/**
 * string规则
 *
 * @author chenqixu
 */
@AnnoRule
public class StringRule extends ByteRule {
    @Override
    protected String parser(byte[] data) {
        // 否则就按UTF-8字符串转换
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] reverse(String data, IDefaultValue iDefaultValue, int size) {
        // 先判断是否为空，为空就走默认值
        if (iDefaultValue.isNull(data)) {
            return iDefaultValue.getDefaultByteValue(size);
        }
        return data.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getName() {
        return "string";
    }
}
