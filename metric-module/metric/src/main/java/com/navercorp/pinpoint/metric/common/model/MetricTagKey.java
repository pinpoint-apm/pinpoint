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
public class MetricTagKey {

    private final String hostGroupName;
    private final String hostName;
    private final String metricName;
    private final String fieldName;

    public MetricTagKey(String hostGroupName, String hostName, String metricName, String fieldName) {
        this.hostGroupName = StringPrecondition.requireHasLength(hostGroupName, "hostGroupName");
        this.hostName = StringPrecondition.requireHasLength(hostName, "hostName");
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
    }

    public String getHostGroupName() {
        return hostGroupName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricTagKey that = (MetricTagKey) o;
        return Objects.equals(hostGroupName, that.hostGroupName) &&
                Objects.equals(hostName, that.hostName) &&
                Objects.equals(metricName, that.metricName) &&
                Objects.equals(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostGroupName, hostName, metricName, fieldName);
    }

    @Override
    public String toString() {
        return "MetricTagKey{" +
                "hostGroupName='" + hostGroupName + '\'' +
                ", hostName='" + hostName + '\'' +
                ", metricName='" + metricName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                '}';
    }
}
