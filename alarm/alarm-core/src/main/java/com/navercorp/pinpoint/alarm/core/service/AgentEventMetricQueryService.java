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

import com.navercorp.pinpoint.alarm.core.vo.CoreAlarmDataSource;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryKey;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryService;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventQuery;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.dao.AgentEventDao;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Reads the events the agents of an application reported about themselves.
 *
 * <p>Only deadlock is exposed. The value is the number of agents that reported a deadlocked
 * thread in the rule's window, and the detail names them, so the notification says which agent
 * to look at. The metric's NEW_GROUP trigger has the evaluator fire on any value above zero,
 * so the count never faces a threshold -- it exists to be non-zero, and to be countable in the
 * message.
 *
 * <p>The events are stored per agent, so the agents have to be listed first. An application
 * with no live agent reports nothing rather than zero: the metric is left out and the condition
 * reads as not met, which is also how a previously firing rule resolves once the deadlock is
 * gone.
 */
public class AgentEventMetricQueryService implements MetricQueryService {

    private static final Logger logger = LogManager.getLogger(AgentEventMetricQueryService.class);

    private static final String METRIC_DEADLOCK_COUNT = "deadlock_count";

    private static final AgentEventQuery DEADLOCK =
            AgentEventQuery.include(Set.of(AgentEventType.AGENT_DEADLOCK_DETECTED));

    /** How many agents a detail line names before it stops listing them. */
    private static final int MAX_DETAIL_AGENTS = 10;

    private final AgentEventDao agentEventDao;
    private final AgentIdsResolver agentIdsResolver;
    private final RuleApplicationResolver applicationResolver;

    public AgentEventMetricQueryService(AgentEventDao agentEventDao,
                                        AgentIdsResolver agentIdsResolver,
                                        RuleApplicationResolver applicationResolver) {
        this.agentEventDao = Objects.requireNonNull(agentEventDao, "agentEventDao");
        this.agentIdsResolver = Objects.requireNonNull(agentIdsResolver, "agentIdsResolver");
        this.applicationResolver = Objects.requireNonNull(applicationResolver, "applicationResolver");
    }

    @Override
    public AlarmDataSource getDataSource() {
        return CoreAlarmDataSource.AGENT_EVENT;
    }

    @Override
    public MetricQueryResult query(AlarmRuleV2 rule, List<AlarmCondition> conditions,
                                   List<AlarmFilter> filters, AlarmState state) {
        long nowMs = System.currentTimeMillis();
        Application application = applicationResolver.resolve(rule);

        Map<MetricQueryKey, Double> values = new LinkedHashMap<>();
        Map<MetricQueryKey, List<String>> details = new LinkedHashMap<>();
        MetricQueryResult.QueriedRange range = null;

        for (AlarmCondition leaf : conditions) {
            if (!METRIC_DEADLOCK_COUNT.equals(leaf.getMetric())) {
                logger.warn("Unknown {} metric='{}', rule_id={}, skipping",
                        getDataSource().name(), leaf.getMetric(), rule.getId());
                continue;
            }
            MetricQueryKey key = MetricQueryKey.from(leaf);
            if (values.containsKey(key)) {
                continue;
            }
            int windowSec = windowSecOf(rule, leaf);
            Range window = Range.between(nowMs - windowSec * 1000L, nowMs);

            List<String> deadlocked = deadlockedAgents(application, window);
            if (deadlocked.isEmpty()) {
                continue;
            }
            values.put(key, (double) deadlocked.size());
            details.put(key, detailOf(deadlocked));
            range = MetricQueryResult.QueriedRange.union(range,
                    new MetricQueryResult.QueriedRange(window.getFrom(), window.getTo()));
        }
        return new MetricQueryResult(values, details, range);
    }

    private List<String> deadlockedAgents(Application application, Range window) {
        List<String> deadlocked = new ArrayList<>();
        for (String agentId : agentIdsResolver.resolve(application, window)) {
            if (!agentEventDao.getAgentEvents(agentId, window, DEADLOCK).isEmpty()) {
                deadlocked.add(agentId);
            }
        }
        return deadlocked;
    }

    private List<String> detailOf(List<String> agents) {
        List<String> named = agents.size() > MAX_DETAIL_AGENTS
                ? agents.subList(0, MAX_DETAIL_AGENTS) : agents;
        String suffix = agents.size() > MAX_DETAIL_AGENTS
                ? String.format(" and %d more", agents.size() - MAX_DETAIL_AGENTS) : "";
        return List.of(String.format("deadlocked thread detected on %s%s",
                String.join(", ", named), suffix));
    }
}
