package com.cqx.netty.sdtp.rule.impl;

import com.cqx.common.utils.system.ArraysUtil;
import com.cqx.common.utils.system.ByteUtil;
import com.cqx.netty.sdtp.rule.AnnoRule;
import com.cqx.netty.sdtp.rule.IDefaultValue;
import com.cqx.netty.sdtp.rule.IRule;

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
    public byte[] reverse(String data, IDefaultValue iDefaultValue, int size) {
        // 先判断是否为空，为空就走默认值
        if (iDefaultValue.isNull(data)) {
            return iDefaultValue.getDefaultByteValue(size);
        }
        return unParser(data, size);
    }

    protected String parser(byte[] data) {
        return ByteUtil.unsignedBytes(data);
    }

    protected byte[] unParser(String data, int size) {
        byte[] bytes = new BigInteger(data).toByteArray();
        if (size > bytes.length) {
            int diff = size - bytes.length;
            byte[] newbytes = new byte[diff];
            for (int i = 0; i < diff; i++) {
                newbytes[i] = 0x00;
            }
            bytes = ArraysUtil.arrayAdd(newbytes, bytes, bytes.length);
        } else if (bytes.length > size) {
            // 取低位
            int diff = bytes.length - size;
            byte[] newbytes = new byte[size];
            for (int i = diff, j = 0; i < bytes.length; i++, j++) {
                newbytes[j] = bytes[i];
            }
            bytes = newbytes;
        }
        return bytes;
    }

    @Override
    public String getName() {
        return "byte";
    }
}
