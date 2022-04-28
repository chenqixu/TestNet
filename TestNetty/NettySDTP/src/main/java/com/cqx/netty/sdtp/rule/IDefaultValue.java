package com.cqx.netty.sdtp.rule;

/**
 * 默认值接口
 *
 * @author chenqixu
 */
public interface IDefaultValue {
    boolean isNull(byte[] data);

    String getDefaultValue();

    String getName();
}
