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

package com.navercorp.pinpoint.inspector.web.dao.model;

import com.navercorp.pinpoint.common.model.SortKeyUtils;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.metric.common.model.Range;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
// TODO : (minwoo) Need to integrate with com.navercorp.pinpoint.metric.web.dao.model.SystemMetricDataSearchKey of metric module
public class InspectorQueryParameterV2 {

    private final String tenantId;
    private final String applicationName;
    private final String agentId;
    private final String metricName;
    private final String fieldName;
    private final List<Tag> tagList;

    private final Range range;
    private final TimePrecision timePrecision;
    private final long limit;

    private final String sortKey;

    public InspectorQueryParameterV2(InspectorDataSearchKey inspectorDataSearchKey, String metricName, String fieldName) {
        this(inspectorDataSearchKey, metricName, fieldName, Collections.emptyList());
    }

    public InspectorQueryParameterV2(InspectorDataSearchKey inspectorDataSearchKey, String metricName, String fieldName, List<Tag> tagList) {
        Objects.requireNonNull(inspectorDataSearchKey, "inspectorDataSearchKey");

        this.tenantId = inspectorDataSearchKey.getTenantId();
        this.applicationName = inspectorDataSearchKey.getApplicationName();
        this.agentId = inspectorDataSearchKey.getAgentId();
        this.sortKey = SortKeyUtils.generateKey(applicationName, agentId, metricName);
        this.metricName = metricName;
        this.fieldName = fieldName;
        this.tagList = tagList;
        this.range = inspectorDataSearchKey.getRange();
        this.timePrecision = inspectorDataSearchKey.getTimePrecision();
        this.limit = inspectorDataSearchKey.getLimit();
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAgentId() {
        return agentId;
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

    public String getApplicationName() {
        return applicationName;
    }

    public String getSortKey() {
        return sortKey;
    }

    @Override
    public String toString() {
        return "InspectorQueryParameterV2{" +
                "tenantId='" + tenantId + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", metricName='" + metricName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", tagList=" + tagList +
                ", range=" + range +
                ", timePrecision=" + timePrecision +
                ", limit=" + limit +
                ", sortKey='" + sortKey + '\'' +
                '}';
    }
}
