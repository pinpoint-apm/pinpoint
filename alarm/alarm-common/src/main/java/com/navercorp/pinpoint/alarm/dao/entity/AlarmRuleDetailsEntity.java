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
