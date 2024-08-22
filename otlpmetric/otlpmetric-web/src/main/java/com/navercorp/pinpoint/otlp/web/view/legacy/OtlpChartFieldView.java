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

package com.navercorp.pinpoint.otlp.web.view.legacy;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtlpChartFieldView {
    private static String SUMMARY_KEY = "type";
    private static String SUMMARY_VALUE = "value";
    private String fieldName;
    private String chartType;
    private String description;
    private String unit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> summary;
    private String version;
    private String aggregationTemporality;
    private List<Number> values;

    public OtlpChartFieldView(String chartType, String fieldName, String description, String unit, String version, String aggregationTemporality, List<Number> values) {
        this.chartType = chartType;
        this.fieldName = fieldName;
        this.description = description;
        this.version = version;
        this.values = values;
        this.unit = unit;
        this.aggregationTemporality = aggregationTemporality;
    }

    public List<Number> getValues() {
        return values;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public String getAggregationTemporality() {
        return aggregationTemporality;
    }

    public String getUnit() {
        return unit;
    }

    public Map<String, String> getSummary() {
        return summary;
    }

    public String getChartType() {
        return chartType;
    }

    public void setSummaryField(String key, String value) {
        summary = new HashMap<>();
        summary.put(SUMMARY_KEY, key);
        summary.put(SUMMARY_VALUE, value);
    }
}
