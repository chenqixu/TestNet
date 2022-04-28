package com.cqx.netty.sdtp.rule.dvimpl;

import com.cqx.netty.sdtp.rule.AnnoRule;

/**
 * N0DefaultValue
 *
 * @author chenqixu
 */
@AnnoRule
public class N0DefaultValue extends FDefaultValue {
    @Override
    public String getDefaultValue() {
        return "0";
    }

    @Override
    public String getName() {
        return "N0";
    }
}
