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
package com.navercorp.pinpoint.alarm.vo;

import com.navercorp.pinpoint.alarm.service.AlarmDataSourceProvider;
import java.util.List;
import java.util.Set;

/**
 * Data sources for tests of logic that needs one but does not care which.
 *
 * <p>This module ships no data sources of its own -- they are contributed per module
 * -- so the tests supply their own rather than borrowing a distribution's, which
 * would tie shared behaviour to one distribution's metric names.
 *
 * <p>The shared module's tests declare the same shapes for the same reason. A test
 * fixture is not published between modules here: the evaluation job's integration
 * test declares its own too.
 *
 * <p>Between them the two cover the shapes the validators branch on: a metric with
 * allowed aggregations, one driven by a trigger instead, and one that takes no
 * aggregation at all. They are named after the shapes a traced application has so
 * that a reader recognises them, not to mirror any data source that ships.
 */
public enum TestAlarmDataSource implements AlarmDataSource {

    AGENT_STAT("Agent Stat",
            List.of("agent", "service_type", "host"),
            List.of(
                    new AlarmMetricDefinition("sample_count", "Sample Count",
                            null, Set.of("COUNT")),
                    new AlarmMetricDefinition("deadlock_count", "Deadlock Detected", "NEW_GROUP")
            )
    ),
    APPLICATION_RESPONSE("Application Response",
            List.of("agent", "callee"),
            List.of(
                    new AlarmMetricDefinition("total_count", "Total Count", null, Set.of("SUM")),
                    new AlarmMetricDefinition("apdex_score", "Apdex Score")
            )
    );

    private final String label;
    private final List<String> filterKeys;
    private final List<AlarmMetricDefinition> metrics;

    TestAlarmDataSource(String label, List<String> filterKeys, List<AlarmMetricDefinition> metrics) {
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
        return String.format("%s/detail/%s?from=%d&to=%d", baseUrl, application, fromMs, toMs);
    }

    public static class Provider implements AlarmDataSourceProvider {
        @Override
        public List<AlarmDataSource> dataSources() {
            return List.of(AGENT_STAT, APPLICATION_RESPONSE);
        }
    }
}
