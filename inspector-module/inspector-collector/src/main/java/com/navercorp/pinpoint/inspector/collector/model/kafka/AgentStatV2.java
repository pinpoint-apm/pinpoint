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
public class AgentStatV2 {

    private final String tenantId;
    private final Long sortKey;
    private final String applicationName;
    private final String agentId;
    private final String metricName;
    private final String fieldName;
    private final double fieldValue;
    private final List<Tag> tags;
    private final long eventTime;

    public AgentStatV2(AgentStat agentStat, Long sortKey) {
        this.tenantId = agentStat.getTenantId();
        this.sortKey = sortKey;
        this.applicationName = agentStat.getApplicationName();
        this.agentId = agentStat.getAgentId();
        this.metricName = agentStat.getMetricName();
        this.fieldName = agentStat.getFieldName();
        this.fieldValue = agentStat.getFieldValue();
        this.eventTime = agentStat.getEventTime();
        this.tags = agentStat.getTags();
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

    public Long getSortKey() {
        return sortKey;
    }


}
