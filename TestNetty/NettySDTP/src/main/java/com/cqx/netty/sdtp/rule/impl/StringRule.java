package com.cqx.netty.sdtp.rule.impl;

import com.cqx.netty.sdtp.rule.AnnoRule;

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
    protected byte[] unParser(String data, int size) {
        return data.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getName() {
        return "string";
    }
}
