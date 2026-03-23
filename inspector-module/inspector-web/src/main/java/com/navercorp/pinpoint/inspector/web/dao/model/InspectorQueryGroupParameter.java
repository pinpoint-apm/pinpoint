/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web.dao.model;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimePrecision;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Query parameter for batched multi-agent inspector queries.
 * Uses sortKey IN (...) instead of sortKey = ? so that all agentIds can be
 * fetched in a single round-trip to Pinot.
 */
public class InspectorQueryGroupParameter {

    private final String tenantId;
    private final String tableName;
    private final List<String> sortKeys;
    private final String metricName;
    private final String fieldName;
    private final List<Tag> tagList;
    private final Range range;
    private final TimePrecision timePrecision;
    private final long limit;

    public InspectorQueryGroupParameter(String tenantId, String tableName, List<String> sortKeys,
                                        String metricName, String fieldName, List<Tag> tagList,
                                        Range range, TimePrecision timePrecision, long perAgentLimit) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.sortKeys = Objects.requireNonNull(sortKeys, "sortKeys");
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName");
        this.tagList = tagList != null ? tagList : Collections.emptyList();
        this.range = Objects.requireNonNull(range, "range");
        this.timePrecision = Objects.requireNonNull(timePrecision, "timePrecision");
        this.limit = perAgentLimit * sortKeys.size();
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getSortKeys() {
        return sortKeys;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public List<Tag> getTagList() {
        return tagList;
    }

    public Range getRange() {
        return range;
    }

    public TimePrecision getTimePrecision() {
        return timePrecision;
    }

    public long getLimit() {
        return limit;
    }
}
