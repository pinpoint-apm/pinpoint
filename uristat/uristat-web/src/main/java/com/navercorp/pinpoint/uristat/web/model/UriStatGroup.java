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

package com.navercorp.pinpoint.uristat.web.model;

import com.navercorp.pinpoint.common.server.util.ObjectUtils;
import com.navercorp.pinpoint.metric.web.view.BasicTimeseriesChartType;
import com.navercorp.pinpoint.metric.web.view.TimeSeriesValueView;
import com.navercorp.pinpoint.metric.web.view.TimeseriesChartType;
import com.navercorp.pinpoint.metric.web.view.TimeseriesValueGroupView;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

public class UriStatGroup implements TimeseriesValueGroupView {
    private final String uri;
    private final List<TimeSeriesValueView> values;
    private final TimeseriesChartType chartType;
    private final String unit;

    public static final UriStatGroup EMPTY_URI_STAT_GROUP = new UriStatGroup();

    private UriStatGroup() {
        this.uri = ObjectUtils.EMPTY_STRING;
        this.values = List.of();
        this.chartType = BasicTimeseriesChartType.BAR;
        this.unit = ObjectUtils.EMPTY_STRING;
    }

    public UriStatGroup(String uri,
                        List<UriStatChartValue> uriStats,
                        List<TimeSeriesValueView> uriStatValues) {
        Assert.notEmpty(uriStats, "uriStats must not be empty");
        this.uri = Objects.requireNonNullElse(uri, ObjectUtils.EMPTY_STRING);
        this.values = uriStatValues;
        UriStatChartValue uriStatChartValue = uriStats.get(0);
        this.chartType = uriStatChartValue.getChartType();
        this.unit = uriStatChartValue.getUnit();
    }

    @Override
    public String getGroupName() {
        return uri;
    }

    @Override
    public List<TimeSeriesValueView> getMetricValues() {
        return values;
    }

    @Override
    public TimeseriesChartType getChartType() {
        return chartType;
    }

    @Override
    public String getUnit() {
        return unit;
    }
}
