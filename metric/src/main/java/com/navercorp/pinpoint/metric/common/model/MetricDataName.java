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
public class MetricDataName {

    private final String fieldName;
    private final String metricName;

    public MetricDataName(String metricName, String fieldName) {
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
    }


    public String getFieldName() {
        return fieldName;
    }

    public String getMetricName() {
        return metricName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricDataName that = (MetricDataName) o;
        return Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(metricName, that.metricName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, metricName);
    }
}
