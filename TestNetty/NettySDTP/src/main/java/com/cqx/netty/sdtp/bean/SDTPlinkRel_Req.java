package com.cqx.netty.sdtp.bean;

/**
 * 连接释放请求，linkRel_Req
 * <table>
 * <tr>
 * <th align="left">字段名</th>
 * <th align="left">字节数</th>
 * <th align="left">类型</th>
 * <th align="left">描述</th>
 * </tr>
 * <tr>
 * <td align="left">Reason</td>
 * <td align="left">1</td>
 * <td align="left">Unsigned integer</td>
 * <td align="left">连接释放的原因，各个值代表意义如下：<br>
 * 1: 用户正常释放。<br>
 * 2: 数据类型错误。<br>
 * 3: 超出机器处理能力。</td>
 * </tr>
 * </table>
 *
 * @author chenqixu
 */
public class SDTPlinkRel_Req implements SDTPBody {
    private byte Reason = (byte) 0x01;

    @Override
    public int length() {
        return 1;
    }

    @Override
    public byte[] getBytes() {
        return new byte[]{Reason};
    }

    @Override
    public void setData(byte[] bytes) {
        if (bytes.length > 0) {
            this.Reason = bytes[0];
        } else {
            throw new NullPointerException("传入的数据为空！");
        }
    }

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.linkRel_Req;
    }
}
