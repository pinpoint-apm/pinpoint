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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.metric.common.model.pinot.FromMetricDataTypeToIntSerializer;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class MetricData {

    private String metricName;
    private String fieldName;
    private MetricDataType metricDataType;
    private long saveTime;

    public MetricData() {
    }

    public MetricData(String metricName, String fieldName, MetricDataType metricDataType, long saveTime) {
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
        this.metricDataType = Objects.requireNonNull(metricDataType, "metricDataType");
        this.saveTime = saveTime;
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

    public long getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(long saveTime) {
        this.saveTime = saveTime;
    }

    @JsonProperty("dataType")
    @JsonSerialize(using = FromMetricDataTypeToIntSerializer.class)
    public MetricDataType getMetricDataType() {
        return metricDataType;
    }

    public void setMetricDataType(MetricDataType metricDataType) {
        this.metricDataType = metricDataType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetricData data = (MetricData) o;
        return saveTime == data.saveTime && Objects.equals(metricName, data.metricName) && Objects.equals(fieldName, data.fieldName) && metricDataType == data.metricDataType;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(metricName);
        result = 31 * result + Objects.hashCode(fieldName);
        result = 31 * result + Objects.hashCode(metricDataType);
        result = 31 * result + Long.hashCode(saveTime);
        return result;
    }

    @Override
    public String toString() {
        return "MetricData{" +
                "metricName='" + metricName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", metricDataType=" + metricDataType +
                ", saveTime=" + saveTime +
                '}';
    }
}
