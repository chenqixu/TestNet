package com.cqx.netty.sdtp.rule;

/**
 * 规则bean
 *
 * @author chenqixu
 */
public class RuleBean {
    private String fieldName;
    private String assistFieldName;// 协助字段(当前仅用于确认Ipv4和Ipv6)
    private ReadType readType;
    private int readlen;
    private IRule rule;
    private IDefaultValue defaultValue;

    @Override
    public String toString() {
        return String.format("fieldName: %s, readType: %s, readlen: %s, rule: %s, defaultValue: %s, assistFieldName: %s"
                , fieldName, readType, readlen, rule, defaultValue, assistFieldName);
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

    enum ReadType {
        LN, LV, LIP;
    }
}
