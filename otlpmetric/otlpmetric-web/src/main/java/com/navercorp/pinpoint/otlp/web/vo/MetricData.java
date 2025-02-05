/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.web.vo;

import com.navercorp.pinpoint.otlp.common.web.definition.property.ChartType;
import com.navercorp.pinpoint.otlp.common.web.model.MetricValue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo-jung
 */
public class MetricData {
    private final static String EMPTY_MESSAEG = "";
    private List<Long> timestampList;
    private final ChartType chartType;
    private final String unit;
    private final List<MetricValue> metricValueList;
    private final String message;

    public MetricData(List<Long> timestampList, ChartType chartType, String unit, String message) {
        this.timestampList = timestampList;
        this.chartType = chartType;
        this.unit = unit;
        this.metricValueList = new ArrayList<>();
        this.message = message;
    }

    public MetricData(List<Long> timestampList, ChartType chartType, String unit) {
        this(timestampList, chartType, unit, EMPTY_MESSAEG);
    }

    public void addMetricValue(MetricValue metricValue) {
        this.metricValueList.add(metricValue);
    }

    public List<Long> getTimestampList() {
        return timestampList;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public String getUnit() {
        return unit;
    }

    public List<MetricValue> getMetricValueList() {
        return metricValueList;
    }

    public String getMessage() {
        return message;
    }
}
