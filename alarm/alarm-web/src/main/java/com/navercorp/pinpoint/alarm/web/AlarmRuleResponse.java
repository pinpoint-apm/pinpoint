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
package com.navercorp.pinpoint.alarm.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleLocalConfig;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;

import java.util.List;

public class AlarmRuleResponse extends AlarmRuleV2 {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AlarmRuleLocalConfig localConfig;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AlarmTemplateItem templateItem;
    private List<String> overrideKeys;

    public static AlarmRuleResponse from(AlarmRuleV2 effectiveRule,
                                         AlarmRuleLocalConfig localConfig,
                                         AlarmTemplateItem templateItem) {
        AlarmRuleResponse response = new AlarmRuleResponse();
        response.setId(effectiveRule.getId());
        response.setName(effectiveRule.getName());
        response.setDescription(effectiveRule.getDescription());
        response.setSeverity(effectiveRule.getSeverity());
        response.setDataSource(effectiveRule.getDataSource());
        response.setTemplateItemId(effectiveRule.getTemplateItemId());
        response.setTemplateItemName(effectiveRule.getTemplateItemName());
        response.setTemplateId(effectiveRule.getTemplateId());
        response.setTemplateName(effectiveRule.getTemplateName());
        response.setApplicationType(effectiveRule.getApplicationType());
        response.setServiceName(effectiveRule.getServiceName());
        response.setApplicationName(effectiveRule.getApplicationName());
        response.setCheckIntervalSec(effectiveRule.getCheckIntervalSec());
        response.setActionIntervalSec(effectiveRule.getActionIntervalSec());
        response.setConditions(effectiveRule.getConditions());
        response.setFilters(effectiveRule.getFilters());
        response.setEnabled(effectiveRule.isEnabled());
        response.setUpdatedAt(effectiveRule.getUpdatedAt());
        response.setLocalConfig(localConfig);
        response.setTemplateItem(templateItem);
        response.overrideKeys = effectiveRule.getOverrideKeys();
        return response;
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

    @Override
    public List<String> getOverrideKeys() {
        return overrideKeys == null ? List.of() : overrideKeys;
    }
}
