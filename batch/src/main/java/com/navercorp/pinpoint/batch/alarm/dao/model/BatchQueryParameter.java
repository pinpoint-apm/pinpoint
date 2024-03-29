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
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class BatchQueryParameter {
    private final String EMPTY_STRING = "";
    private final String applicationName;
    private final String agentId;
    private final String metricName;
    private final String sortKey;
    private final List<String> fieldList;

    private final List<Tag> tagList;
    private final String fieldName;
    private final Range range;


    public BatchQueryParameter(String applicationName, String metricName, List<String> fieldList, Range range) {
        StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.applicationName = applicationName;
        this.agentId = EMPTY_STRING;
        StringPrecondition.requireHasLength(metricName, "metricName");
        this.metricName = metricName;
        this.sortKey = SortKeyUtils.generateKey(applicationName, agentId, metricName);
        this.fieldList = Objects.requireNonNull(fieldList, "fieldList");
        this.tagList = Collections.EMPTY_LIST;
        this.fieldName = EMPTY_STRING;
        this.range = Objects.requireNonNull(range, "range");
    }

    public BatchQueryParameter(String applicationName, String metricName, String fieldName, Range range) {
        StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.applicationName = applicationName;
        this.agentId = EMPTY_STRING;
        StringPrecondition.requireHasLength(metricName, "metricName");
        this.metricName = metricName;
        this.sortKey = SortKeyUtils.generateKey(applicationName, agentId, metricName);
        this.fieldList = Collections.EMPTY_LIST;
        this.tagList = Collections.EMPTY_LIST;
        this.fieldName = fieldName;
        this.range = Objects.requireNonNull(range, "range");
    }

    public BatchQueryParameter(String applicationName, String metricName, List<String> fieldList, List<Tag> tagList, Range range) {
        StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.applicationName = applicationName;
        this.agentId = EMPTY_STRING;
        StringPrecondition.requireHasLength(metricName, "metricName");
        this.metricName = metricName;
        this.sortKey = SortKeyUtils.generateKey(applicationName, agentId, metricName);
        this.fieldList = Objects.requireNonNull(fieldList, "fieldList");
        this.tagList = Objects.requireNonNull(tagList, "tagList");
        this.fieldName = EMPTY_STRING;
        this.range = Objects.requireNonNull(range, "range");
    }

    public BatchQueryParameter(String applicationName, String agentId, String metricName, String fieldName, Range range) {
        StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.applicationName = applicationName;
        StringPrecondition.requireHasLength(agentId, "agentId");
        this.agentId = agentId;
        StringPrecondition.requireHasLength(metricName, "metricName");
        this.metricName = metricName;
        this.sortKey = SortKeyUtils.generateKey(applicationName, agentId, metricName);
        this.fieldList = Collections.EMPTY_LIST;
        this.tagList = Collections.EMPTY_LIST;
        this.fieldName = fieldName;
        this.range = Objects.requireNonNull(range, "range");
    }

    public BatchQueryParameter(String applicationName, String agentId, String metricName, List<String> fieldList, List<Tag> tagList, Range range) {
        StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.applicationName = applicationName;
        StringPrecondition.requireHasLength(agentId, "agentId");
        this.agentId = agentId;
        StringPrecondition.requireHasLength(metricName, "metricName");
        this.metricName = metricName;
        this.sortKey = SortKeyUtils.generateKey(applicationName, agentId, metricName);
        this.fieldName = EMPTY_STRING;
        this.fieldList = Objects.requireNonNull(fieldList, "fieldList");;
        this.tagList = Objects.requireNonNull(tagList, "tagList");
        this.range = Objects.requireNonNull(range, "range");
    }

    public BatchQueryParameter(String applicationName, String agentId, String metricName, String fieldName, List<Tag> tagList, Range range) {
        StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.applicationName = applicationName;
        StringPrecondition.requireHasLength(agentId, "agentId");
        this.agentId = agentId;
        StringPrecondition.requireHasLength(metricName, "metricName");
        this.metricName = metricName;
        this.sortKey = SortKeyUtils.generateKey(applicationName, agentId, metricName);
        this.fieldName = fieldName;
        this.fieldList = Collections.EMPTY_LIST;
        this.tagList = Objects.requireNonNull(tagList, "tagList");
        this.range = Objects.requireNonNull(range, "range");
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

    @Override
    public String toString() {
        return "BatchQueryParameter{" +
                "EMPTY_STRING='" + EMPTY_STRING + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", metricName='" + metricName + '\'' +
                ", fieldList=" + fieldList +
                ", tagList=" + tagList +
                ", fieldName='" + fieldName + '\'' +
                ", range=" + range.prettyToString() +
                '}';
    }
}
