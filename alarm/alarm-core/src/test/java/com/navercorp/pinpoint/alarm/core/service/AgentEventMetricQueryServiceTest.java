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
package com.navercorp.pinpoint.alarm.core.service;

import com.navercorp.pinpoint.alarm.evaluation.ConditionEvaluator;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryKey;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventQuery;
import com.navercorp.pinpoint.common.server.dao.AgentEventDao;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * That a deadlock rule fires on the event being there at all, and resolves once it is not.
 *
 * <p>The operator picks the metric and nothing else -- no operator, no threshold -- so the
 * tests are written against what the evaluator does with the value rather than only against
 * the value, since the two together are the behaviour.
 */
class AgentEventMetricQueryServiceTest {

    private static final RuleApplicationResolver RESOLVER =
            rule -> new Application(rule.getApplicationName(), ServiceType.TEST_STAND_ALONE);

    @Test
    void anAgentReportingADeadlockCounts() {
        StubDao dao = new StubDao();
        dao.deadlocked("agent-b");

        MetricQueryResult result = query(dao, List.of("agent-a", "agent-b"));

        assertEquals(1.0, value(result));
        assertTrue(fires(result), "any deadlock should fire the rule");
    }

    @Test
    void everyDeadlockedAgentIsCounted() {
        StubDao dao = new StubDao();
        dao.deadlocked("agent-a");
        dao.deadlocked("agent-c");

        assertEquals(2.0, value(query(dao, List.of("agent-a", "agent-b", "agent-c"))));
    }

    @Test
    void repeatedEventsFromOneAgentStillCountOnce() {
        // The count is of agents to look at, not of events: a monitor that reports the same
        // deadlock twice in a window should not read as two deadlocked agents.
        StubDao dao = new StubDao();
        dao.deadlocked("agent-a");
        dao.deadlocked("agent-a");

        assertEquals(1.0, value(query(dao, List.of("agent-a"))));
    }

    @Test
    void theDeadlockedAgentsAreNamed() {
        StubDao dao = new StubDao();
        dao.deadlocked("agent-b");

        MetricQueryResult result = query(dao, List.of("agent-a", "agent-b"));
        MetricQueryKey key = result.values().keySet().iterator().next();

        assertTrue(result.details(key).get(0).contains("agent-b"),
                "the detail should name the deadlocked agent, was " + result.details(key));
        assertFalse(result.details(key).get(0).contains("agent-a"),
                "a healthy agent should not be named, was " + result.details(key));
    }

    @Test
    void noDeadlockYieldsNoValueSoAFiringRuleResolves() {
        MetricQueryResult result = query(new StubDao(), List.of("agent-a"));

        assertTrue(result.isEmpty());
        assertFalse(fires(result), "with nothing reported the condition should not be met");
    }

    @Test
    void anApplicationWithNoAgentReportsNothing() {
        assertTrue(query(new StubDao(), List.of()).isEmpty());
    }

    @Test
    void anUnknownMetricIsSkippedRatherThanFailingTheWholeRule() {
        StubDao dao = new StubDao();
        dao.deadlocked("agent-a");

        MetricQueryResult result =
                query(dao, List.of("agent-a"), List.of("no_such_metric", "deadlock_count"));

        assertEquals(1, result.values().size());
        assertEquals(1.0, value(result));
    }

    @Test
    void aWindowlessLeafFallsBackToTheRuleCheckInterval() {
        // A NEW_GROUP leaf is not required to carry a window, and the validator lets it
        // through without one, so the rule's interval has to cover it.
        StubDao dao = new StubDao();
        dao.deadlocked("agent-a");

        MetricQueryResult result = query(dao, List.of("agent-a"), List.of("deadlock_count"), null);

        assertEquals(300_000L, result.range().toMs() - result.range().fromMs());
    }

    // ---- helpers ----

    private MetricQueryResult query(AgentEventDao dao, List<String> agentIds) {
        return query(dao, agentIds, List.of("deadlock_count"));
    }

    private MetricQueryResult query(AgentEventDao dao, List<String> agentIds,
                                    List<String> metrics) {
        return query(dao, agentIds, metrics, 300);
    }

    private MetricQueryResult query(AgentEventDao dao, List<String> agentIds,
                                    List<String> metrics, Integer windowSec) {
        AgentEventMetricQueryService service =
                new AgentEventMetricQueryService(dao, (application, window) -> agentIds, RESOLVER);

        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(1L);
        rule.setApplicationName("app");
        rule.setCheckIntervalSec(300);

        List<AlarmCondition> conditions = metrics.stream().map(metric -> {
            AlarmCondition leaf = new AlarmCondition();
            leaf.setType(AlarmCondition.Type.LEAF);
            leaf.setMetric(metric);
            leaf.setWindowSec(windowSec);
            leaf.setTrigger(AlarmCondition.Trigger.NEW_GROUP);
            return leaf;
        }).toList();

        return service.query(rule, conditions, List.of(), null);
    }

    /** What the evaluator makes of the result, which for this metric is the whole condition. */
    private boolean fires(MetricQueryResult result) {
        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setMetric("deadlock_count");
        leaf.setWindowSec(300);
        leaf.setTrigger(AlarmCondition.Trigger.NEW_GROUP);

        return new ConditionEvaluator().evaluate(leaf, result.values());
    }

    private Double value(MetricQueryResult result) {
        return result.values().values().stream().findFirst()
                .orElseThrow(() -> new AssertionError("no value was read"));
    }

    private static class StubDao implements AgentEventDao {
        private final Map<String, List<AgentEventBo>> events = new HashMap<>();

        void deadlocked(String agentId) {
            events.computeIfAbsent(agentId, key -> new ArrayList<>())
                    .add(new AgentEventBo(agentId, 0L, 1L,
                            AgentEventType.AGENT_DEADLOCK_DETECTED));
        }

        @Override
        public AgentEventBo getAgentEvent(String agentId, long eventTimestamp,
                                          AgentEventType eventType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<AgentEventBo> getAgentEvents(String agentId, Range range,
                                                 AgentEventQuery filter) {
            return events.getOrDefault(agentId, List.of());
        }
    }
}
