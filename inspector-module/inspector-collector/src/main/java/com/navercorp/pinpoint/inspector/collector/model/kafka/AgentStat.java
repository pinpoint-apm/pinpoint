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
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.Collections;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class AgentStat {

    private final String tenantId;
    private final String applicationName;
    private final String agentId;

    private final String metricName;
    private final String fieldName;
    private final double fieldValue;
    private final List<Tag> tags;

    private final long eventTime;

    public AgentStat(String tenantId, String applicationName, String agentId, String metricName, String fieldName, double fieldValue, long eventTime) {
        this(tenantId, applicationName, agentId, metricName, fieldName, fieldValue, eventTime, Collections.emptyList());
    }

    public AgentStat(String tenantId, String applicationName, String agentId, String metricName, String fieldName, double fieldValue, long eventTime, List<Tag> tags) {
        this.tenantId = tenantId;
        this.applicationName = applicationName;
        this.agentId = agentId;
        this.metricName = metricName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.eventTime = eventTime;
        this.tags = tags;
    }

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
        return eventTime;
    }

    @JsonSerialize(contentUsing = ToStringSerializer.class)
    public List<Tag> getTags() {
        return tags;
    }


}
