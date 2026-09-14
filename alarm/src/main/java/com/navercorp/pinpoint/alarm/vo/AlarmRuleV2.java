package com.navercorp.pinpoint.alarm.vo;

import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlarmRuleV2 {

    private Long id;

    /**
     * The effective name. It is not stored on the rule row: like severity and the other
     * inherited fields it lives in {@link AlarmRuleLocalConfig}, where null on a
     * template-linked rule means "inherit the bundle item's name" and a value is an
     * override. A standalone rule has nothing to inherit from, so its name is required —
     * enforced where the rule is saved rather than by an annotation.
     */
    @Size(max = AlarmValidationConstants.MAX_RULE_NAME_LENGTH, message = "name is too long")
    private String name;

    @Size(max = AlarmValidationConstants.MAX_RULE_DESCRIPTION_LENGTH, message = "description is too long")
    private String description;

    private AlarmSeverity severity;

    private String dataSource;

    /** Decides which registry resolves the target application. */
    @NotBlank(message = "applicationType must not be blank")
    @Size(max = 30, message = "applicationType is too long")
    private String applicationType;

    // The only template link stored on the rule row: the bundle item this rule was stamped from.
    private Long templateItemId;

    // Transient bundle context resolved from templateItemId (item -> header); never stored on the rule.
    private Long templateId;
    private String templateName;
    private String templateItemName;

    @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
    private String serviceName;

    @NotBlank(message = "applicationName must not be blank")
    @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "applicationName is too long")
    private String applicationName;

    @Min(value = 60, message = "checkIntervalSec must be at least 60")
    private Integer checkIntervalSec;

    @Min(value = 60, message = "actionIntervalSec must be at least 60")
    private Integer actionIntervalSec;

    @Valid
    private AlarmCondition conditions;

    @Valid
    @Size(max = AlarmValidationConstants.MAX_FILTER_COUNT,
            message = "filters must contain at most " + AlarmValidationConstants.MAX_FILTER_COUNT + " items")
    private List<AlarmFilter> filters;
    /** Matches the column default: a request that omits it means an evaluated rule. */
    private boolean enabled = true;
    private LocalDateTime updatedAt;
    private boolean overrideName;
    private boolean overrideDescription;
    private boolean overrideSeverity;
    private boolean overrideCheckIntervalSec;
    private boolean overrideActionIntervalSec;
    private boolean overrideConditions;
    private boolean overrideFilters;

    public AlarmRuleV2() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public Long getTemplateItemId() {
        return templateItemId;
    }

    public void setTemplateItemId(Long templateItemId) {
        this.templateItemId = templateItemId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateItemName() {
        return templateItemName;
    }

    public void setTemplateItemName(String templateItemName) {
        this.templateItemName = templateItemName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setOverrideSeverity(boolean overrideSeverity) {
        this.overrideSeverity = overrideSeverity;
    }

    public void setOverrideName(boolean overrideName) {
        this.overrideName = overrideName;
    }

    public void setOverrideDescription(boolean overrideDescription) {
        this.overrideDescription = overrideDescription;
    }

    public void setOverrideCheckIntervalSec(boolean overrideCheckIntervalSec) {
        this.overrideCheckIntervalSec = overrideCheckIntervalSec;
    }

    public void setOverrideActionIntervalSec(boolean overrideActionIntervalSec) {
        this.overrideActionIntervalSec = overrideActionIntervalSec;
    }

    public void setOverrideConditions(boolean overrideConditions) {
        this.overrideConditions = overrideConditions;
    }

    public void setOverrideFilters(boolean overrideFilters) {
        this.overrideFilters = overrideFilters;
    }

    public List<String> getOverrideKeys() {
        if (templateItemId == null) {
            return List.of();
        }
        List<String> overrideKeys = new ArrayList<>();
        if (overrideName) {
            overrideKeys.add("name");
        }
        if (overrideDescription) {
            overrideKeys.add("description");
        }
        if (overrideSeverity) {
            overrideKeys.add("severity");
        }
        if (overrideCheckIntervalSec) {
            overrideKeys.add("checkIntervalSec");
        }
        if (overrideActionIntervalSec) {
            overrideKeys.add("actionIntervalSec");
        }
        if (overrideConditions) {
            overrideKeys.add("conditions");
        }
        if (overrideFilters) {
            overrideKeys.add("filters");
        }
        return overrideKeys;
    }

    @Override
    public String toString() {
        return "AlarmRuleV2{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dataSource='" + dataSource + '\'' +
                ", applicationType='" + applicationType + "'" +
                ", templateItemId=" + templateItemId +
                ", serviceName='" + serviceName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
