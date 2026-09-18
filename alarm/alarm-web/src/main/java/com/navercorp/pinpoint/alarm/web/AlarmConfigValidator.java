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

import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import com.navercorp.pinpoint.alarm.validation.ConditionValidator;
import com.navercorp.pinpoint.alarm.validation.FilterKeyValidator;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * Validates and normalizes what a rule or a bundle item may hold.
 * <p>
 * A bundle item and a standalone rule carry the same configuration, so the rules for it
 * are the same on both sides -- interval options, condition tree limits, filter keys
 * belonging to the data source. Keeping them here means the two paths cannot drift, and
 * neither can the two services that own them.
 * <p>
 * "Normalize" is part of the job, not a side effect: intervals round up to the next
 * option and a one-child group collapses, so what is stored is what the screen shows.
 */
@Component
class AlarmConfigValidator {

    private final AlarmTemplateItemDao templateItemDao;
    private final ConditionValidator conditionValidator;
    private final FilterKeyValidator filterKeyValidator;
    private final AlarmDataSourceRegistry dataSourceRegistry;

    AlarmConfigValidator(AlarmTemplateItemDao templateItemDao,
                         ConditionValidator conditionValidator,
                         FilterKeyValidator filterKeyValidator,
                         AlarmDataSourceRegistry dataSourceRegistry) {
        this.templateItemDao = Objects.requireNonNull(templateItemDao, "templateItemDao");
        this.conditionValidator = Objects.requireNonNull(conditionValidator, "conditionValidator");
        this.filterKeyValidator = Objects.requireNonNull(filterKeyValidator, "filterKeyValidator");
        this.dataSourceRegistry = Objects.requireNonNull(dataSourceRegistry, "dataSourceRegistry");
    }

    void validateRuleConfig(AlarmRuleV2 rule, AlarmTemplateItem templateItem) {
        normalizeInheritedText(rule, templateItem);
        if (templateItem == null) {
            if (!StringUtils.hasText(rule.getName())) {
                // Nothing to inherit from, so the name cannot be left out.
                throw new IllegalArgumentException("name must not be blank");
            }
            requireStandaloneConfig(rule);
            validateConfig(rule.getDataSource(), rule.getConditions(), rule.getFilters());
            return;
        }

        validateOptionalInterval(rule.getCheckIntervalSec(), "checkIntervalSec",
                AlarmValidationConstants.CHECK_INTERVAL_SEC_OPTIONS);
        validateOptionalInterval(rule.getActionIntervalSec(), "actionIntervalSec",
                AlarmValidationConstants.ACTION_INTERVAL_SEC_OPTIONS);
        if (rule.getConditions() != null) {
            conditionValidator.validate(rule.getConditions());
        }
        validateConfig(templateItem.getDataSource(), rule.getConditions(), rule.getFilters());
    }

    /**
     * Blanks a template-linked rule's name/description when it matches the item, so
     * editing one back to the inherited value clears the override instead of freezing
     * a copy. Mirrors how {@code saveLocalConfig} drops an empty local config.
     */
    void normalizeInheritedText(AlarmRuleV2 rule, AlarmTemplateItem templateItem) {
        if (templateItem == null) {
            return;
        }
        if (!StringUtils.hasText(rule.getName()) || rule.getName().equals(templateItem.getName())) {
            rule.setName(null);
        }
        if (!StringUtils.hasText(rule.getDescription())
                || rule.getDescription().equals(templateItem.getDescription())) {
            rule.setDescription(null);
        }
    }

    List<AlarmTemplateItem> requireItems(AlarmTemplate template) {
        if (CollectionUtils.isEmpty(template.getItems())) {
            throw new IllegalArgumentException("Template must have at least one item");
        }
        return template.getItems();
    }

    void prepareItem(AlarmTemplateItem item) {
        item.setConditions(normalizeConditions(item.getConditions()));
        validateItemConfig(item);
        roundUpIntervals(item);
    }

    void validateItemConfig(AlarmTemplateItem item) {
        if (!StringUtils.hasText(item.getDataSource())) {
            throw new IllegalArgumentException("dataSource must not be blank");
        }
        if (item.getSeverity() == null) {
            throw new IllegalArgumentException("severity must not be null");
        }
        validateRequiredInterval(item.getCheckIntervalSec(), "checkIntervalSec",
                AlarmValidationConstants.CHECK_INTERVAL_SEC_OPTIONS);
        validateRequiredInterval(item.getActionIntervalSec(), "actionIntervalSec",
                AlarmValidationConstants.ACTION_INTERVAL_SEC_OPTIONS);
        if (item.getConditions() == null) {
            throw new IllegalArgumentException("conditions must not be null");
        }
        conditionValidator.validate(item.getConditions());
        validateConfig(item.getDataSource(), item.getConditions(), item.getFilters());
    }

    void validateItemDataSourceChange(AlarmTemplateItem existing, AlarmTemplateItem update) {
        if (update.getDataSource() == null || update.getDataSource().equals(existing.getDataSource())) {
            return;
        }
        if (templateItemDao.countRulesByTemplateItemId(existing.getId()) > 0) {
            throw new AlarmResourceConflictException(
                    "Template item dataSource cannot be changed while rules reference it: itemId=" + existing.getId());
        }
    }

    void requireStandaloneConfig(AlarmRuleV2 rule) {
        if (!StringUtils.hasText(rule.getDataSource())) {
            throw new IllegalArgumentException("dataSource must not be blank");
        }
        if (rule.getSeverity() == null) {
            throw new IllegalArgumentException("severity must not be null");
        }
        validateRequiredInterval(rule.getCheckIntervalSec(), "checkIntervalSec",
                AlarmValidationConstants.CHECK_INTERVAL_SEC_OPTIONS);
        validateRequiredInterval(rule.getActionIntervalSec(), "actionIntervalSec",
                AlarmValidationConstants.ACTION_INTERVAL_SEC_OPTIONS);
        if (rule.getConditions() == null) {
            throw new IllegalArgumentException("conditions must not be null");
        }
        conditionValidator.validate(rule.getConditions());
    }

    void validateConfig(String dataSource,
                        AlarmCondition conditions,
                        List<AlarmFilter> filters) {
        AlarmDataSource resolved = dataSourceRegistry.get(dataSource);
        filterKeyValidator.validateFilters(resolved, filters);
        filterKeyValidator.validateConditions(resolved, conditions);
    }

    private void validateRequiredInterval(Integer intervalSec, String fieldName, List<Integer> optionsSec) {
        if (intervalSec == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        validateOptionalInterval(intervalSec, fieldName, optionsSec);
    }

    /**
     * Below the floor is rejected; a value between the floor and the ceiling that is not
     * in the list is accepted and raised to the next option by {@link #roundUpToInterval}.
     * Above the ceiling is rejected rather than absorbed, because rounding it would have
     * to round down -- evaluating and notifying more often than was asked for.
     */
    private void validateOptionalInterval(Integer intervalSec, String fieldName, List<Integer> optionsSec) {
        if (intervalSec == null) {
            return;
        }
        if (intervalSec < 60) {
            throw new IllegalArgumentException(fieldName + " must be at least 60");
        }
        int maxIntervalSec = optionsSec.get(optionsSec.size() - 1);
        if (intervalSec > maxIntervalSec) {
            throw new IllegalArgumentException(fieldName + " must be at most " + maxIntervalSec);
        }
    }

    void roundUpIntervals(AlarmRuleV2 rule) {
        rule.setCheckIntervalSec(
                roundUpToInterval(rule.getCheckIntervalSec(),
                        AlarmValidationConstants.CHECK_INTERVAL_SEC_OPTIONS));
        rule.setActionIntervalSec(
                roundUpToInterval(rule.getActionIntervalSec(),
                        AlarmValidationConstants.ACTION_INTERVAL_SEC_OPTIONS));
    }

    void roundUpIntervals(AlarmTemplateItem item) {
        item.setCheckIntervalSec(
                roundUpToInterval(item.getCheckIntervalSec(),
                        AlarmValidationConstants.CHECK_INTERVAL_SEC_OPTIONS));
        item.setActionIntervalSec(
                roundUpToInterval(item.getActionIntervalSec(),
                        AlarmValidationConstants.ACTION_INTERVAL_SEC_OPTIONS));
    }

    /**
     * The editor offers a fixed list of intervals, but the API accepts any value, so a
     * value off the list is raised to the next one on it. Up rather than down: rounding
     * down would evaluate or notify more often than was asked for. Values above the
     * ceiling never reach here -- {@link #validateOptionalInterval} has rejected them.
     */
    private static Integer roundUpToInterval(Integer intervalSec, List<Integer> optionsSec) {
        if (intervalSec == null) {
            return null;
        }
        return optionsSec.stream()
                .filter(option -> option >= intervalSec)
                .findFirst()
                .orElse(intervalSec);
    }

    /**
     * A group with a single child means nothing that the child alone does not, so it is
     * collapsed into that child on save, recursively. Like the interval round-up, this
     * normalizes at write time so that what is stored and what is displayed agree.
     */
    static AlarmCondition normalizeConditions(AlarmCondition node) {
        if (node == null || !node.isGroup() || CollectionUtils.isEmpty(node.getCriteria())) {
            return node;
        }
        node.setCriteria(node.getCriteria().stream()
                .map(AlarmConfigValidator::normalizeConditions)
                .toList());
        if (node.getCriteria().size() == 1) {
            return node.getCriteria().get(0);
        }
        return node;
    }
}
