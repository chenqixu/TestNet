package com.cqx.netty.sdtp.rule.dvimpl;

import com.cqx.netty.sdtp.rule.AnnoRule;
import com.cqx.netty.sdtp.rule.IDefaultValue;
import com.cqx.netty.util.Constant;

import java.util.Arrays;

/**
 * FDefaultValue
 *
 * @author chenqixu
 */
@AnnoRule
public class FDefaultValue implements IDefaultValue {
    @Override
    public boolean isNull(byte[] data) {
        int size = data.length;
        if (size == 1 && Arrays.equals(data, Constant.BYTE1_DEFAULT)) {
            return true;
        } else if (size == 2 && Arrays.equals(data, Constant.BYTE2_DEFAULT)) {
            return true;
        } else if (size == 4 && Arrays.equals(data, Constant.BYTE4_DEFAULT)) {
            return true;
        } else if (size == 8 && Arrays.equals(data, Constant.BYTE8_DEFAULT)) {
            return true;
        } else if (size == 16 && Arrays.equals(data, Constant.BYTE16_DEFAULT)) {
            return true;
        } else if (size == 32 && Arrays.equals(data, Constant.BYTE32_DEFAULT)) {
            return true;
        } else if (size == 44 && Arrays.equals(data, Constant.BYTE44_DEFAULT)) {
            return true;
        } else if (size == 55 && Arrays.equals(data, Constant.BYTE55_DEFAULT)) {
            return true;
        } else if (size == 64 && Arrays.equals(data, Constant.BYTE64_DEFAULT)) {
            return true;
        } else if (size == 128 && Arrays.equals(data, Constant.BYTE128_DEFAULT)) {
            return true;
        } else if (size == 256 && Arrays.equals(data, Constant.BYTE256_DEFAULT)) {
            return true;
        } else {
            // 针对可变长度字段判断
            return Arrays.equals(data, getEmptyByte(size));
        }
    }

    @Override
    public String getDefaultValue() {
        return Constant.EMPTY_STRING;
    }

    @Override
    public String getName() {
        return "F";
    }

    /**
     * 获取指定长度的0xFF默认值字节数组
     *
     * @param size
     * @return
     */
    public final byte[] getEmptyByte(int size) {
        byte[] BYTE_DEFAULT = new byte[size];
        for (int i = 0; i < size; i++) {
            BYTE_DEFAULT[i] = (byte) 0xff;
        }
        return BYTE_DEFAULT;
    }
}
