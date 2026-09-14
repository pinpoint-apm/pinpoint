package com.navercorp.pinpoint.alarm.dao.entity;

import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;

import java.time.LocalDateTime;

public class AlarmTemplateItemEntity {

    private Long id;
    private Long templateId;
    private String name;

    private String description;
    private AlarmSeverity severity;
    private String dataSource;
    private int checkIntervalSec;
    private int actionIntervalSec;
    private String conditions;
    private String filters;
    private LocalDateTime updatedAt;
    private int usedRuleCount;
    private int enabledUsedRuleCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
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

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public int getCheckIntervalSec() {
        return checkIntervalSec;
    }

    public void setCheckIntervalSec(int checkIntervalSec) {
        this.checkIntervalSec = checkIntervalSec;
    }

    public int getActionIntervalSec() {
        return actionIntervalSec;
    }

    public void setActionIntervalSec(int actionIntervalSec) {
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getUsedRuleCount() {
        return usedRuleCount;
    }

    public void setUsedRuleCount(int usedRuleCount) {
        this.usedRuleCount = usedRuleCount;
    }

    public int getEnabledUsedRuleCount() {
        return enabledUsedRuleCount;
    }

    public void setEnabledUsedRuleCount(int enabledUsedRuleCount) {
        this.enabledUsedRuleCount = enabledUsedRuleCount;
    }
}
