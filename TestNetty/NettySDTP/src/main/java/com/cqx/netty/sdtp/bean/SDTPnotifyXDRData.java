package com.cqx.netty.sdtp.bean;

import com.cqx.netty.util.ByteUtil;

/**
 * XDR数据传输，notifyXDRData
 * <table>
 * <tr>
 * <th align="left">属性</th>
 * <th align="left">字节数</th>
 * <th align="left">类型</th>
 * <th align="left">描述</th>
 * </tr>
 * <tr>
 * <td align="left">XDRType</td>
 * <td align="left">1</td>
 * <td align="left">Unsigned integer</td>
 * <td align="left">XDR数据类型：<br>
 * 1：合成XDR数据<br>
 * 2：单接口XDR数据<br>
 * 对于IF1接口，此处取值为2。</td>
 * </tr>
 * <tr>
 * <td align="left">Load</td>
 * <td align="left">不定长</td>
 * <td align="left"></td>
 * <td align="left">XDR数据，各种接口的XDR数据格式参考本规范第6至30章。</td>
 * </tr>
 * </table>
 *
 * @author chenqixu
 */
public class SDTPnotifyXDRData implements SDTPBody {
    private byte XDRType = 0x02;
    private byte[] LoadLength;
    private byte[] Load;

    @Override
    public int length() {
        return 3 + getLoad().length;
    }

    @Override
    public byte[] getBytes() {
        return ByteUtil.arrayAdd(new byte[]{XDRType, LoadLength[0], LoadLength[1]}, Load, Load.length);
    }

    @Override
    public void setData(byte[] bytes) {
        if (bytes.length > 0) {
            this.Load = bytes;
            this.LoadLength = ByteUtil.intTo2ByteArray(Load.length);
        } else {
            throw new NullPointerException("传入的数据为空！");
        }
    }

    @Override
    public EnumMessageType getMessageType() {
        return EnumMessageType.notifyXDRData_Req;
    }

    public byte getXDRType() {
        return XDRType;
    }

    public void setXDRType(byte XDRType) {
        this.XDRType = XDRType;
    }

    public byte[] getLoad() {
        return Load;
    }

    public void setLoad(byte[] load) {
        Load = load;
    }
}
