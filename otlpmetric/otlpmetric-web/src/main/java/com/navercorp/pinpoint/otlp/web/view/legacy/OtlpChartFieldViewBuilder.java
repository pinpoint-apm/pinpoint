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

import com.navercorp.pinpoint.otlp.web.vo.FieldAttribute;

import java.util.List;

public class OtlpChartFieldViewBuilder {
    private static Number defaultValue = -1;

    private String chartType;
    private String fieldName;
    private String description;
    private String unit;
    private String version;
    private String aggregationTemporality;
    private List<Number> values;


    public OtlpChartFieldView build() {
        return new OtlpChartFieldView(chartType, fieldName, description, unit, version, aggregationTemporality, values);
    }

    public OtlpChartFieldViewBuilder self() {
        return this;
    }

    public OtlpChartFieldViewBuilder setChartType(String chartType) {
        this.chartType = chartType;
        return self();
    }

    public OtlpChartFieldViewBuilder setDescription(String description) {
        this.description = description;
        return self();
    }

    public OtlpChartFieldViewBuilder setUnit(String unit) {
        this.unit = unit;
        return self();
    }

    public OtlpChartFieldViewBuilder setVersion(String version) {
        this.version = version;
        return self();
    }

    public OtlpChartFieldViewBuilder setAggregationTemporality(String aggregationTemporality) {
        this.aggregationTemporality = aggregationTemporality;
        return self();
    }

    public OtlpChartFieldViewBuilder setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return self();
    }

    public OtlpChartFieldViewBuilder setValues(List<Number> values) {
        this.values = values;
        return self();
    }

    public OtlpChartFieldViewBuilder add(List<Number> values) {
        setValues(values);
        return self();
    }

    public static OtlpChartFieldView makeFilledFieldData(String chartType, FieldAttribute field, OtlpChartViewBuilder parentView, List<Long> currentTimestamp, List<Number> value) {
        List<Long> parentTimestamp = parentView.getTimestamp();

        for (int i = 0; i < parentTimestamp.size(); i++) {
            Long parentValue = parentTimestamp.get(i);
            if (currentTimestamp.size() <= i) {
                currentTimestamp.add(i, parentValue);
                value.add(i, defaultValue);
                continue;
            }

            Long currentValue = currentTimestamp.get(i);
            if (parentValue < currentValue) {
                currentTimestamp.add(i, parentValue);
                value.add(i, defaultValue);
            } else if (parentValue > currentValue) {
                parentView.shiftFillEmptyValue(i, currentValue);
            }
        }

        for (int i = parentTimestamp.size(); i < currentTimestamp.size(); i++) {
            parentView.shiftFillEmptyValue(i, currentTimestamp.get(i));
        }

        return new OtlpChartFieldView(chartType, field.fieldName(), field.description(), field.unit(), field.version(), field.aggregationTemporality().name(), value);
    }
}
