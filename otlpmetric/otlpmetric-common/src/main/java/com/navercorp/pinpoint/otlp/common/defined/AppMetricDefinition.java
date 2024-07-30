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

package com.navercorp.pinpoint.otlp.common.defined;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;

/**
 * @author minwoo-jung
 */
public class AppMetricDefinition {

    public static final int SCHEMA_VERSION = 1;

    private final String applicationName;
    private String id;
    private final String title;
    private final String metricGroupName;
    private final String metricName;
    private final String fieldName;
    private final String tags;
    private final String unit;
    private final String chartType;


    public AppMetricDefinition(String applicationName, String id, String title, String metricGroupName, String metricName, String fieldName, String tags, String unit, String chartType) {
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.id = id;
        this.title = StringPrecondition.requireHasLength(title, "title");
        this.metricGroupName = StringPrecondition.requireHasLength(metricGroupName, "metricGroupName");
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.fieldName = "";
        this.tags = StringPrecondition.requireHasLength(tags, "tags");
        this.unit = StringPrecondition.requireHasLength(unit, "unit");
        this.chartType = StringPrecondition.requireHasLength(chartType, "chartType");
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
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

    public String getTags() {
        return tags;
    }

    public String getUnit() {
        return unit;
    }

    public String getChartType() {
        return chartType;
    }

    public int getSchemaVersion() {
        return SCHEMA_VERSION;
    }

}
