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
package com.navercorp.pinpoint.alarm.batch.config;

import com.navercorp.pinpoint.alarm.service.AlarmDataSourceProvider;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.AlarmMetricDefinition;

import java.util.List;
import java.util.Set;

/**
 * A data source for the integration test, which needs one but does not care which.
 *
 * <p>This module ships none of its own -- they are contributed per module -- so borrowing a
 * distribution's would tie the job's behaviour to that distribution's metric names.
 */
public enum IntegrationTestAlarmDataSource implements AlarmDataSource {

    PRIMARY("Primary",
            List.of("environment"),
            List.of(new AlarmMetricDefinition("event_count", "Event Count", null, Set.of("COUNT"))));

    private final String label;
    private final List<String> filterKeys;
    private final List<AlarmMetricDefinition> metrics;

    IntegrationTestAlarmDataSource(String label, List<String> filterKeys, List<AlarmMetricDefinition> metrics) {
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
            return List.of(PRIMARY);
        }
    }
}
