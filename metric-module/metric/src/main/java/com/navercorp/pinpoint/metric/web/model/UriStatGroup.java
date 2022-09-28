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

package com.navercorp.pinpoint.metric.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.metric.common.model.StringPrecondition;
import com.navercorp.pinpoint.metric.common.model.UriStat;
import com.navercorp.pinpoint.metric.web.view.TimeSeriesValueView;
import com.navercorp.pinpoint.metric.web.view.TimeseriesValueGroupView;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UriStatGroup implements TimeseriesValueGroupView {
    private final String uri;

    private final List<TimeSeriesValueView> values;

    public static final UriStatGroup EMPTY_URI_STAT_GROUP = new UriStatGroup();

    public UriStatGroup() {
        this.uri = StringUtils.EMPTY;
        this.values = Collections.emptyList();
    }

    public UriStatGroup(String uri, List<UriStat> uriStats) {
        this.uri = uri;
        this.values = UriStatValue.createValueList(uriStats);
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
        private static final String FIELD_PREFIX = "histogram";
        private final String fieldName;
        private final List<Integer> values;

        public static List<TimeSeriesValueView> createValueList(List<UriStat> uriStats) {
            Objects.requireNonNull(uriStats);
            List<TimeSeriesValueView> values = new ArrayList<>();

            final int bucketSize = uriStats.get(0).getTotalHistogram().length;
            for (int i = 0 ; i < bucketSize; i++) {
                final int histogramIndex = i;
                values.add(new UriStatValue(FIELD_PREFIX + histogramIndex,
                        uriStats.stream().map(e -> e.getTotalHistogram()[histogramIndex]).collect(Collectors.toList())));
            }
            return values;
        }

        public UriStatValue(String fieldName, List<Integer> values) {
            this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
            this.values = Objects.requireNonNull(values);
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
        public List<Integer> getValues() {
            return values;
        }
    }
}
