/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.vo;

import java.util.List;

public class AlarmRuleLocalConfig {

    private Long ruleId;
    /** Null means the rule inherits its bundle item's text; a standalone rule always has one. */
    private String name;
    private String description;
    private AlarmSeverity severity;
    private Integer checkIntervalSec;
    private Integer actionIntervalSec;
    private AlarmCondition conditions;
    private List<AlarmFilter> filters;

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
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

    public boolean isEmpty() {
        return name == null
                && description == null
                && severity == null
                && checkIntervalSec == null
                && actionIntervalSec == null
                && conditions == null
                && filters == null;
    }
}
