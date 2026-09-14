package com.navercorp.pinpoint.alarm.dao.entity;

import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;

public class AlarmRuleLocalConfigEntity {

    private Long ruleId;
    private String name;
    private String description;
    private AlarmSeverity severity;
    private Integer checkIntervalSec;
    private Integer actionIntervalSec;
    private String conditions;
    private String filters;

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AlarmSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlarmSeverity severity) {
        this.severity = severity;
    }

    public Integer getCheckIntervalSec() {
        return checkIntervalSec;
    }

    public void setCheckIntervalSec(Integer checkIntervalSec) {
        this.checkIntervalSec = checkIntervalSec;
    }

    public Integer getActionIntervalSec() {
        return actionIntervalSec;
    }

    public void setActionIntervalSec(Integer actionIntervalSec) {
        this.actionIntervalSec = actionIntervalSec;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }
}
