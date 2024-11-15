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

package com.navercorp.pinpoint.otlp.web.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

/**
 * @author minwoo-jung
 */
public class MetricDataRequestParameter {
    @NotBlank
    private String applicationName;

    private String agentId;

    @NotBlank
    private String metricGroupName;

    @NotBlank
    private String metricName;

    @NotBlank
    private String primaryForFieldAndTagRelation;

    private List<String> tagGroupList;

    private List<String> fieldNameList;

    @PositiveOrZero
    private long from;

    @PositiveOrZero
    private long to;

    @NotBlank
    private String chartType;

    @NotBlank
    private String aggregationFunction;

    @PositiveOrZero
    private int samplingInterval;

    // Getters and setters
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getMetricGroupName() {
        return metricGroupName;
    }

    public void setMetricGroupName(String metricGroupName) {
        this.metricGroupName = metricGroupName;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getPrimaryForFieldAndTagRelation() {
        return primaryForFieldAndTagRelation;
    }

    public void setPrimaryForFieldAndTagRelation(String primaryForFieldAndTagRelation) {
        this.primaryForFieldAndTagRelation = primaryForFieldAndTagRelation;
    }

    public List<String> getTagGroupList() {
        return tagGroupList;
    }

    public void setTagGroupList(List<String> tagGroupList) {
        this.tagGroupList = tagGroupList;
    }

    public List<String> getFieldNameList() {
        return fieldNameList;
    }

    public void setFieldNameList(List<String> fieldNameList) {
        this.fieldNameList = fieldNameList;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    public String getAggregationFunction() {
        return aggregationFunction;
    }

    public void setAggregationFunction(String aggregationFunction) {
        this.aggregationFunction = aggregationFunction;
    }

    public int getSamplingInterval() {
        return samplingInterval;
    }

    public void setSamplingInterval(@PositiveOrZero int samplingInterval) {
        this.samplingInterval = samplingInterval;
    }

}
