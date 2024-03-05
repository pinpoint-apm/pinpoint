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
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricGroupData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricValue;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author minwoo.jung
 */
public class InspectorMetricGroupDataView {
    private final InspectorMetricGroupData inspectorMetricGroupData;

    public InspectorMetricGroupDataView(InspectorMetricGroupData inspectorMetricGroupData) {
        this.inspectorMetricGroupData = Objects.requireNonNull(inspectorMetricGroupData, "inspectorMetricGroupData");
    }


    public String getTitle() {
        return inspectorMetricGroupData.title();
    }

    public List<Long> getTimestamp() {
        return inspectorMetricGroupData.timestamps();
    }

    public List<MetricValueGroupView> getMetricValueGroups() {
        Map<List<Tag>, List<InspectorMetricValue>> metricValueGroups = inspectorMetricGroupData.metricValueGroups();

        List<MetricValueGroupView> metricValueGroupViewList= new ArrayList<>(metricValueGroups.size());

        for (Map.Entry<List<Tag>, List<InspectorMetricValue>> entry : metricValueGroups.entrySet()) {
            MetricValueGroupView metricValueGroupView = new MetricValueGroupView(entry.getKey(), entry.getValue());
            metricValueGroupViewList.add(metricValueGroupView);
        }

        return metricValueGroupViewList;
    }

    public static class MetricValueGroupView {
        private final List<Tag> tags;
        private final List<MetricValueView> metricValues;

        public MetricValueGroupView(List<Tag> tags, List<InspectorMetricValue> metricValues) {
            this.tags = Objects.requireNonNull(tags, "tags");
            Objects.requireNonNull(metricValues, "metricValues");
            this.metricValues = metricValues.stream().map(MetricValueView::new).collect(Collectors.toList());
        }

        public List<Tag> getTags() {
            return tags;
        }

        public List<MetricValueView> getMetricValues() {
            return metricValues;
        }
    }

    public static class MetricValueView {
        private final String fieldName;
        private final String chartType;
        private final String unit;
        private final List<Double> valueList;

        public MetricValueView(InspectorMetricValue metricValue) {
            Objects.requireNonNull(metricValue, "metricValue");
            this.fieldName = StringPrecondition.requireHasLength(metricValue.getFieldName(), "fieldName");
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


        public String getChartType() {
            return chartType;
        }

        public String getUnit() {
            return unit;
        }
    }
}

