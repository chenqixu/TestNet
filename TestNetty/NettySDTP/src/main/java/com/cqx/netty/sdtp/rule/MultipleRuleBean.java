package com.cqx.netty.sdtp.rule;

import java.util.ArrayList;
import java.util.List;

/**
 * MultipleRuleBean
 *
 * @author chenqixu
 */
public class MultipleRuleBean {
    private EnumRuleType ruleType = EnumRuleType.SINGLE;
    private List<RuleBean> ruleBeanList = new ArrayList<>();
    private RuleBean groupFirstRule;

    public MultipleRuleBean() {
    }

    public MultipleRuleBean(RuleBean groupFirstRule) {
        this.groupFirstRule = groupFirstRule;
    }

    public void addRule(RuleBean ruleBean) {
        ruleBeanList.add(ruleBean);
        if (ruleType.equals(EnumRuleType.SINGLE) && groupFirstRule != null && ruleBeanList.size() > 0) {
            ruleType = EnumRuleType.GROUP;
        }
    }

    public EnumRuleType getRuleType() {
        return ruleType;
    }

    public List<RuleBean> getGroupRuleBeanList() {
        if (ruleType.equals(EnumRuleType.GROUP) && ruleBeanList.size() > 0) {
            return ruleBeanList;
        }
        return null;
    }

    public RuleBean getSingleRuleBean() {
        if (ruleType.equals(EnumRuleType.SINGLE) && ruleBeanList.size() > 0) {
            return ruleBeanList.get(0);
        }
        return null;
    }

    public List<RuleBean> getSingleRuleBeanList() {
        if (ruleType.equals(EnumRuleType.SINGLE) && ruleBeanList.size() > 0) {
            return ruleBeanList;
        }
        return null;
    }

    public RuleBean getGroupFirstRule() {
        return groupFirstRule;
    }
}
