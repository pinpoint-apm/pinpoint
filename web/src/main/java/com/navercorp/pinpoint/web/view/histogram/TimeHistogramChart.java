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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.metric.web.view.TimeSeriesView;
import com.navercorp.pinpoint.metric.web.view.TimeseriesValueGroupView;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Objects;

public class TimeHistogramChart implements TimeSeriesView {
    private final String title;
    @Nullable
    private final List<Long> timestamp;
    private final List<TimeseriesValueGroupView> metricValueGroups;

    public TimeHistogramChart(String title, List<Long> timestamp, List<TimeseriesValueGroupView> metricValueGroups) {
        this.title = Objects.requireNonNull(title, "title");
        this.timestamp = timestamp;
        this.metricValueGroups = Objects.requireNonNull(metricValueGroups, "metricValueGroups");
    }

    @Override
    public String getTitle() {
        return title;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Override
    public List<Long> getTimestamp() {
        return timestamp;
    }

    @Override
    public List<TimeseriesValueGroupView> getMetricValueGroups() {
        return metricValueGroups;
    }
}
