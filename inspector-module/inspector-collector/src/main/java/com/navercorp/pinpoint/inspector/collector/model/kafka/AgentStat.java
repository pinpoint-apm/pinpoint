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

package com.navercorp.pinpoint.inspector.collector.model.kafka;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class AgentStat {

    static final List<Tag> EMPTY_TAGS = List.of();

    private final String tenantId;
    private final long timestamp;
    private final String applicationName;
    private final String agentId;
    private final String sortKey;
    private final String metricName;
    private final String fieldName;
    private final double fieldValue;
    private final List<Tag> tags;


    public AgentStat(String tenantId, long timestamp, String applicationName, String agentId,
                     String sortKey,
                     String metricName, String fieldName, double fieldValue, List<Tag> tags) {
        this.tenantId = tenantId;
        this.timestamp = timestamp;
        this.sortKey = sortKey;
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        this.metricName = metricName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.tags = tags;
    }

    @Deprecated
    public String getTenantId() {
        return tenantId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getMetricName() {
        return metricName;
    }

    public double getFieldValue() {
        return fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public long getEventTime() {
        return timestamp;
    }

    @JsonSerialize(contentUsing = ToStringSerializer.class)
    public List<Tag> getTags() {
        return tags;
    }

    public String getSortKey() {
        return sortKey;
    }

    @Override
    public String toString() {
        return "AgentStat{" +
                "tenantId='" + tenantId + '\'' +
                ", timestamp=" + timestamp +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", sortKey='" + sortKey + '\'' +
                ", metricName='" + metricName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", fieldValue=" + fieldValue +
                ", tags=" + tags +
                '}';
    }
}
