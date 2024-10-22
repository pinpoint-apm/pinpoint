/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.collector.model;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class PinotOtlpMetricDataRow {

    protected final String  serviceId;
    protected final String sortKey;
    @NotBlank
    protected final String applicationId;
    @NotBlank
    protected final String agentId;
    protected final String metricGroupName;
    @NotBlank
    protected final String metricName;
    protected final String fieldName;
    protected final int flag;
    protected final List<String> tags;
    protected final String version;
    protected final Long eventTime;
    protected final Long startTime;

    public PinotOtlpMetricDataRow(String serviceId, String sortKey, String applicationName,
                                  String agentId, String metricGroupName, String metricName, String fieldName,int flag,
                                  List<String> tags, String version,
                                  Long eventTime, Long startTime) {
        this.serviceId = serviceId;
        this.sortKey = sortKey;
        this.applicationId = applicationName;
        this.agentId = agentId;
        this.metricGroupName = metricGroupName;
        this.metricName = metricName;
        this.fieldName = fieldName;
        this.flag = flag;
        this.version = version;
        this.eventTime = eventTime;
        this.startTime = startTime;
        this.tags = tags;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getMetricGroupName() {
        return metricGroupName;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getFieldName() {
        return fieldName;
    }
    public int getFlag() {
        return flag;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getVersion() {
        return version;
    }

    public Long getEventTime() {
        return eventTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getSortKey() {
        return sortKey;
    }

    @Override
    public String toString() {
        return "PinotOtlpMetricDataRow{" +
                "serviceId='" + serviceId + '\'' +
                ", sortKey='" + sortKey + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", agentId='" + agentId + '\'' +
                ", metricGroupName='" + metricGroupName + '\'' +
                ", metricName='" + metricName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", flag=" + flag +
                ", tags=" + tags +
                ", version='" + version + '\'' +
                ", eventTime=" + eventTime +
                ", startTime=" + startTime +
                '}';
    }
}
