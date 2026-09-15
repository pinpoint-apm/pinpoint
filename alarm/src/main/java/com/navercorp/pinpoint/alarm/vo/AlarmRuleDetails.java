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
