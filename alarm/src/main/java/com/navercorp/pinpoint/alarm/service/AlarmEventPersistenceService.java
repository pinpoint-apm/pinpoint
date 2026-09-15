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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryKey;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmStateDao;
import com.navercorp.pinpoint.alarm.sender.AlarmNotificationService;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmHistoryV2;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmSeverity;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import com.navercorp.pinpoint.alarm.vo.AlarmStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.navercorp.pinpoint.alarm.util.ExceptionMessageUtils.rootCauseMessage;

/**
 * Persists alarm state, history, and notification deliveries atomically.
 */
@Service
public class AlarmEventPersistenceService {

    private static final Logger logger = LogManager.getLogger(AlarmEventPersistenceService.class);
    private static final int DEFAULT_CHECK_FAILURE_RETRY_SEC = 300;
    private static final int DEFAULT_CHECK_FAILURE_ACTION_INTERVAL_SEC = 3600;

    private final AlarmRuleV2Dao ruleDao;
    private final AlarmStateDao stateDao;
    private final AlarmHistoryV2Dao historyDao;
    private final AlarmNotificationOutboxDao outboxDao;
    private final AlarmNotificationService notificationService;
    private final TransactionTemplate requiresNew;
    private final ObjectWriter alarmContextWriter;
    private final ObjectWriter checkFailedContextWriter;

    public AlarmEventPersistenceService(AlarmRuleV2Dao ruleDao,
                                        AlarmStateDao stateDao,
                                        AlarmHistoryV2Dao historyDao,
                                        AlarmNotificationOutboxDao outboxDao,
                                        AlarmNotificationService notificationService,
                                        @Qualifier("transactionManager")
                                        PlatformTransactionManager transactionManager,
                                        ObjectMapper objectMapper) {
        this.ruleDao = Objects.requireNonNull(ruleDao, "ruleDao");
        this.stateDao = Objects.requireNonNull(stateDao, "stateDao");
        this.historyDao = Objects.requireNonNull(historyDao, "historyDao");
        this.outboxDao = Objects.requireNonNull(outboxDao, "outboxDao");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService");
        this.requiresNew = new TransactionTemplate(Objects.requireNonNull(transactionManager, "transactionManager"));
        this.requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        ObjectMapper requiredObjectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.alarmContextWriter = requiredObjectMapper.writerFor(AlarmContext.class);
        this.checkFailedContextWriter = requiredObjectMapper.writerFor(CheckFailedContext.class);
    }

    public void recordFired(AlarmRuleV2 rule,
                            MetricQueryResult metricResults,
                            List<AlarmCondition> leaves,
                            LocalDateTime now) {
        requiresNew.executeWithoutResult(status -> {
            lockRule(rule.getId());
            AlarmState state = getOrCreateState(rule.getId());
            boolean wasNormal = (state.getStatus() == AlarmStatus.NORMAL);

            state.setStatus(AlarmStatus.FIRING);
            state.setLastCheckedAt(now);
            state.setLastFiredAt(now);
            state.setNextCheckAt(now.plusSeconds(rule.getCheckIntervalSec()));

            if (!wasNormal && !shouldEnqueue(state, now, rule.getActionIntervalSec())) {
                stateDao.upsert(state);
                return;
            }

            state.setLastNotificationEnqueuedAt(now);
            AlarmNotificationService.PreparationResult preparation = prepareNotifications(rule, metricResults);

            AlarmHistoryV2 history = AlarmHistoryV2.fired(
                    rule.getId(),
                    "[" + rule.getSeverity() + "] " + rule.getName(),
                    buildContext(rule, metricResults, leaves, preparation));
            persistEvent(state, history, preparation, now);
        });
    }

    public void recordResolved(AlarmRuleV2 rule, List<AlarmCondition> leaves, LocalDateTime now) {
        requiresNew.executeWithoutResult(status -> {
            lockRule(rule.getId());
            AlarmState state = getOrCreateState(rule.getId());
            boolean wasFiring = state.isFiring();

            state.setStatus(AlarmStatus.NORMAL);
            state.setLastCheckedAt(now);
            state.setNextCheckAt(now.plusSeconds(rule.getCheckIntervalSec()));

            if (wasFiring && !isOneShotRule(leaves)) {
                historyDao.insert(AlarmHistoryV2.resolved(
                        rule.getId(), "[RESOLVED] " + rule.getName()));
            }
            stateDao.upsert(state);
        });
    }

    public void recordCheckFailed(AlarmRuleV2 rule, RuntimeException failure, LocalDateTime now) {
        requiresNew.executeWithoutResult(status -> {
            lockRule(rule.getId());
            AlarmState state = getOrCreateState(rule.getId());
            AlarmRuleV2 notificationRule = buildCheckFailedNotificationRule(rule, failure);
            int checkIntervalSec = notificationRule.getCheckIntervalSec();
            int actionIntervalSec = notificationRule.getActionIntervalSec();
            state.setStatus(AlarmStatus.CHECK_FAILED);
            state.setLastCheckedAt(now);
            state.setNextCheckAt(now.plusSeconds(checkIntervalSec));

            if (!shouldEnqueue(state, now, actionIntervalSec)) {
                stateDao.upsert(state);
                return;
            }

            state.setLastNotificationEnqueuedAt(now);
            AlarmNotificationService.PreparationResult preparation =
                    prepareNotifications(notificationRule, MetricQueryResult.empty());

            AlarmHistoryV2 history = AlarmHistoryV2.checkFailed(
                    rule.getId(),
                    "[CHECK_FAILED] " + ruleLabel(rule) + ": " + rootCauseMessage(failure),
                    buildCheckFailedContext(rule, failure, preparation));
            persistEvent(state, history, preparation, now);
        });
    }

    public void recordCheckFailedSilently(AlarmRuleV2 rule, LocalDateTime now) {
        requiresNew.executeWithoutResult(status -> {
            lockRule(rule.getId());
            AlarmState state = getOrCreateState(rule.getId());
            state.setStatus(AlarmStatus.CHECK_FAILED);
            state.setLastCheckedAt(now);
            state.setNextCheckAt(now.plusSeconds(positiveOrDefault(
                    rule.getCheckIntervalSec(), DEFAULT_CHECK_FAILURE_RETRY_SEC)));
            stateDao.upsert(state);
        });
    }

    private AlarmNotificationService.PreparationResult prepareNotifications(
            AlarmRuleV2 rule, MetricQueryResult metricResults) {
        try {
            return notificationService.prepareNotifications(rule, metricResults);
        } catch (RuntimeException failure) {
            logger.error("Failed to prepare alarm notifications: rule_id={}", rule.getId(), failure);
            return AlarmNotificationService.PreparationResult.failed(rootCauseMessage(failure));
        }
    }

    private void persistEvent(AlarmState state,
                              AlarmHistoryV2 history,
                              AlarmNotificationService.PreparationResult preparation,
                              LocalDateTime now) {
        historyDao.insert(history);
        stateDao.upsert(state);
        for (AlarmNotificationService.PreparedDelivery prepared : preparation.deliveries()) {
            AlarmNotificationOutbox delivery = AlarmNotificationOutbox.pending(
                    history.getId(), prepared.channelId(), prepared.methodType(), prepared.payload(), now);
            outboxDao.insert(delivery);
        }
    }

    private void lockRule(Long ruleId) {
        if (ruleDao.selectRuleByIdForUpdate(ruleId) == null) {
            throw new IllegalStateException("Alarm rule not found: ruleId=" + ruleId);
        }
    }

    private AlarmState getOrCreateState(Long ruleId) {
        AlarmState state = stateDao.selectByRuleId(ruleId);
        return state != null ? state : new AlarmState(ruleId);
    }

    private boolean shouldEnqueue(AlarmState state,
                                  LocalDateTime now,
                                  int actionIntervalSec) {
        if (state.getLastNotificationEnqueuedAt() == null) {
            return true;
        }
        return !now.isBefore(state.getLastNotificationEnqueuedAt().plusSeconds(actionIntervalSec));
    }

    private boolean isOneShotRule(List<AlarmCondition> leaves) {
        return !leaves.isEmpty() && leaves.stream()
                .allMatch(leaf -> leaf.getTrigger() == AlarmCondition.Trigger.NEW_GROUP);
    }

    private AlarmRuleV2 buildCheckFailedNotificationRule(AlarmRuleV2 rule, RuntimeException failure) {
        return CheckFailedNotificationRuleBuilder.from(rule)
                .name("Alarm check failed: " + ruleLabel(rule))
                .description(rootCauseMessage(failure))
                .severity(rule.getSeverity() != null ? rule.getSeverity() : AlarmSeverity.CRITICAL)
                .checkIntervalSec(positiveOrDefault(
                        rule.getCheckIntervalSec(), DEFAULT_CHECK_FAILURE_RETRY_SEC))
                .actionIntervalSec(positiveOrDefault(
                        rule.getActionIntervalSec(), DEFAULT_CHECK_FAILURE_ACTION_INTERVAL_SEC))
                .filters(rule.getFilters() != null ? rule.getFilters() : List.of())
                .build();
    }

    /**
     * Names a rule for a CHECK_FAILED record.
     *
     * <p>A rule that failed to resolve is the raw row, and no rule stores its name there --
     * a bundle rule inherits it from the item, a standalone one keeps it in its local config --
     * so the name is null exactly when we are about to report the failure. Falling back to the
     * id keeps the alert identifiable instead of titling it "[CHECK_FAILED] null".
     */
    private static String ruleLabel(AlarmRuleV2 rule) {
        return StringUtils.hasText(rule.getName()) ? rule.getName() : "rule#" + rule.getId();
    }

    private static int positiveOrDefault(Integer value, int defaultValue) {
        return value != null && value > 0 ? value : defaultValue;
    }

    private String buildContext(AlarmRuleV2 rule,
                                MetricQueryResult metricResults,
                                List<AlarmCondition> leaves,
                                AlarmNotificationService.PreparationResult preparation) {
        List<ConditionResult> results = new ArrayList<>(leaves.size());
        for (AlarmCondition leaf : leaves) {
            MetricQueryKey key = MetricQueryKey.from(leaf);
            results.add(ConditionResult.from(leaf, key,
                    metricResults.values().get(key), metricResults.details(key)));
        }
        AlarmContext context = new AlarmContext(results, EffectiveRuleSnapshot.from(rule),
                rule.getServiceName(), rule.getApplicationName(),
                rule.getDataSource(),
                NotificationResult.pending(preparation));
        return serialize(alarmContextWriter, context, "alarm context");
    }

    private String buildCheckFailedContext(AlarmRuleV2 rule,
                                           RuntimeException failure,
                                           AlarmNotificationService.PreparationResult preparation) {
        CheckFailedContext context = new CheckFailedContext(
                new FailureSnapshot(failure.getClass().getName(), rootCauseMessage(failure)),
                EffectiveRuleSnapshot.from(rule),
                rule.getServiceName(),
                rule.getApplicationName(),
                rule.getDataSource(),
                NotificationResult.pending(preparation));
        return serialize(checkFailedContextWriter, context, "alarm check failure context");
    }

    private String serialize(ObjectWriter writer, Object value, String description) {
        try {
            return writer.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize {}", description, e);
            return "{}";
        }
    }

    private record AlarmContext(List<ConditionResult> results,
                                @JsonProperty("effective_rule") EffectiveRuleSnapshot effectiveRule,
                                @JsonProperty("service_name") String serviceName,
                                @JsonProperty("application_name") String applicationName,
                                @JsonProperty("data_source") String dataSource,
                                NotificationResult notification) {
    }

    private record CheckFailedContext(FailureSnapshot failure,
                                      @JsonProperty("effective_rule") EffectiveRuleSnapshot effectiveRule,
                                      @JsonProperty("service_name") String serviceName,
                                      @JsonProperty("application_name") String applicationName,
                                      @JsonProperty("data_source") String dataSource,
                                      NotificationResult notification) {
    }

    private record ConditionResult(String metric,
                                   @JsonProperty("metric_key") String metricKey,
                                   AlarmCondition.Trigger trigger,
                                   @JsonProperty("window_sec") Integer windowSec,
                                   AlarmCondition.Aggregation aggregation,
                                   Double value,
                                   Double threshold,
                                   AlarmCondition.ComparisonOp op,
                                   @JsonInclude(JsonInclude.Include.NON_EMPTY)
                                   List<String> details) {
        private static ConditionResult from(AlarmCondition leaf, MetricQueryKey key, Double value,
                                            List<String> details) {
            return new ConditionResult(leaf.getMetric(), key.label(), leaf.getTrigger(), leaf.getWindowSec(),
                    leaf.getAggregation(), value, leaf.getThreshold(), leaf.getOp(), details);
        }
    }

    private record NotificationResult(int sent,
                                      int failed,
                                      int pending,
                                      @JsonProperty("preparation_failed")
                                      int preparationFailed,
                                      @JsonProperty("preparation_failures")
                                      @JsonInclude(JsonInclude.Include.NON_EMPTY)
                                      List<PreparationFailureSnapshot> preparationFailures,
                                      @JsonProperty("skipped_reason")
                                      @JsonInclude(JsonInclude.Include.NON_NULL)
                                      String skippedReason) {
        private static NotificationResult pending(AlarmNotificationService.PreparationResult preparation) {
            List<PreparationFailureSnapshot> failures = preparation.failures().stream()
                    .map(PreparationFailureSnapshot::from)
                    .toList();
            return new NotificationResult(0, 0, preparation.deliveries().size(), failures.size(), failures,
                    preparation.skippedReason());
        }
    }

    private record PreparationFailureSnapshot(
            @JsonProperty("channel_id")
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Long channelId,
            String message) {
        private static PreparationFailureSnapshot from(
                AlarmNotificationService.PreparationFailure failure) {
            return new PreparationFailureSnapshot(failure.channelId(), failure.message());
        }
    }

    private record FailureSnapshot(String type, String message) {
    }

    private static final class CheckFailedNotificationRuleBuilder {

        private final AlarmRuleV2 source;
        private String name;
        private String description;
        private AlarmSeverity severity;
        private Integer checkIntervalSec;
        private Integer actionIntervalSec;
        private List<AlarmFilter> filters;

        private CheckFailedNotificationRuleBuilder(AlarmRuleV2 source) {
            this.source = Objects.requireNonNull(source, "source");
        }

        private static CheckFailedNotificationRuleBuilder from(AlarmRuleV2 source) {
            return new CheckFailedNotificationRuleBuilder(source);
        }

        private CheckFailedNotificationRuleBuilder name(String name) {
            this.name = Objects.requireNonNull(name, "name");
            return this;
        }

        private CheckFailedNotificationRuleBuilder description(String description) {
            this.description = Objects.requireNonNull(description, "description");
            return this;
        }

        private CheckFailedNotificationRuleBuilder severity(AlarmSeverity severity) {
            this.severity = Objects.requireNonNull(severity, "severity");
            return this;
        }

        private CheckFailedNotificationRuleBuilder checkIntervalSec(int checkIntervalSec) {
            this.checkIntervalSec = requirePositive(checkIntervalSec, "checkIntervalSec");
            return this;
        }

        private CheckFailedNotificationRuleBuilder actionIntervalSec(int actionIntervalSec) {
            this.actionIntervalSec = requirePositive(actionIntervalSec, "actionIntervalSec");
            return this;
        }

        private CheckFailedNotificationRuleBuilder filters(List<AlarmFilter> filters) {
            this.filters = Objects.requireNonNull(filters, "filters");
            return this;
        }

        private AlarmRuleV2 build() {
            AlarmRuleV2 notificationRule = new AlarmRuleV2();
            notificationRule.setId(Objects.requireNonNull(source.getId(), "source.id"));
            notificationRule.setName(Objects.requireNonNull(name, "name"));
            notificationRule.setDescription(Objects.requireNonNull(description, "description"));
            notificationRule.setSeverity(Objects.requireNonNull(severity, "severity"));
            notificationRule.setDataSource(source.getDataSource());
            notificationRule.setApplicationType(source.getApplicationType());
            notificationRule.setTemplateId(source.getTemplateId());
            notificationRule.setTemplateName(source.getTemplateName());
            notificationRule.setServiceName(source.getServiceName());
            notificationRule.setApplicationName(source.getApplicationName());
            notificationRule.setCheckIntervalSec(Objects.requireNonNull(checkIntervalSec, "checkIntervalSec"));
            notificationRule.setActionIntervalSec(Objects.requireNonNull(actionIntervalSec, "actionIntervalSec"));
            notificationRule.setConditions(source.getConditions());
            notificationRule.setFilters(Objects.requireNonNull(filters, "filters"));
            notificationRule.setEnabled(source.isEnabled());
            notificationRule.setUpdatedAt(source.getUpdatedAt());
            return notificationRule;
        }

        private static int requirePositive(int value, String name) {
            if (value <= 0) {
                throw new IllegalArgumentException(name + " must be positive");
            }
            return value;
        }
    }

    private record EffectiveRuleSnapshot(Long id,
                                         String name,
                                         String description,
                                         String severity,
                                         @JsonProperty("data_source") String dataSource,
                                         @JsonProperty("template_id") Long templateId,
                                         @JsonProperty("template_name") String templateName,
                                         @JsonProperty("service_name") String serviceName,
                                         @JsonProperty("application_name") String applicationName,
                                         @JsonProperty("check_interval_sec") Integer checkIntervalSec,
                                         @JsonProperty("action_interval_sec") Integer actionIntervalSec,
                                         AlarmCondition conditions,
                                         List<AlarmFilter> filters,
                                         boolean enabled,
                                         @JsonProperty("override_keys") List<String> overrideKeys) {
        private static EffectiveRuleSnapshot from(AlarmRuleV2 rule) {
            Objects.requireNonNull(rule, "rule");
            return new EffectiveRuleSnapshot(rule.getId(), rule.getName(), rule.getDescription(),
                    rule.getSeverity() == null ? null : rule.getSeverity().name(),
                    rule.getDataSource(),
                    rule.getTemplateId(), rule.getTemplateName(), rule.getServiceName(), rule.getApplicationName(),
                    rule.getCheckIntervalSec(), rule.getActionIntervalSec(), rule.getConditions(), rule.getFilters(),
                    rule.isEnabled(), rule.getOverrideKeys());
        }
    }
}
