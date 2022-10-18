package com.cqx.netty.sdtp.rule;

/**
 * 规则bean
 *
 * @author chenqixu
 */
public class RuleBean {
    public static final String TLVHeader = "T";
    public static final String FixedLength = "L";
    public static final String VariableLength = "LV";
    public static final String LengthByOtherTypes = "LIP[";
    public static final String GroupFirst = "G1";
    public static final String GroupBody = "GP";

    private String fieldName;
    private String assistFieldName;// 协助字段(当前仅用于确认Ipv4和Ipv6)
    private ReadType readType;
    private int readlen;
    private IRule rule;
    private IDefaultValue defaultValue;

    private String parserRuleName;
    private String defaultValueRuleName;

    private boolean isGroup;
    private boolean isGroupFirst;
    private boolean isGroupBody;

    private boolean isTLV;
    private int tag;

    public RuleBean() {
    }

    public RuleBean(String singleRule) {
        String[] singleRuleArray = singleRule.split("-", -1);
        // 读取字段名称
        String fieldName = singleRuleArray[0];
        setFieldName(fieldName);
        // 读取规则(定长、变长)
        String readLengthRule = singleRuleArray[1];
        // 转换规则(无符号、有符号、字符、16进制等)
        String parserRule = singleRuleArray[2];
        setParserRuleName(parserRule);
        // 默认值规则(默认全F、默认0等)
        String defaultValueRule = singleRuleArray[3];
        setDefaultValueRuleName(defaultValueRule);
        // 是否group
        if (singleRuleArray.length >= 5) {
            String groupTag = singleRuleArray[4];
            isGroup = true;
            // 组开始
            if (groupTag.equals(GroupFirst)) {
                isGroupFirst = true;
            } else if (groupTag.equals(GroupBody)) {// 组体
                isGroupBody = true;
            }
        }

        if (readLengthRule.startsWith(TLVHeader)) {
            isTLV = true;
            // 替换T
            readLengthRule = readLengthRule.replaceFirst(TLVHeader, "");
            // 找到第一个L
            int index = readLengthRule.indexOf(FixedLength);
            String tagStr = readLengthRule.substring(0, index);
            tag = Integer.valueOf(tagStr);
            readLengthRule = readLengthRule.substring(index);
        }

        // 判断是定长还是变长
        if (readLengthRule.equals(VariableLength)) {// 变长
            setReadType(ReadType.LV);
        } else if (readLengthRule.startsWith(LengthByOtherTypes)) {// 由另一个字段来确定
            setReadType(ReadType.LIP);
            // 设置协助字段
            setAssistFieldName(readLengthRule.replace(LengthByOtherTypes, ""));
        } else {// 定长
            int readlen = Integer.valueOf(readLengthRule.replace(FixedLength, ""));
            setReadType(ReadType.LN);
            setReadlen(readlen);
        }
    }

    public String toSuperString() {
        return super.toString();
    }

    @Override
    public String toString() {
        return String.format("fieldName: %s, readType: %s, readlen: %s, rule: %s, defaultValue: %s, assistFieldName: %s" +
                        ", isGroup: %s, isGroupFirst: %s, isGroupBody: %s, isTLV: %s, Tag: %s"
                , fieldName, readType, readlen, rule, defaultValue, assistFieldName, isGroup, isGroupFirst, isGroupBody, isTLV, tag);
    }

    public ReadType getReadType() {
        return readType;
    }

    public void setReadType(ReadType readType) {
        this.readType = readType;
    }

    public int getReadlen() {
        return readlen;
    }

    public void setReadlen(int readlen) {
        this.readlen = readlen;
    }

    public IRule getRule() {
        return rule;
    }

    public void setRule(IRule rule) {
        this.rule = rule;
    }

    public IDefaultValue getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(IDefaultValue defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getAssistFieldName() {
        return assistFieldName;
    }

    public void setAssistFieldName(String assistFieldName) {
        this.assistFieldName = assistFieldName;
    }

    public String getParserRuleName() {
        return parserRuleName;
    }

    public void setParserRuleName(String parserRuleName) {
        this.parserRuleName = parserRuleName;
    }

    public String getDefaultValueRuleName() {
        return defaultValueRuleName;
    }

    public void setDefaultValueRuleName(String defaultValueRuleName) {
        this.defaultValueRuleName = defaultValueRuleName;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public boolean isGroupFirst() {
        return isGroupFirst;
    }

    public boolean isGroupBody() {
        return isGroupBody;
    }

    public boolean isTLV() {
        return isTLV;
    }

    public int getTag() {
        return tag;
    }

    enum ReadType {
        LN, LV, LIP;
    }
}
