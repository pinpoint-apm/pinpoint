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
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryKey;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * That a rule written against an application still reads its agents the way the v1 alarm did.
 *
 * <p>Every value here belongs to one agent, so the interesting part is the reduction: without
 * an aggregation a rule gets the maximum, which is what the v1 alarm compared when it fired
 * because any one agent exceeded. The agents behind the value are named, so a notification
 * says which ones rather than only that the application is unwell.
 */
class AgentStatMetricQueryServiceTest {

    private static final RuleApplicationResolver RESOLVER =
            rule -> new Application(rule.getApplicationName(), ServiceType.TEST_STAND_ALONE);

    @Test
    void heapUsageRateIsUsedOverMax() {
        StubDao dao = new StubDao();
        dao.fieldUsages = List.of(
                new AgentFieldUsage("agent-a", "heapUsed", 300.0),
                new AgentFieldUsage("agent-a", "heapMax", 1000.0));

        assertEquals(30.0, value(query(dao, "heap_usage_rate", null), "heap_usage_rate"));
    }

    @Test
    void cpuUsageRateIsTheMeanLoadScaledToAPercentage() {
        // A stored sample is a 0-1 load, so 45 summed over 100 samples is a mean of 0.45,
        // which is 45%. The v1 alarm divided in integer arithmetic and reported 0 here.
        StubDao dao = new StubDao();
        dao.usageCounts = List.of(new AgentUsageCount("agent-a", 45.0, 100.0));

        assertEquals(45.0, value(query(dao, "jvm_cpu_usage_rate", null), "jvm_cpu_usage_rate"));
    }

    @Test
    void withNoAggregationTheWorstAgentIsCompared() {
        // The v1 alarm fired when any one agent exceeded, which is the maximum.
        StubDao dao = new StubDao();
        dao.usages = List.of(
                new AgentUsage("agent-a", 120.0),
                new AgentUsage("agent-b", 8000.0),
                new AgentUsage("agent-c", 300.0));

        assertEquals(8000.0,
                value(query(dao, "file_descriptor_count", null), "file_descriptor_count"));
    }

    @Test
    void theAgentsBehindTheValueAreNamed() {
        StubDao dao = new StubDao();
        dao.usages = List.of(
                new AgentUsage("agent-a", 120.0),
                new AgentUsage("agent-b", 8000.0));

        MetricQueryResult result = query(dao, "file_descriptor_count", null);
        MetricQueryKey key = result.values().keySet().iterator().next();

        assertEquals(1, result.details(key).size());
        assertTrue(result.details(key).get(0).contains("agent-b"),
                "the detail should name the agent that carried the value, was "
                        + result.details(key));
    }

    @Test
    void minAndAverageReduceTheSameAgentsDifferently() {
        StubDao dao = new StubDao();
        dao.usages = List.of(
                new AgentUsage("agent-a", 100.0),
                new AgentUsage("agent-b", 300.0));

        assertEquals(100.0, value(
                query(dao, "file_descriptor_count", AlarmCondition.Aggregation.MIN),
                "file_descriptor_count"));
        assertEquals(200.0, value(
                query(dao, "file_descriptor_count", AlarmCondition.Aggregation.AVG),
                "file_descriptor_count"));
    }

    @Test
    void anApplicationWithNoReportingAgentYieldsNoValueRatherThanZero() {
        // Zero would read as healthy, which is the opposite of what no live agent means.
        MetricQueryResult result = query(new StubDao(), "heap_usage_rate", null);

        assertTrue(result.isEmpty());
    }

    @Test
    void anUnknownMetricIsSkippedRatherThanFailingTheWholeRule() {
        StubDao dao = new StubDao();
        dao.usages = List.of(new AgentUsage("agent-a", 5.0));

        MetricQueryResult result = query(dao,
                List.of("file_descriptor_count", "no_such_metric"), null);

        assertEquals(1, result.values().size());
        assertEquals(5.0, value(result, "file_descriptor_count"));
    }

    @Test
    void connectionPoolUsageIsReadPerPoolOfEachAgent() {
        // Two agents, and one of them holds two pools: the reduction runs over all three
        // rather than over the agents, since it is a pool that fills up.
        StubDao dao = new StubDao();
        dao.pool("agent-a", "orders", 5.0, 100.0);
        dao.pool("agent-a", "billing", 90.0, 100.0);
        dao.pool("agent-b", "orders", 20.0, 100.0);

        MetricQueryResult result = query(dao, List.of("datasource_connection_usage_rate"), null,
                List.of("agent-a", "agent-b"));

        assertEquals(90.0, value(result, "datasource_connection_usage_rate"));
    }

    @Test
    void theFullPoolIsNamedByItsDatabaseAndAgent() {
        StubDao dao = new StubDao();
        dao.pool("agent-a", "orders", 5.0, 100.0);
        dao.pool("agent-a", "billing", 90.0, 100.0);

        MetricQueryResult result = query(dao, List.of("datasource_connection_usage_rate"), null);
        MetricQueryKey key = result.values().keySet().iterator().next();

        assertTrue(result.details(key).get(0).contains("agent-a billing"),
                "the detail should name the pool that filled up, was " + result.details(key));
    }

    @Test
    void aPoolReportingNoCeilingIsLeftOut() {
        // Dividing by the ceiling it did not report would fail, and calling it zero percent
        // would report a pool of unknown size as empty.
        StubDao dao = new StubDao();
        dao.pool("agent-a", "orders", 30.0, 0.0);

        assertTrue(query(dao, List.of("datasource_connection_usage_rate"), null).isEmpty());
    }

    // Same metric, two windows: one read each, and the values must not be shared. Counting
    // calls alone would not catch a key that dropped the window -- the second leaf would then
    // be answered with the first leaf's range and nobody would notice.
    @Test
    void oneMetricAtTwoWindowsIsTwoReadsWithTwoAnswers() {
        WindowSensitiveDao dao = new WindowSensitiveDao();

        AgentStatMetricQueryService service = new AgentStatMetricQueryService(
                dao, (application, window) -> List.of("agent-a"), RESOLVER);

        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(1L);
        rule.setApplicationName("app");
        rule.setCheckIntervalSec(300);

        List<AlarmCondition> conditions = Stream.of(300, 3600)
                .map(windowSec -> {
                    AlarmCondition leaf = new AlarmCondition();
                    leaf.setType(AlarmCondition.Type.LEAF);
                    leaf.setMetric("heap_usage_rate");
                    leaf.setWindowSec(windowSec);
                    leaf.setAggregation(AlarmCondition.Aggregation.MAX);
                    return leaf;
                }).toList();

        MetricQueryResult result = service.query(rule, conditions, List.of(), null);

        assertEquals(2, dao.calls, "two windows are two questions");
        List<Double> values = List.copyOf(result.values().values());
        assertEquals(2, values.size());
        assertNotEquals(values.get(0), values.get(1),
                "the second window must not be answered with the first window's data");
    }

    /** Answers in proportion to the window it was asked about. */
    private static class WindowSensitiveDao extends StubDao {
        private int calls;

        @Override
        public List<AgentFieldUsage> selectSumGroupByField(String applicationName, String metricName,
                                                           List<String> fieldList, Range range) {
            calls++;
            double used = (range.getTo() - range.getFrom()) / 1000.0;
            return List.of(new AgentFieldUsage("agent-a", "heapUsed", used),
                    new AgentFieldUsage("agent-a", "heapMax", 10_000.0));
        }
    }

    // An outage is not an empty pool: answering with no value would take a firing rule back
    // to normal for the length of it, which is the one moment the alarm is worth having.
    @Test
    void aPinotFailureIsRaisedRatherThanReadAsNoUsage() {
        StubDao dao = new StubDao() {
            @Override
            public CompletableFuture<List<Tag>> selectTagInfo(String applicationName, String agentId,
                                                              String metricName, String fieldName,
                                                              Range range) {
                return CompletableFuture.failedFuture(
                        new RuntimeException("pinot broker unavailable"));
            }
        };

        assertThrows(IllegalStateException.class,
                () -> query(dao, "datasource_connection_usage_rate", AlarmCondition.Aggregation.MAX));
    }

    // ---- helpers ----

    // MIN picks the smallest rate in the application, so an agent recorded at a false 0%
    // would win every time and 'heap_usage_rate MIN below x' would fire forever.
    @Test
    void anAgentThatReportedNoHeapCeilingIsLeftOut() {
        StubDao dao = new StubDao();
        dao.fieldUsages = List.of(
                new AgentFieldUsage("agent-a", "heapUsed", 300.0),
                new AgentFieldUsage("agent-a", "heapMax", 1000.0),
                // reported its usage but not its ceiling
                new AgentFieldUsage("agent-b", "heapUsed", 500.0));

        MetricQueryResult result = query(dao, List.of("heap_usage_rate"),
                AlarmCondition.Aggregation.MIN, List.of("agent-a", "agent-b"));

        assertEquals(30.0, value(result, "heap_usage_rate"),
                "agent-b has no rate to speak of and must not be read as an empty heap");
    }

    // Two leaves over one metric and one window are two conditions but a single question for
    // the store. Asking it once per aggregation doubles the largest fan-out in this service.
    @Test
    void oneReadServesEveryAggregationOverTheSameWindow() {
        StubDao dao = new StubDao();
        dao.fieldUsages = List.of(
                new AgentFieldUsage("agent-a", "heapUsed", 300.0),
                new AgentFieldUsage("agent-a", "heapMax", 1000.0));

        AgentStatMetricQueryService service = new AgentStatMetricQueryService(
                dao, (application, window) -> List.of("agent-a"), RESOLVER);

        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(1L);
        rule.setApplicationName("app");
        rule.setCheckIntervalSec(300);

        List<AlarmCondition> conditions = Stream.of(
                        AlarmCondition.Aggregation.MAX, AlarmCondition.Aggregation.AVG)
                .map(aggregation -> {
                    AlarmCondition leaf = new AlarmCondition();
                    leaf.setType(AlarmCondition.Type.LEAF);
                    leaf.setMetric("heap_usage_rate");
                    leaf.setWindowSec(300);
                    leaf.setAggregation(aggregation);
                    return leaf;
                }).toList();

        MetricQueryResult result = service.query(rule, conditions, List.of(), null);

        assertEquals(2, result.values().size(), "both aggregations still produce a value");
        assertEquals(1, dao.sumGroupByFieldCalls, "one window, one metric, one read");
    }

    private MetricQueryResult query(AgentStatAlarmDao dao, String metric,
                                    AlarmCondition.Aggregation aggregation) {
        return query(dao, List.of(metric), aggregation);
    }

    private MetricQueryResult query(AgentStatAlarmDao dao, List<String> metrics,
                                    AlarmCondition.Aggregation aggregation) {
        return query(dao, metrics, aggregation, List.of("agent-a"));
    }

    private MetricQueryResult query(AgentStatAlarmDao dao, List<String> metrics,
                                    AlarmCondition.Aggregation aggregation,
                                    List<String> agentIds) {
        AgentStatMetricQueryService service =
                new AgentStatMetricQueryService(dao, (application, window) -> agentIds, RESOLVER);

        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(1L);
        rule.setApplicationName("app");
        rule.setCheckIntervalSec(300);

        List<AlarmCondition> conditions = metrics.stream().map(metric -> {
            AlarmCondition leaf = new AlarmCondition();
            leaf.setType(AlarmCondition.Type.LEAF);
            leaf.setMetric(metric);
            leaf.setWindowSec(300);
            leaf.setAggregation(aggregation);
            return leaf;
        }).toList();

        return service.query(rule, conditions, List.of(), null);
    }

    private Double value(MetricQueryResult result, String metric) {
        return result.values().entrySet().stream()
                .filter(entry -> metric.equals(entry.getKey().metric()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no value for " + metric));
    }

    private static class StubDao implements AgentStatAlarmDao {
        private List<AgentFieldUsage> fieldUsages = List.of();
        /** How many times the store was actually asked, as opposed to how many leaves asked. */
        private int sumGroupByFieldCalls;
        private List<AgentUsageCount> usageCounts = List.of();
        private List<AgentUsage> usages = List.of();
        /** agentId -> the pools it reported, in discovery order */
        private final Map<String, List<String>> pools = new LinkedHashMap<>();
        /** "agentId database" -> active and max connections */
        private final Map<String, double[]> poolUsage = new LinkedHashMap<>();

        void pool(String agentId, String database, double active, double max) {
            pools.computeIfAbsent(agentId, id -> new ArrayList<>()).add(database);
            poolUsage.put(agentId + " " + database, new double[]{active, max});
        }

        @Override
        public List<String> selectAgentIds(String applicationName, String metricName, Range range) {
            return List.copyOf(pools.keySet());
        }

        @Override
        public List<AgentFieldUsage> selectSumGroupByField(String applicationName, String metricName,
                                                           List<String> fieldList, Range range) {
            sumGroupByFieldCalls++;
            return fieldUsages;
        }

        @Override
        public CompletableFuture<List<AgentFieldUsage>> selectAvgGroupByField(
                String applicationName, String agentId, String metricName,
                List<String> fieldList, List<Tag> tagList, Range range) {
            String database = tagList.stream()
                    .filter(tag -> "databaseName".equals(tag.getName()))
                    .map(Tag::getValue).findFirst().orElse("");
            double[] usage = poolUsage.get(agentId + " " + database);
            if (usage == null) {
                return CompletableFuture.completedFuture(fieldUsages);
            }
            return CompletableFuture.completedFuture(List.of(
                    new AgentFieldUsage(agentId, "activeConnectionSize", usage[0]),
                    new AgentFieldUsage(agentId, "maxConnectionSize", usage[1])));
        }

        @Override
        public List<AgentUsageCount> selectSumCount(String applicationName, String metricName,
                                                    String fieldName, Range range) {
            return usageCounts;
        }

        @Override
        public List<AgentUsage> selectAvg(String applicationName, String metricName,
                                          String fieldName, Range range) {
            return usages;
        }

        /** A pool is discovered by the url it reported; other tags come back alongside. */
        @Override
        public CompletableFuture<List<Tag>> selectTagInfo(String applicationName, String agentId,
                                                          String metricName, String fieldName,
                                                          Range range) {
            List<Tag> tags = new ArrayList<>();
            for (String database : pools.getOrDefault(agentId, List.of())) {
                tags.add(new Tag("jdbcUrl", "jdbc:mysql://host/" + database));
            }
            tags.add(new Tag("databaseName", "ignored, not a url"));
            return CompletableFuture.completedFuture(tags);
        }

        @Override
        public CompletableFuture<List<TagInformation>> getTagInfoContainedSpecificTag(
                String applicationName, String agentId, String metricName,
                String fieldActiveConnection, List<Tag> tagList, Range range) {
            String url = tagList.get(0).getValue();
            String database = url.substring(url.lastIndexOf('/') + 1);
            return CompletableFuture.completedFuture(List.of(new TagInformation(
                    applicationName, agentId, metricName, fieldActiveConnection,
                    List.of(new Tag("jdbcUrl", url), new Tag("databaseName", database)))));
        }
    }
}
