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
 * <p>Between them the two cover the shapes the validators branch on: a metric with
 * allowed aggregations, one driven by a trigger instead, and one that takes no
 * aggregation at all.
 */
public enum TestAlarmDataSource implements AlarmDataSource {

    PRIMARY("Primary",
            List.of("environment", "release", "client"),
            List.of(
                    new AlarmMetricDefinition("event_count", "Event Count", null, Set.of("COUNT")),
                    new AlarmMetricDefinition("new_group_count", "New Group", "NEW_GROUP")
            )
    ),
    SECONDARY("Secondary",
            List.of("environment", "channel"),
            List.of(
                    new AlarmMetricDefinition("total_count", "Total Count", null, Set.of("SUM")),
                    new AlarmMetricDefinition("success_rate", "Success Rate (%)")
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
            return List.of(PRIMARY, SECONDARY);
        }
    }
}
