/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.collector.model.kafka;

import com.navercorp.pinpoint.common.model.SortKeyUtils;

/**
 * @author minwoo-jung
 */
public class ApplicationStat {

    private static String NULL_STRING = "null";

    private final String tenantId;

    private final String sortKey;
    private final String applicationName;
    private final String metricName;
    private final String fieldName;
    private final double fieldValue;
    private final String primaryTag;
    private final long eventTime;
    public ApplicationStat(String tenantId, String applicationName, String metricName, String fieldName, double fieldValue, long eventTime) {
        this(tenantId, applicationName, metricName, fieldName, NULL_STRING, fieldValue, eventTime);
    }

    public ApplicationStat(String tenantId, String applicationName, String metricName, String fieldName, String primaryTag, double fieldValue, long eventTime) {
        this.tenantId = tenantId;
        this.applicationName = applicationName;
        this.metricName = metricName;
        this.sortKey = SortKeyUtils.generateKeyForApplicationStat(applicationName, metricName);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.eventTime = eventTime;
        this.primaryTag = primaryTag;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getSortKey() {
        return sortKey;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public double getFieldValue() {
        return fieldValue;
    }

    public String getPrimaryTag() {
        return primaryTag;
    }

    public long getEventTime() {
        return eventTime;
    }
}
