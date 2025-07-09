/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.alarm.dao.model;

import com.navercorp.pinpoint.common.model.SortKeyUtils;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class BatchQueryParameter {

    private final static String DEFAULT_AGENT_ID = "agentId";
    private final static String DEFAULT_FIELD_NAME = "fieldName";
    private final String tableName;
    private final String applicationName;
    private final String agentId;
    private final String metricName;
    private final String sortKey;
    private final List<String> fieldList;
    private final List<Tag> tagList;
    private final String fieldName;
    private final Range range;

    public BatchQueryParameter(String tableName,
                               String applicationName,
                               String agentId,
                               String metricName,
                               String fieldName,
                               List<Tag> tagList,
                               Range range) {
        this(tableName, applicationName, agentId, metricName, fieldName, Collections.emptyList(), tagList, range);
    }

    public BatchQueryParameter(String tableName,
                               String applicationName,
                               String agentId,
                               String metricName,
                               List<String> fieldList,
                               List<Tag> tagList,
                               Range range) {
        this(tableName, applicationName, agentId, metricName, DEFAULT_FIELD_NAME, fieldList, tagList, range);
    }

    public BatchQueryParameter(String tableName,
                               String applicationName,
                               String agentId,
                               String metricName,
                               String fieldName,
                               Range range) {
        this(tableName, applicationName, agentId, metricName, fieldName, Collections.emptyList(), Collections.emptyList(), range);
    }

    public BatchQueryParameter(String tableName,
                               String applicationName,
                               String metricName,
                               List<String> fieldList,
                               List<Tag> tagList,
                               Range range) {
        this(tableName, applicationName, DEFAULT_AGENT_ID, metricName, DEFAULT_FIELD_NAME, fieldList, tagList, range);
    }

    public BatchQueryParameter(String tableName,
                               String applicationName,
                               String metricName,
                               String fieldName,
                               Range range) {
        this(tableName, applicationName, DEFAULT_AGENT_ID, metricName, fieldName, Collections.emptyList(), Collections.emptyList(), range);
    }

    public BatchQueryParameter(String tableName,
                               String applicationName,
                               String metricName,
                               List<String> fieldList,
                               Range range) {
        this(tableName, applicationName, DEFAULT_AGENT_ID, metricName, DEFAULT_FIELD_NAME, fieldList, Collections.emptyList(), range);
    }

    private BatchQueryParameter(String tableName,
                                String applicationName,
                                String agentId,
                                String metricName,
                                String fieldName,
                                List<String> fieldList,
                                List<Tag> tagList,
                                Range range) {
        this.tableName = StringPrecondition.requireHasLength(tableName, "tableName");
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.sortKey = SortKeyUtils.generateKeyForAgentStat(applicationName, agentId, metricName);
        this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
        this.fieldList = Objects.requireNonNull(fieldList, "fieldList");
        this.tagList = Objects.requireNonNull(tagList, "tagList");
        this.range = Objects.requireNonNull(range, "range");
    }

    public String getTableName() {
        return tableName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public List<String> getFieldList() {
        return fieldList;
    }

    public Range getRange() {
        return range;
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

    public String getAgentId() {
        return agentId;
    }

    public String getSortKey() {
        return sortKey;
    }

    public BatchQueryParameter(String tableName, String applicationName, String agentId, String metricName, String sortKey, List<String> fieldList, List<Tag> tagList, String fieldName, Range range) {
        this.tableName = tableName;
        this.applicationName = applicationName;
        this.agentId = agentId;
        this.metricName = metricName;
        this.sortKey = sortKey;
        this.fieldList = fieldList;
        this.tagList = tagList;
        this.fieldName = fieldName;
        this.range = range;
    }

    @Override
    public String toString() {
        return "BatchQueryParameter{" +
                "tableName='" + tableName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", metricName='" + metricName + '\'' +
                ", sortKey='" + sortKey + '\'' +
                ", fieldList=" + fieldList +
                ", tagList=" + tagList +
                ", fieldName='" + fieldName + '\'' +
                ", range=" + range.prettyToString() +
                '}';
    }
}
