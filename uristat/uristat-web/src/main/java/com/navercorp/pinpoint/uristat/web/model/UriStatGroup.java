/*
 * Copyright 2022 NAVER Corp.
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

import com.google.common.primitives.Doubles;
import com.navercorp.pinpoint.common.server.util.ObjectUtils;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.common.server.util.array.DoubleArray;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.metric.web.view.TimeSeriesValueView;
import com.navercorp.pinpoint.metric.web.view.TimeseriesChartType;
import com.navercorp.pinpoint.metric.web.view.TimeseriesValueGroupView;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
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
        this.chartType = TimeseriesChartType.bar;
        this.unit = ObjectUtils.EMPTY_STRING;
    }

    public UriStatGroup(String uri, TimeWindow timeWindow,
                        List<UriStatChartValue> uriStats,
                        List<String> fieldNames) {
        Assert.notEmpty(uriStats, "uriStats must not be empty");
        this.uri = Objects.requireNonNullElse(uri, ObjectUtils.EMPTY_STRING);
        this.values = UriStatValue.createChartValueList(timeWindow, uriStats, fieldNames);
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

    public static class UriStatValue implements TimeSeriesValueView {
        private static final double NULL = -1D;

        private final String fieldName;
        private final List<Double> values;

        public static List<TimeSeriesValueView> createChartValueList(TimeWindow timeWindow, List<UriStatChartValue> uriStats, List<String> fieldNames) {

            List<TimeSeriesValueView> values = new ArrayList<>();

            final int bucketSize = uriStats.get(0).getValues().size();

            for (int i = 0 ; i < bucketSize; i++) {
                double[] filledData = DoubleArray.newArray(timeWindow.getWindowRangeCount(), NULL);

                for (UriStatChartValue uriStat : uriStats) {
                    int index = timeWindow.getWindowIndex(uriStat.getTimestamp());
                    filledData[index] = uriStat.getValues().get(i);
                }
                values.add(new UriStatValue(fieldNames.get(i), Doubles.asList(filledData)));
            }
            return values;
        }


        public UriStatValue(String fieldName, List<Double> uriStats) {
            this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
            this.values = Objects.requireNonNull(uriStats, "uriStats");
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public List<String> getTags() {
            return Collections.emptyList();
        }

        @Override
        public List<Double> getValues() {
            return values;
        }
    }
}
