/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */

public class SystemMetric {
    protected final String metricName;
    protected final String hostName;
    protected final String fieldName;
    protected final List<Tag> tags;
    protected final long timestamp;

    public SystemMetric(String metricName, String hostName, String fieldName, List<Tag> tags, long timestamp) {
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.hostName = Objects.requireNonNull(hostName, "hostName");
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName");
        this.tags = tags;
        this.timestamp = timestamp;
    }

    @JsonProperty("metricName")
    public String getMetricName() {
        return metricName;
    }

    @JsonProperty("hostName")
    public String getHostName() {
        return hostName;
    }

    @JsonProperty("fieldName")
    public String getFieldName() {
        return fieldName;
    }

    @JsonSerialize(using = TagSerializer.class)
    public List<Tag> getTags() {
        return tags;
    }

    @JsonProperty("timestampInEpoch")
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SystemMetric{");
        sb.append("metric=").append(metricName);
        sb.append(", host=").append(hostName);
        sb.append(", field=").append(fieldName);
        sb.append(", tags=").append(tags);
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }
}
