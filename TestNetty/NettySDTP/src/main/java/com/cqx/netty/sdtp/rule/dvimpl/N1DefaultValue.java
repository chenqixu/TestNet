package com.cqx.netty.sdtp.rule.dvimpl;

import com.cqx.netty.sdtp.rule.AnnoRule;

/**
 * N1DefaultValue
 *
 * @author chenqixu
 */
@AnnoRule
public class N1DefaultValue extends N0DefaultValue {
    @Override
    public String getDefaultValue() {
        return "1";
    }

    @Override
    public String getName() {
        return "N1";
    }
}
