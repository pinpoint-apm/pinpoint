package com.navercorp.pinpoint.alarm.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.AlarmMetricDefinition;

import java.util.List;
import java.util.Set;

public final class AlarmCatalogResponse {

    private AlarmCatalogResponse() {
    }

    public record DataSource(String value, String label, List<String> filterKeys, List<Metric> metrics) {

        public static DataSource from(AlarmDataSource dataSource) {
            return new DataSource(
                    dataSource.name(),
                    dataSource.label(),
                    dataSource.filterKeys(),
                    dataSource.metrics().stream()
                            .map(Metric::from)
                            .toList());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Metric(String value, String label, String trigger, Set<String> allowedAggregations) {

        public static Metric from(AlarmMetricDefinition metric) {
            return new Metric(
                    metric.value(),
                    metric.label(),
                    metric.trigger(),
                    metric.allowedAggregations());
        }
    }
}
