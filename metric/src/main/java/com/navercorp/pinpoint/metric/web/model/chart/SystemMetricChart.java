/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.metric.web.model.chart;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.view.SystemMetricChartSerializer;
import com.navercorp.pinpoint.metric.web.model.SampledSystemMetric;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Hyunjoon Cho
 */
@JsonSerialize(using = SystemMetricChartSerializer.class)
public class SystemMetricChart {
    private final SystemMetricChartGroup systemMetricChartGroup;

    public <T extends Number> SystemMetricChart(TimeWindow timeWindow, String chartName, List<SampledSystemMetric<T>> sampledSystemMetrics) {
        this.systemMetricChartGroup = new SystemMetricChartGroup<>(timeWindow, chartName, sampledSystemMetrics);
    }

    public SystemMetricChartGroup getSystemMetricChartGroup() {
        return systemMetricChartGroup;
    }

    public static class SystemMetricChartGroup <T extends Number> {

        private final String chartName;

        private final TimeWindow timeWindow;

        private final List<List<Tag>> tagsList;

        private final List<Chart<? extends Point>> charts;

        private SystemMetricChartGroup(TimeWindow timeWindow, String chartName, List<SampledSystemMetric<T>> sampledSystemMetrics) {
            this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
            this.chartName = Objects.requireNonNull(chartName, "chartName");

            Map<List<Tag>, List<SampledSystemMetric<T>>> taggedSystemMetrics = sampledSystemMetrics.stream().collect(Collectors.groupingBy(SampledSystemMetric::getTags));
            this.tagsList = processTagList(taggedSystemMetrics);
            this.charts = processChartList(taggedSystemMetrics);
        }

        private List<List<Tag>> processTagList(Map<List<Tag>, List<SampledSystemMetric<T>>> taggedSystemMetrics) {
            ImmutableList.Builder<List<Tag>> builder = ImmutableList.builder();
            for (Map.Entry<List<Tag>, List<SampledSystemMetric<T>>> entry : taggedSystemMetrics.entrySet()) {
                builder.add(entry.getKey());
            }
            return builder.build();
        }

        private List<Chart<? extends Point>> processChartList(Map<List<Tag>, List<SampledSystemMetric<T>>> taggedSystemMetrics) {
            ImmutableList.Builder<Chart<? extends Point>> builder = ImmutableList.builder();
            for (Map.Entry<List<Tag>, List<SampledSystemMetric<T>>> entry : taggedSystemMetrics.entrySet()) {
                    builder.add(newChart(entry.getValue(), SampledSystemMetric::getPoint));
            }

            return builder.build();
        }

        private Chart<SystemMetricPoint<T>> newChart(List<SampledSystemMetric<T>> sampledSystemMetrics, Function<SampledSystemMetric<T>, SystemMetricPoint<T>> function) {
            TimeSeriesChartBuilder<SystemMetricPoint<T>> builder = new TimeSeriesChartBuilder(this.timeWindow, SampledSystemMetric.UNCOLLECTED_POINT_CREATOR);
            return builder.build(sampledSystemMetrics, function);
        }

        public String getChartName() {
            return chartName;
        }

        public TimeWindow getTimeWindow() {
            return timeWindow;
        }

        public List<List<Tag>> getTagsList() {
            return tagsList;
        }

        public List<Chart<? extends Point>> getCharts() {
            return charts;
        }
    }
}
