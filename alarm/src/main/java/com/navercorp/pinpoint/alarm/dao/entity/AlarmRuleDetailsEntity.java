package com.navercorp.pinpoint.alarm.dao.entity;

import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;

public class AlarmRuleDetailsEntity {

    private AlarmRuleEntity rule;
    private AlarmRuleLocalConfigEntity localConfig;
    private AlarmTemplateItemEntity templateItem;
    // Header has no JSON columns, so the VO is mapped directly without an entity.
    private AlarmTemplate template;

    public AlarmRuleEntity getRule() {
        return rule;
    }

    public void setRule(AlarmRuleEntity rule) {
        this.rule = rule;
    }

    public AlarmRuleLocalConfigEntity getLocalConfig() {
        return localConfig;
    }

    public void setLocalConfig(AlarmRuleLocalConfigEntity localConfig) {
        this.localConfig = localConfig;
    }

    public AlarmTemplateItemEntity getTemplateItem() {
        return templateItem;
    }

    public void setTemplateItem(AlarmTemplateItemEntity templateItem) {
        this.templateItem = templateItem;
    }

    public AlarmTemplate getTemplate() {
        return template;
    }

    public void setTemplate(AlarmTemplate template) {
        this.template = template;
    }
}
