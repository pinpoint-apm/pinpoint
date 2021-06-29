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
public class MetricTagName {

    private final String applicationId;
    private final String metricName;
    private final String fieldName;

    public MetricTagName(String applicationId, String metricName, String fieldName) {
        this.applicationId = StringPrecondition.requireHasLength(applicationId, "applicationId");
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricTagName that = (MetricTagName) o;
        return Objects.equals(applicationId, that.applicationId) &&
                Objects.equals(metricName, that.metricName) &&
                Objects.equals(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationId, metricName, fieldName);
    }
}
