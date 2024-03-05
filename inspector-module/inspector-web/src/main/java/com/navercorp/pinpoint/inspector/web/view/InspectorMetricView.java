/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web.view;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author minwoo.jung
 */
// TODO : (minwoo) Let's integrate it with com.navercorp.pinpoint.metric.collector.view.SystemMetricView
public class InspectorMetricView {
    private final InspectorMetricData inspectorMetricData;

    public InspectorMetricView(InspectorMetricData inspectorMetricData) {
        this.inspectorMetricData = Objects.requireNonNull(inspectorMetricData, "inspectorMetricData");
    }

    public String getTitle() {
        return inspectorMetricData.title();
    }

    public List<Long> getTimestamp() {
        return inspectorMetricData.timestamps();
    }

    public List<MetricValueView> getMetricValues() {
        return inspectorMetricData.metricValues().stream()
                .map(MetricValueView::new)
                .collect(Collectors.toList());
    }

    public static class MetricValueView {
        private final String fieldName;
        private final String chartType;
        private final String unit;
        private final List<Double> valueList;
        private final List<Tag> tagList;

        public MetricValueView(InspectorMetricValue metricValue) {
            Objects.requireNonNull(metricValue, "metricValue");
            this.fieldName = StringPrecondition.requireHasLength(metricValue.getFieldName(), "fieldName");
            this.tagList = Objects.requireNonNull(metricValue.getTagList(), "tagList");
            this.chartType = Objects.requireNonNull(metricValue.getChartType(), "chartType");
            this.unit = Objects.requireNonNull(metricValue.getUnit(), "unit");
            this.valueList = Objects.requireNonNull(metricValue.getValueList(), "valueList");
        }

        public List<Double> getValueList() {
            return valueList;
        }

        public String getFieldName() {
            return fieldName;
        }

        public List<Tag> getTagList() {
            return tagList;
        }

        public String getChartType() {
            return chartType;
        }
        public String getUnit() {
            return unit;
        }
    }
}
