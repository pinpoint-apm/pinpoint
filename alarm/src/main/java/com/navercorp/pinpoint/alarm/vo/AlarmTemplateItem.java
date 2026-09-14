package com.navercorp.pinpoint.alarm.vo;

import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One rule definition inside an alarm template bundle ({@link AlarmTemplate}).
 * Successor of the pre-bundle {@code AlarmRuleTemplate}: the identity fields
 * (serviceName, channel bindings) moved to the bundle header.
 */
public class AlarmTemplateItem {

    private Long id;

    private Long templateId;

    @NotBlank(message = "name must not be blank")
    @Size(max = AlarmValidationConstants.MAX_RULE_NAME_LENGTH, message = "name is too long")
    private String name;

    @Size(max = AlarmValidationConstants.MAX_RULE_DESCRIPTION_LENGTH, message = "description is too long")
    private String description;

    @NotNull(message = "severity must not be null")
    private AlarmSeverity severity;

    @NotBlank(message = "dataSource must not be blank")
    private String dataSource;

    @Min(value = 60, message = "checkIntervalSec must be at least 60")
    private int checkIntervalSec;

    @Min(value = 60, message = "actionIntervalSec must be at least 60")
    private int actionIntervalSec;

    @Valid
    @NotNull(message = "conditions must not be null")
    private AlarmCondition conditions;

    @Valid
    @Size(max = AlarmValidationConstants.MAX_FILTER_COUNT,
            message = "filters must contain at most " + AlarmValidationConstants.MAX_FILTER_COUNT + " items")
    private List<AlarmFilter> filters;
    private LocalDateTime updatedAt;
    private int usedRuleCount;
    private int enabledUsedRuleCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public AlarmCondition getConditions() {
        return conditions;
    }

    public void setConditions(AlarmCondition conditions) {
        this.conditions = conditions;
    }

    public List<AlarmFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<AlarmFilter> filters) {
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
