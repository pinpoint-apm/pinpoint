/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.view;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.model.MetricValue;
import com.navercorp.pinpoint.metric.web.model.MetricValueGroup;
import com.navercorp.pinpoint.metric.web.model.SystemMetricData;

import java.util.List;
import java.util.Objects;

public class SystemMetricView extends DefaultTimeSeriesView {

    public static SystemMetricView transform(SystemMetricData<? extends Number> systemMetricData) {
        String title = systemMetricData.getTitle();
        List<Long> timeStampList = systemMetricData.getTimeStampList();
        String unit = systemMetricData.getUnit();

        List<TimeseriesValueGroupView> groupViews = Lists.transform(systemMetricData.getMetricValueGroupList(),
                value -> new MetricValueGroupView(value, unit));
        return new SystemMetricView(title, timeStampList, groupViews);
    }

    public SystemMetricView(String title, List<Long> timestamp, List<TimeseriesValueGroupView> metricValueGroups) {
        super(title, timestamp, metricValueGroups);
    }

    public static class MetricValueGroupView implements TimeseriesValueGroupView {
        private final MetricValueGroup<? extends Number> value;
        private final String unit;

        public MetricValueGroupView(MetricValueGroup<? extends Number> value, String unit) {
            this.value = Objects.requireNonNull(value, "value");
            this.unit = Objects.requireNonNull(unit, "unit");
        }

        @Override
        public String getGroupName() {
            return value.getGroupName();
        }

        @Override
        public List<TimeSeriesValueView> getMetricValues() {
            return Lists.transform(value.getMetricValueList(), MetricValueView::new);
        }

        @Override
        public TimeseriesChartType getChartType() {
            return BasicTimeseriesChartType.LINE;
        }

        @Override
        public String getUnit() {
            return unit;
        }
    }

    public static class MetricValueView implements TimeSeriesValueView {
        private final MetricValue<? extends Number> value;

        public MetricValueView(MetricValue<? extends Number> value) {
            this.value = Objects.requireNonNull(value, "value");
        }

        public String getFieldName() {
            return value.getFieldName();
        }

        public List<String> getTags() {
            return Lists.transform(value.getTagList(), Tag::toString);
        }

        public List<? extends Number> getValues() {
            return value.getValueList();
        }
    }
}
