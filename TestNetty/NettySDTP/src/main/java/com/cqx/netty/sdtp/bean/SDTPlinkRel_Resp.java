package com.cqx.netty.sdtp.bean;

import com.cqx.common.utils.system.ByteUtil;

/**
 * 连接释放应答，linkRel_Resp
 * <table>
 * <tr>
 * <th align="left">字段名</th>
 * <th align="left">字节数</th>
 * <th align="left">类型</th>
 * <th align="left">描述</th>
 * </tr>
 * <tr>
 * <td align="left">Result</td>
 * <td align="left">1</td>
 * <td align="left">Unsigned integer</td>
 * <td align="left">连接释放的完成状态<br>
 * 1：释放完成。<br>
 * 其它：释放失败。</td>
 * </tr>
 * </table>
 *
 * @author chenqixu
 */
public class SDTPlinkRel_Resp implements SDTPBody {
    private byte Result = (byte) 0x01;

    @Override
    public int length() {
        return 1;
    }

    @Override
    public byte[] getBytes() {
        return new byte[]{Result};
    }

    @Override
    public void setData(byte[] bytes) {
        if (bytes.length > 0) {
            this.Result = bytes[0];
        } else {
            throw new NullPointerException("传入的数据为空！");
        }
    }

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.linkRel_Resp;
    }

    public String getResult() {
        return ByteUtil.unsignedByte(Result);
    }
}
