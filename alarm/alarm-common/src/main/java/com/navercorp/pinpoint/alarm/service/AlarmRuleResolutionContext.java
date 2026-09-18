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

import com.navercorp.pinpoint.alarm.dao.AlarmRuleLocalConfigDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleLocalConfig;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Everything a batch of rules needs to be resolved into its effective form, read in
 * three queries instead of three per rule.
 *
 * <p>Loaded the same way for the evaluation batch and for the rule listing API, so the
 * two cannot drift apart on what counts as a live item or how the ids are chunked.
 */
public final class AlarmRuleResolutionContext {

    private static final AlarmRuleResolutionContext EMPTY =
            new AlarmRuleResolutionContext(Map.of(), Map.of(), Map.of());

    private final Map<Long, AlarmRuleLocalConfig> localConfigs;
    private final Map<Long, AlarmTemplateItem> items;
    private final Map<Long, AlarmTemplate> templates;

    private AlarmRuleResolutionContext(Map<Long, AlarmRuleLocalConfig> localConfigs,
                                       Map<Long, AlarmTemplateItem> items,
                                       Map<Long, AlarmTemplate> templates) {
        this.localConfigs = localConfigs;
        this.items = items;
        this.templates = templates;
    }

    public static AlarmRuleResolutionContext load(List<AlarmRuleV2> rules,
                                                  AlarmRuleLocalConfigDao localConfigDao,
                                                  AlarmTemplateItemDao templateItemDao,
                                                  AlarmTemplateDao templateDao) {
        if (rules == null || rules.isEmpty()) {
            return EMPTY;
        }
        Map<Long, AlarmRuleLocalConfig> localConfigs = byId(
                ids(rules, AlarmRuleV2::getId),
                localConfigDao::selectByRuleIds,
                AlarmRuleLocalConfig::getRuleId);
        Map<Long, AlarmTemplateItem> items = byId(
                ids(rules, AlarmRuleV2::getTemplateItemId),
                templateItemDao::selectByIds,
                AlarmTemplateItem::getId);
        Map<Long, AlarmTemplate> templates = byId(
                ids(items.values(), AlarmTemplateItem::getTemplateId),
                templateDao::selectByIds,
                AlarmTemplate::getId);
        return new AlarmRuleResolutionContext(localConfigs, items, templates);
    }

    public AlarmRuleLocalConfig localConfigOf(AlarmRuleV2 rule) {
        return localConfigs.get(rule.getId());
    }

    public AlarmTemplateItem itemOf(AlarmRuleV2 rule) {
        return rule.getTemplateItemId() == null ? null : items.get(rule.getTemplateItemId());
    }

    public AlarmTemplate templateOf(AlarmTemplateItem item) {
        return item == null || item.getTemplateId() == null ? null : templates.get(item.getTemplateId());
    }

    private static <T> List<Long> ids(Collection<T> source, Function<T, Long> idOf) {
        return source.stream()
                .map(idOf)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private static <T> Map<Long, T> byId(List<Long> ids,
                                         Function<List<Long>, List<T>> select,
                                         Function<T, Long> idOf) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return select.apply(ids).stream()
                .collect(Collectors.toMap(idOf, Function.identity(), (left, right) -> right));
    }
}
