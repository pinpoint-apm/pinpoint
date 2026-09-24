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
package com.navercorp.pinpoint.alarm.core.vo;

import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.AlarmMetricDefinition;

import java.util.List;
import java.util.Set;

/**
 * What a rule can measure about an application this installation already collects.
 *
 * <p>Split by where the numbers are read from rather than by what they are called, because a
 * data source is what the evaluation job routes on: one metric query service reads each of
 * these, and mixing two storages behind one name would mean one query having to know about
 * both.
 *
 * <p>The metric names are the ones the v1 alarm used, lowercased. Keeping them recognisable
 * matters more than tidiness here -- an operator moving a rule across reads these, and a rule
 * stores the name rather than an ordinal, so a rename is a migration.
 */
public enum CoreAlarmDataSource implements AlarmDataSource {

    /**
     * Responses the application served, summed over the rule's window. Read from the map
     * statistics, which is the same source the server map draws its node histograms from, so a
     * rule and the screen an operator checks it against cannot disagree.
     */
    APPLICATION_RESPONSE("Application Response",
            List.of(),
            List.of(
                    new AlarmMetricDefinition("slow_count", "Slow Count", null, Set.of("SUM")),
                    new AlarmMetricDefinition("slow_rate", "Slow Rate (%)"),
                    new AlarmMetricDefinition("error_count", "Error Count", null, Set.of("SUM")),
                    new AlarmMetricDefinition("error_rate", "Error Rate (%)"),
                    new AlarmMetricDefinition("total_count", "Total Count", null, Set.of("SUM")),
                    new AlarmMetricDefinition("apdex_score", "Apdex Score")
            )
    ),

    /**
     * Calls this application made to another, summed over the rule's window. Which callee is
     * a filter rather than part of the metric name, because it is a value an operator types
     * and not one of a fixed set: the v1 alarm carried it in the rule's notes field, which
     * meant a rule could only ever watch one.
     */
    APPLICATION_OUT_CALL("Calls To Another Application",
            List.of("callee"),
            List.of(
                    new AlarmMetricDefinition("slow_count", "Slow Count", null, Set.of("SUM")),
                    new AlarmMetricDefinition("slow_rate", "Slow Rate (%)"),
                    new AlarmMetricDefinition("error_count", "Error Count", null, Set.of("SUM")),
                    new AlarmMetricDefinition("error_rate", "Error Rate (%)"),
                    new AlarmMetricDefinition("total_count", "Total Count", null, Set.of("SUM"))
            )
    ),

    /**
     * What the agents of an application reported about themselves. Every value here is per
     * agent, and the aggregation says how a rule reduces them: MAX with a ceiling threshold
     * reproduces the v1 alarm, which fired when any one agent exceeded. Which agents did is
     * reported as detail beside the value, so the notification names them.
     */
    AGENT_STAT("Agent Stat",
            List.of(),
            List.of(
                    // A per-agent value is reduced by one of these; MAX is what the v1 alarm did.
                    new AlarmMetricDefinition("heap_usage_rate", "Heap Usage Rate (%)",
                            null, Set.of("MAX", "MIN", "AVG")),
                    new AlarmMetricDefinition("jvm_cpu_usage_rate", "JVM CPU Usage Rate (%)",
                            null, Set.of("MAX", "MIN", "AVG")),
                    new AlarmMetricDefinition("system_cpu_usage_rate", "System CPU Usage Rate (%)",
                            null, Set.of("MAX", "MIN", "AVG")),
                    new AlarmMetricDefinition("file_descriptor_count", "Open File Descriptor Count",
                            null, Set.of("MAX", "MIN", "AVG")),
                    // Per connection pool as well as per agent, so the reduction runs over
                    // every pool of every agent and the detail names which one.
                    new AlarmMetricDefinition("datasource_connection_usage_rate",
                            "Connection Pool Usage Rate (%)", null, Set.of("MAX", "MIN", "AVG"))
            )
    ),

    /**
     * What an agent reported about itself as an event rather than as a measurement. Read from
     * the agent event store, which is where the profiler's deadlock monitor writes.
     *
     * <p>The metric carries the NEW_GROUP trigger, so a rule on it takes no operator and no
     * threshold -- the evaluator fires on any value above zero. That is the whole condition an
     * operator wants here: a deadlocked thread does not recover, and there is no count of them
     * worth tolerating. The v1 alarm asked for a threshold and then ignored it, treating any
     * value above zero as "enabled" and anything else as "off"; this says that directly.
     */
    AGENT_EVENT("Agent Event",
            List.of(),
            List.of(
                    new AlarmMetricDefinition("deadlock_count", "Deadlock Detected", "NEW_GROUP")
            )
    );

    private final String label;
    private final List<String> filterKeys;
    private final List<AlarmMetricDefinition> metrics;

    CoreAlarmDataSource(String label, List<String> filterKeys, List<AlarmMetricDefinition> metrics) {
        this.label = label;
        this.filterKeys = List.copyOf(filterKeys);
        this.metrics = List.copyOf(metrics);
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public List<String> filterKeys() {
        return filterKeys;
    }

    @Override
    public List<AlarmMetricDefinition> metrics() {
        return metrics;
    }

    @Override
    public String detailLink(String baseUrl, String application, long fromMs, long toMs) {
        return String.format("%s/main/%s?from=%d&to=%d", baseUrl, application, fromMs, toMs);
    }
}
