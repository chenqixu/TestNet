package com.cqx.netty.sdtp.rule;

/**
 * 规则接口
 *
 * @author chenqixu
 */
public interface IRule {
    String read(byte[] data, IDefaultValue iDefaultValue);

    byte[] reverse(String data, IDefaultValue iDefaultValue, int size);

    String getName();
}
