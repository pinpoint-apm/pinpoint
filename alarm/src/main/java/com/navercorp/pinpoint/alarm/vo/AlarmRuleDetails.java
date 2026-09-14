package com.navercorp.pinpoint.alarm.vo;

public class AlarmRuleDetails {

    private AlarmRuleV2 rule;
    private AlarmRuleLocalConfig localConfig;
    private AlarmTemplateItem templateItem;
    private AlarmTemplate template;

    public AlarmRuleV2 getRule() {
        return rule;
    }

    public void setRule(AlarmRuleV2 rule) {
        this.rule = rule;
    }

    public AlarmRuleLocalConfig getLocalConfig() {
        return localConfig;
    }

    public void setLocalConfig(AlarmRuleLocalConfig localConfig) {
        this.localConfig = localConfig;
    }

    public AlarmTemplateItem getTemplateItem() {
        return templateItem;
    }

    public void setTemplateItem(AlarmTemplateItem templateItem) {
        this.templateItem = templateItem;
    }

    public AlarmTemplate getTemplate() {
        return template;
    }

    public void setTemplate(AlarmTemplate template) {
        this.template = template;
    }
}
