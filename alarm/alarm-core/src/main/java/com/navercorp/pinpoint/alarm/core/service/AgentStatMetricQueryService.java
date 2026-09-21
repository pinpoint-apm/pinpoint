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

import com.navercorp.pinpoint.alarm.core.agentstat.dao.AgentStatAlarmDao;
import com.navercorp.pinpoint.alarm.core.agentstat.vo.AgentFieldUsage;
import com.navercorp.pinpoint.alarm.core.agentstat.vo.AgentUsage;
import com.navercorp.pinpoint.alarm.core.agentstat.vo.AgentUsageCount;
import com.navercorp.pinpoint.alarm.core.vo.CoreAlarmDataSource;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryKey;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryService;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Reads what the agents of an application reported about themselves.
 *
 * <p>Every value here belongs to one agent, and a rule is written against the application. The
 * aggregation on the condition says how the agents are reduced to the one number a threshold
 * is compared against: MAX with a ceiling threshold is the v1 alarm, which fired when any one
 * agent exceeded. The agents that carried the reduced value are reported as detail beside it,
 * so the notification names them rather than only saying the application is unwell.
 */
public class AgentStatMetricQueryService implements MetricQueryService {

    private static final Logger logger = LogManager.getLogger(AgentStatMetricQueryService.class);

    private static final String METRIC_JVM_GC = "jvmGc";
    private static final String METRIC_CPU_LOAD = "cpuLoad";
    private static final String METRIC_FILE_DESCRIPTOR = "fileDescriptor";

    private static final String FIELD_HEAP_USED = "heapUsed";
    private static final String FIELD_HEAP_MAX = "heapMax";
    private static final String FIELD_JVM = "jvm";
    private static final String FIELD_SYSTEM = "system";
    private static final String FIELD_OPEN_FILE_DESCRIPTOR = "openFileDescriptorCount";

    /** How many agents a detail line names before it stops listing them. */
    private static final int MAX_DETAIL_AGENTS = 10;

    private final AgentStatAlarmDao agentStatAlarmDao;
    private final AgentIdsResolver agentIdsResolver;
    private final RuleApplicationResolver applicationResolver;
    private final ConnectionPoolUsageReader connectionPoolUsageReader;

    public AgentStatMetricQueryService(AgentStatAlarmDao agentStatAlarmDao,
                                       AgentIdsResolver agentIdsResolver,
                                       RuleApplicationResolver applicationResolver) {
        this.agentStatAlarmDao = Objects.requireNonNull(agentStatAlarmDao, "agentStatAlarmDao");
        this.agentIdsResolver = Objects.requireNonNull(agentIdsResolver, "agentIdsResolver");
        this.applicationResolver = Objects.requireNonNull(applicationResolver, "applicationResolver");
        this.connectionPoolUsageReader = new ConnectionPoolUsageReader(agentStatAlarmDao);
    }

    @Override
    public AlarmDataSource getDataSource() {
        return CoreAlarmDataSource.AGENT_STAT;
    }

    @Override
    public MetricQueryResult query(AlarmRuleV2 rule, List<AlarmCondition> conditions,
                                   List<AlarmFilter> filters, AlarmState state) {
        long nowMs = System.currentTimeMillis();
        Application application = applicationResolver.resolve(rule);
        String applicationName = application.getApplicationName();

        Map<MetricQueryKey, Double> values = new LinkedHashMap<>();
        Map<MetricQueryKey, List<String>> details = new LinkedHashMap<>();
        // Two leaves differing only in how they reduce are two keys but one read; the dedup
        // above is keyed by the whole condition and would issue the query once per each.
        Map<MetricRead, Map<String, Double>> reads = new HashMap<>();
        MetricQueryResult.QueriedRange range = null;

        for (AlarmCondition leaf : conditions) {
            MetricQueryKey key = MetricQueryKey.from(leaf);
            if (values.containsKey(key)) {
                continue;
            }
            int windowSec = windowSecOf(rule, leaf);
            Range window = Range.between(nowMs - windowSec * 1000L, nowMs);

            Map<String, Double> byAgent = reads.computeIfAbsent(
                    new MetricRead(leaf.getMetric(), windowSec),
                    read -> readByAgent(read.metric(), application, window));
            if (byAgent == null) {
                logger.warn("Unknown {} metric='{}', rule_id={}, skipping",
                        getDataSource().name(), leaf.getMetric(), rule.getId());
                continue;
            }
            if (byAgent.isEmpty()) {
                // No agent reported in the window. Reporting zero would read as "healthy",
                // which is the opposite of what an application with no live agent means, so
                // the metric is left out and the condition sees no value.
                logger.info("No agent reported {} for application={} in the last {}s, rule_id={}",
                        leaf.getMetric(), applicationName, windowSec, rule.getId());
                continue;
            }

            Reduction reduction = reduce(byAgent, leaf.getAggregation());
            values.put(key, reduction.value());
            details.put(key, reduction.detail());
            range = MetricQueryResult.QueriedRange.union(range,
                    new MetricQueryResult.QueriedRange(window.getFrom(), window.getTo()));
        }
        return new MetricQueryResult(values, details, range);
    }

    /**
     * @return the value per agent -- or per pool of each agent, for the connection metric --
     *         or null when the metric is not one of this data source's
     */
    private Map<String, Double> readByAgent(String metric, Application application, Range window) {
        String applicationName = application.getApplicationName();
        return switch (metric == null ? "" : metric) {
            case "heap_usage_rate" -> heapUsageRate(applicationName, window);
            case "jvm_cpu_usage_rate" -> cpuUsageRate(applicationName, FIELD_JVM, window);
            case "system_cpu_usage_rate" -> cpuUsageRate(applicationName, FIELD_SYSTEM, window);
            case "file_descriptor_count" -> fileDescriptorCount(applicationName, window);
            case "datasource_connection_usage_rate" -> connectionPoolUsageReader.read(
                    applicationName, agentIdsResolver.resolve(application, window), window);
            default -> null;
        };
    }

    /** Used over max, as a percentage, the way the v1 collector reported it. */
    private Map<String, Double> heapUsageRate(String applicationName, Range window) {
        List<AgentFieldUsage> usages = agentStatAlarmDao.selectSumGroupByField(
                applicationName, METRIC_JVM_GC, List.of(FIELD_HEAP_MAX, FIELD_HEAP_USED), window);

        Map<String, Double> used = new HashMap<>();
        Map<String, Double> max = new HashMap<>();
        for (AgentFieldUsage usage : usages) {
            if (FIELD_HEAP_USED.equals(usage.getFieldName())) {
                used.put(usage.getAgentId(), usage.getValue());
            } else if (FIELD_HEAP_MAX.equals(usage.getFieldName())) {
                max.put(usage.getAgentId(), usage.getValue());
            }
        }
        Map<String, Double> rates = new HashMap<>();
        for (Map.Entry<String, Double> entry : used.entrySet()) {
            Double heapMax = max.get(entry.getKey());
            if (heapMax == null || heapMax <= 0) {
                // No ceiling, no rate. Recording zero would read as an empty heap and MIN
                // would pick it every time. Same rule as the connection pools.
                continue;
            }
            rates.put(entry.getKey(), percentOf(entry.getValue(), heapMax));
        }
        return rates;
    }

    /**
     * The mean load over the window, as a percentage.
     *
     * <p>A stored sample is a 0-1 load -- the inspector screen scales it by a hundred to show
     * it -- so the mean is the summed load over the number of samples, and the percentage is
     * that times a hundred.
     *
     * <p>This does not match what the v1 alarm computed. It divided the summed load by a
     * hundred times the sample count in integer arithmetic, which is the mean without the
     * scaling and then truncated: since a mean load is between zero and one, the result was
     * always zero. Its threshold is an Integer and its comparison a Long, so no entry could
     * have recovered the lost scale. Copying that to keep the numbers identical would carry
     * a checker that cannot fire.
     */
    private Map<String, Double> cpuUsageRate(String applicationName, String field, Range window) {
        List<AgentUsageCount> counts =
                agentStatAlarmDao.selectSumCount(applicationName, METRIC_CPU_LOAD, field, window);

        Map<String, Double> rates = new HashMap<>();
        for (AgentUsageCount count : counts) {
            Double samples = count.getCountValue();
            rates.put(count.getAgentId(),
                    percentOf(count.getValue(), samples == null ? 0 : samples));
        }
        return rates;
    }

    private Map<String, Double> fileDescriptorCount(String applicationName, Range window) {
        List<AgentUsage> usages = agentStatAlarmDao.selectAvg(
                applicationName, METRIC_FILE_DESCRIPTOR, FIELD_OPEN_FILE_DESCRIPTOR, window);

        Map<String, Double> counts = new HashMap<>();
        for (AgentUsage usage : usages) {
            counts.put(usage.getAgentId(), usage.getValue() == null ? 0 : usage.getValue());
        }
        return counts;
    }

    private static double percentOf(Double value, Double total) {
        return percentOf(value, total == null ? 0 : total);
    }

    private static double percentOf(Double value, double total) {
        if (value == null || total == 0) {
            return 0;
        }
        return (value * 100.0) / total;
    }

    /**
     * Reduces the agents to one number and names the ones it came from. MAX is the default
     * because that is what the v1 alarm compared, and a rule written without an aggregation
     * was written against that behaviour.
     */
    private Reduction reduce(Map<String, Double> byAgent, AlarmCondition.Aggregation aggregation) {
        AlarmCondition.Aggregation reducer =
                aggregation != null ? aggregation : AlarmCondition.Aggregation.MAX;

        return switch (reducer) {
            case MIN -> extremum(byAgent, Comparator.naturalOrder());
            case AVG -> average(byAgent);
            default -> extremum(byAgent, Comparator.reverseOrder());
        };
    }

    /** Names every agent that reported the value being compared, not just the first one found. */
    private Reduction extremum(Map<String, Double> byAgent, Comparator<Double> order) {
        double picked = byAgent.values().stream().min(order).orElse(0.0);
        List<String> agents = new ArrayList<>();
        for (Map.Entry<String, Double> entry : byAgent.entrySet()) {
            if (entry.getValue() == picked) {
                agents.add(entry.getKey());
            }
        }
        return new Reduction(picked, detailOf(agents, picked));
    }

    private Reduction average(Map<String, Double> byAgent) {
        double average = byAgent.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return new Reduction(average,
                List.of(String.format("averaged over %d agent(s)", byAgent.size())));
    }

    private List<String> detailOf(List<String> agents, double value) {
        List<String> named = agents.size() > MAX_DETAIL_AGENTS
                ? agents.subList(0, MAX_DETAIL_AGENTS) : agents;
        String suffix = agents.size() > MAX_DETAIL_AGENTS
                ? String.format(" and %d more", agents.size() - MAX_DETAIL_AGENTS) : "";
        return List.of(String.format("%s%s: %.2f", String.join(", ", named), suffix, value));
    }

    /** What a read is decided by: the same pair asks the store the same question. */
    private record MetricRead(String metric, int windowSec) {
    }

    private record Reduction(double value, List<String> detail) {
    }
}
