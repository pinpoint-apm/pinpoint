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

package com.navercorp.pinpoint.web.view.histogram;

import com.navercorp.pinpoint.metric.web.view.TimeSeriesValueView;
import com.navercorp.pinpoint.metric.web.view.TimeseriesChartType;
import com.navercorp.pinpoint.metric.web.view.TimeseriesValueGroupView;

import java.util.List;
import java.util.Objects;

public class TimeHistogramValueGroupView implements TimeseriesValueGroupView {
    private final String groupName;
    private final String unit;
    private final TimeseriesChartType chartType;
    private final List<TimeSeriesValueView> metricValues;

    public TimeHistogramValueGroupView(String groupName, String unit, TimeseriesChartType chartType, List<TimeSeriesValueView> metricValues) {
        this.groupName = Objects.requireNonNull(groupName, "groupName");
        this.chartType = Objects.requireNonNull(chartType, "chartType");

        this.metricValues = Objects.requireNonNull(metricValues, "metricValues");
        this.unit = Objects.requireNonNull(unit, "unit");
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public TimeseriesChartType getChartType() {
        return chartType;
    }

    @Override
    public List<TimeSeriesValueView> getMetricValues() {
        return metricValues;
    }

    @Override
    public String getUnit() {
        return unit;
    }
}
