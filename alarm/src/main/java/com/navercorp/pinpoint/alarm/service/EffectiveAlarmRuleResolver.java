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
package com.navercorp.pinpoint.alarm.service;

import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleLocalConfig;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
public class EffectiveAlarmRuleResolver {

    public AlarmRuleV2 resolve(AlarmRuleV2 rule,
                               AlarmTemplateItem templateItem,
                               AlarmTemplate template,
                               AlarmRuleLocalConfig localConfig) {
        Objects.requireNonNull(rule, "rule");

        ResolvedConfig config = rule.getTemplateItemId() == null
                ? resolveStandaloneConfig(rule, localConfig)
                : resolveTemplateConfig(rule, templateItem, template, localConfig);
        return buildEffectiveRule(rule, config);
    }

    private ResolvedConfig resolveStandaloneConfig(AlarmRuleV2 rule, AlarmRuleLocalConfig localConfig) {
        AlarmSeverity severity = localValue(localConfig, AlarmRuleLocalConfig::getSeverity);
        if (severity == null) {
            severity = rule.getSeverity();
        }
        Integer checkIntervalSec = localValue(localConfig, AlarmRuleLocalConfig::getCheckIntervalSec);
        if (checkIntervalSec == null) {
            checkIntervalSec = rule.getCheckIntervalSec();
        }
        Integer actionIntervalSec = localValue(localConfig, AlarmRuleLocalConfig::getActionIntervalSec);
        if (actionIntervalSec == null) {
            actionIntervalSec = rule.getActionIntervalSec();
        }
        AlarmCondition conditions = localValue(localConfig, AlarmRuleLocalConfig::getConditions);
        if (conditions == null) {
            conditions = rule.getConditions();
        }
        List<AlarmFilter> filters = localValue(localConfig, AlarmRuleLocalConfig::getFilters);
        if (filters == null) {
            filters = rule.getFilters() != null ? rule.getFilters() : List.of();
        }

        if (severity == null || checkIntervalSec == null || actionIntervalSec == null || conditions == null) {
            throw new IllegalArgumentException("standalone rule config is incomplete: ruleId=" + rule.getId());
        }

        return new ResolvedConfig(
                rule.getDataSource(),
                null,
                null,
                null,
                // Nothing to inherit from: a standalone rule owns its own text.
                ResolvedValue.inherited(localValue(localConfig, AlarmRuleLocalConfig::getName)),
                ResolvedValue.inherited(localValue(localConfig, AlarmRuleLocalConfig::getDescription)),
                ResolvedValue.inherited(severity),
                ResolvedValue.inherited(checkIntervalSec),
                ResolvedValue.inherited(actionIntervalSec),
                ResolvedValue.inherited(conditions),
                ResolvedValue.inherited(filters));
    }

    private ResolvedConfig resolveTemplateConfig(AlarmRuleV2 rule,
                                                 AlarmTemplateItem templateItem,
                                                 AlarmTemplate template,
                                                 AlarmRuleLocalConfig localConfig) {
        if (templateItem == null) {
            throw new IllegalArgumentException(
                    "templateItem must not be null for template-linked rule: ruleId=" + rule.getId());
        }
        if (template == null) {
            throw new IllegalArgumentException(
                    "template must not be null for template-linked rule: ruleId=" + rule.getId());
        }

        return new ResolvedConfig(
                templateItem.getDataSource(),
                template.getId(),
                template.getName(),
                templateItem.getName(),
                ResolvedValue.override(localValue(localConfig, AlarmRuleLocalConfig::getName),
                        templateItem.getName()),
                ResolvedValue.override(localValue(localConfig, AlarmRuleLocalConfig::getDescription),
                        templateItem.getDescription()),
                ResolvedValue.override(localValue(localConfig, AlarmRuleLocalConfig::getSeverity),
                        templateItem.getSeverity()),
                ResolvedValue.override(localValue(localConfig, AlarmRuleLocalConfig::getCheckIntervalSec),
                        templateItem.getCheckIntervalSec()),
                ResolvedValue.override(localValue(localConfig, AlarmRuleLocalConfig::getActionIntervalSec),
                        templateItem.getActionIntervalSec()),
                ResolvedValue.override(localValue(localConfig, AlarmRuleLocalConfig::getConditions),
                        templateItem.getConditions()),
                ResolvedValue.override(localValue(localConfig, AlarmRuleLocalConfig::getFilters),
                        templateItem.getFilters()));
    }

    private AlarmRuleV2 buildEffectiveRule(AlarmRuleV2 source, ResolvedConfig config) {
        AlarmRuleV2 effective = new AlarmRuleV2();
        effective.setId(source.getId());
        effective.setName(config.name().value());
        effective.setDescription(config.description().value());
        effective.setServiceName(source.getServiceName());
        effective.setApplicationName(source.getApplicationName());
        effective.setApplicationType(source.getApplicationType());
        effective.setDataSource(config.dataSource());
        effective.setTemplateItemId(source.getTemplateItemId());
        effective.setTemplateItemName(config.templateItemName());
        effective.setTemplateId(config.templateId());
        effective.setTemplateName(config.templateName());
        effective.setSeverity(config.severity().value());
        effective.setCheckIntervalSec(config.checkIntervalSec().value());
        effective.setActionIntervalSec(config.actionIntervalSec().value());
        effective.setConditions(config.conditions().value());
        effective.setFilters(config.filters().value());
        effective.setEnabled(source.isEnabled());
        effective.setUpdatedAt(source.getUpdatedAt());
        effective.setOverrideName(config.name().overridden());
        effective.setOverrideDescription(config.description().overridden());
        effective.setOverrideSeverity(config.severity().overridden());
        effective.setOverrideCheckIntervalSec(config.checkIntervalSec().overridden());
        effective.setOverrideActionIntervalSec(config.actionIntervalSec().overridden());
        effective.setOverrideConditions(config.conditions().overridden());
        effective.setOverrideFilters(config.filters().overridden());
        return effective;
    }

    private static <T> T localValue(AlarmRuleLocalConfig localConfig,
                                    Function<AlarmRuleLocalConfig, T> getter) {
        return localConfig == null ? null : getter.apply(localConfig);
    }

    private record ResolvedConfig(
            String dataSource,
            Long templateId,
            String templateName,
            String templateItemName,
            ResolvedValue<String> name,
            ResolvedValue<String> description,
            ResolvedValue<AlarmSeverity> severity,
            ResolvedValue<Integer> checkIntervalSec,
            ResolvedValue<Integer> actionIntervalSec,
            ResolvedValue<AlarmCondition> conditions,
            ResolvedValue<List<AlarmFilter>> filters) {
    }

    private record ResolvedValue<T>(T value, boolean overridden) {

        private static <T> ResolvedValue<T> inherited(T value) {
            return new ResolvedValue<>(value, false);
        }

        private static <T> ResolvedValue<T> override(T localValue, T inheritedValue) {
            return localValue != null
                    ? new ResolvedValue<>(localValue, true)
                    : inherited(inheritedValue);
        }
    }
}
