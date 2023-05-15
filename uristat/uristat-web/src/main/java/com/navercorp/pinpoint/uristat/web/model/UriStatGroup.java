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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.common.server.util.ObjectUtils;
import com.navercorp.pinpoint.metric.common.model.StringPrecondition;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.view.TimeSeriesValueView;
import com.navercorp.pinpoint.metric.web.view.TimeseriesValueGroupView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UriStatGroup implements TimeseriesValueGroupView {
    private final String uri;

    private final List<TimeSeriesValueView> values;

    public static final UriStatGroup EMPTY_URI_STAT_GROUP = new UriStatGroup();

    public UriStatGroup() {
        this.uri = ObjectUtils.EMPTY_STRING;
        this.values = Collections.emptyList();
    }

    public UriStatGroup(String uri, int dataSize, TimeWindow timeWindow, List<UriStatChartValue> uriStats, List<String> fieldNames) {
        this.uri = uri;
        this.values = UriStatValue.createChartValueList(dataSize, timeWindow, uriStats, fieldNames);
    }

    @Override
    public String getGroupName() {
        return uri;
    }

    @Override
    public List<TimeSeriesValueView> getMetricValues() {
        return values;
    }

    public static class UriStatValue implements TimeSeriesValueView {
        private final String fieldName;
        private final List<Double> values;

        private static Double nullToNegativeOne(Double d) {
            if (d == null) {
                return Double.valueOf(-1);
            } else {
                return d;
            }
        }

        public static List<TimeSeriesValueView> createChartValueList(int dataSize, TimeWindow timeWindow, List<UriStatChartValue> uriStats, List<String> fieldNames) {
            Objects.requireNonNull(uriStats);

            List<TimeSeriesValueView> values = new ArrayList<>();

            final int bucketSize = uriStats.get(0).getValues().size();

            for (int histogramIndex = 0 ; histogramIndex < bucketSize; histogramIndex++) {
                Double[] filledData = new Double[dataSize];

                for (UriStatChartValue uriStat : uriStats) {
                    int filledIndex = timeWindow.getWindowIndex(uriStat.getTimestamp());
                    if ((filledData[filledIndex] != null) && (filledData[filledIndex] != 0)) {
                        throw new RuntimeException("Uri stat timestamp mismatch.");
                    }
                    filledData[filledIndex] = uriStat.getValues().get(histogramIndex);
                }
                List<Double> adjusted = Arrays.stream(filledData).map(UriStatValue::nullToNegativeOne).collect(Collectors.toList());
                values.add(new UriStatValue(fieldNames.get(histogramIndex), adjusted));
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
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public List<String> getTags() {
            return null;
        }

        @Override
        public List<Double> getValues() {
            return values;
        }
    }
}
