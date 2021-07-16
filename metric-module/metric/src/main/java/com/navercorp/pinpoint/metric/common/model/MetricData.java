/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.common.model;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class MetricData {

    private String metricName;
    private String fieldName;
    private MetricDataType metricDataType;

    public MetricData() {
    }

    public MetricData(String metricName, String fieldName, MetricDataType metricDataType) {
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
        this.metricDataType = Objects.requireNonNull(metricDataType, "metricDataType");
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public MetricDataType getMetricDataType() {
        return metricDataType;
    }

    public void setMetricDataType(MetricDataType metricDataType) {
        this.metricDataType = metricDataType;
    }

    @Override
    public String toString() {
        return "MetricData{" +
                "metricName='" + metricName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", metricDataType=" + metricDataType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricData that = (MetricData) o;
        return Objects.equals(metricName, that.metricName) &&
                Objects.equals(fieldName, that.fieldName) &&
                metricDataType == that.metricDataType;
    }
}
