package com.navercorp.pinpoint.alarm.service;

import com.navercorp.pinpoint.alarm.evaluation.ConditionEvaluator;
import com.navercorp.pinpoint.alarm.evaluation.ConditionUtils;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryService;
import com.navercorp.pinpoint.alarm.dao.AlarmStateDao;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

/**
 * Evaluates metrics and delegates durable state transitions to the event persistence service.
 */
@Service
public class AlarmEvaluationService {

    private final AlarmStateDao stateDao;
    private final AlarmEventPersistenceService eventPersistenceService;
    private final ConditionEvaluator conditionEvaluator;

    public AlarmEvaluationService(AlarmStateDao stateDao,
                                  AlarmEventPersistenceService eventPersistenceService,
                                  ConditionEvaluator conditionEvaluator) {
        this.stateDao = Objects.requireNonNull(stateDao, "stateDao");
        this.eventPersistenceService = Objects.requireNonNull(eventPersistenceService, "eventPersistenceService");
        this.conditionEvaluator = Objects.requireNonNull(conditionEvaluator, "conditionEvaluator");
    }

    /**
     * @return true if the alarm fired
     */
    public boolean evaluate(AlarmRuleV2 rule, MetricQueryService metricQueryService) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        AlarmState state = stateDao.selectByRuleId(rule.getId());
        if (state == null) {
            state = new AlarmState(rule.getId());
        }

        if (rule.getConditions() == null) {
            throw new IllegalArgumentException("rule.conditions must not be null");
        }
        List<AlarmCondition> leaves = ConditionUtils.extractLeaves(rule.getConditions());
        MetricQueryResult metricResults = metricQueryService.query(
                rule, leaves, rule.getFilters(), state);
        boolean fired = conditionEvaluator.evaluate(rule.getConditions(), metricResults.values());

        if (fired) {
            eventPersistenceService.recordFired(rule, metricResults, leaves, now);
        } else {
            eventPersistenceService.recordResolved(rule, leaves, now);
        }
        return fired;
    }

    public void handleRuleConfigurationFailed(AlarmRuleV2 rule, RuntimeException failure) {
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(failure, "failure");
        eventPersistenceService.recordCheckFailed(rule, failure, LocalDateTime.now(ZoneOffset.UTC));
    }

    public void handleInfrastructureFailed(AlarmRuleV2 rule, RuntimeException failure) {
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(failure, "failure");
        eventPersistenceService.recordCheckFailedSilently(rule, LocalDateTime.now(ZoneOffset.UTC));
    }
}
