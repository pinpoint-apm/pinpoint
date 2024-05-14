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

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
// TODO : (minwoo) Need to integrate with com.navercorp.pinpoint.metric.web.dao.model.SystemMetricDataSearchKey of metric module
public class InspectorQueryParameter {

    private final static String DEFAULT_TABLE_NAME = "tableName";
    private final String tenantId;
    private final String tableName;
    private final String applicationName;
    private final String sortKey;
    private final String agentId;
    private final String metricName;
    private final String fieldName;
    private final List<Tag> tagList;
    private final Range range;
    private final TimePrecision timePrecision;
    private final long limit;


    public InspectorQueryParameter(InspectorDataSearchKey inspectorDataSearchKey, String sortKey, String metricName, String fieldName) {
        this(inspectorDataSearchKey, DEFAULT_TABLE_NAME, sortKey, metricName, fieldName, Collections.emptyList());
    }

    public InspectorQueryParameter(InspectorDataSearchKey inspectorDataSearchKey, String tableName, String sortKey, String metricName, String fieldName) {
        this(inspectorDataSearchKey, tableName, sortKey, metricName, fieldName, Collections.emptyList());
    }

    public InspectorQueryParameter(InspectorDataSearchKey inspectorDataSearchKey, String sortKey, String metricName, String fieldName, List<Tag> tagList) {
        this(inspectorDataSearchKey, DEFAULT_TABLE_NAME, sortKey, metricName, fieldName, tagList);
    }

    public InspectorQueryParameter(InspectorDataSearchKey inspectorDataSearchKey, String tableName, String sortKey, String metricName, String fieldName, List<Tag> tagList) {
        Objects.requireNonNull(inspectorDataSearchKey, "inspectorDataSearchKey");

        this.tenantId = inspectorDataSearchKey.getTenantId();
        this.tableName = StringPrecondition.requireHasLength(tableName, "tableName");
        this.sortKey = StringPrecondition.requireHasLength(sortKey, "sortKey");
        this.applicationName = inspectorDataSearchKey.getApplicationName();
        this.agentId = inspectorDataSearchKey.getAgentId();
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
        this.tagList = Objects.requireNonNull(tagList, "tagList");
        this.range = inspectorDataSearchKey.getRange();
        this.timePrecision = inspectorDataSearchKey.getTimePrecision();
        this.limit = inspectorDataSearchKey.getLimit();
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getTableName() {
        return tableName;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InspectorQueryParameter that = (InspectorQueryParameter) o;
        return limit == that.limit && Objects.equals(tenantId, that.tenantId) && Objects.equals(tableName, that.tableName) && Objects.equals(applicationName, that.applicationName) && Objects.equals(sortKey, that.sortKey) && Objects.equals(agentId, that.agentId) && Objects.equals(metricName, that.metricName) && Objects.equals(fieldName, that.fieldName) && Objects.equals(tagList, that.tagList) && Objects.equals(range, that.range) && Objects.equals(timePrecision, that.timePrecision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, tableName, applicationName, sortKey, agentId, metricName, fieldName, tagList, range, timePrecision, limit);
    }

    @Override
    public String toString() {
        return "InspectorQueryParameterV2{" +
                "tenantId='" + tenantId + '\'' +
                ", tableName='" + tableName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", sortKey='" + sortKey + '\'' +
                ", agentId='" + agentId + '\'' +
                ", metricName='" + metricName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", tagList=" + tagList +
                ", range=" + range +
                ", rangefrom=" + range.getFrom() +
                ", rangeTo=" + range.getTo() +
                ", timePrecision=" + timePrecision +
                ", limit=" + limit +
                '}';
    }
}
