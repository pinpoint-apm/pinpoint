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

import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class MetricTagKey {

    private String hostGroupId;
    private String hostName;
    private String metricName;
    private String fieldName;

    public MetricTagKey(String hostGroupId, String hostName, String metricName, String fieldName) {
        if (StringUtils.isEmpty(hostGroupId)) {
            throw new IllegalArgumentException("hostGroupId must not be empty");
        }
        if (StringUtils.isEmpty(hostName)) {
            throw new IllegalArgumentException("hostName must not be empty");
        }
        if (StringUtils.isEmpty(metricName)) {
            throw new IllegalArgumentException("metricName must not be empty");
        }
        if (StringUtils.isEmpty(fieldName)) {
            throw new IllegalArgumentException("fieldName must not be empty");
        }
        this.hostGroupId = hostGroupId;
        this.hostName = hostName;
        this.metricName = metricName;
        this.fieldName = fieldName;
    }

    public String getHostGroupId() {
        return hostGroupId;
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
        return Objects.equals(hostGroupId, that.hostGroupId) &&
                Objects.equals(hostName, that.hostName) &&
                Objects.equals(metricName, that.metricName) &&
                Objects.equals(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostGroupId, hostName, metricName, fieldName);
    }

    @Override
    public String toString() {
        return "MetricTagKey{" +
                "hostGroupId='" + hostGroupId + '\'' +
                ", hostName='" + hostName + '\'' +
                ", metricName='" + metricName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                '}';
    }
}
