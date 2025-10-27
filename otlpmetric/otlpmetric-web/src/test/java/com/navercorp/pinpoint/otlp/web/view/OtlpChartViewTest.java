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

package com.navercorp.pinpoint.otlp.web.view;

import com.navercorp.pinpoint.otlp.common.model.AggreTemporality;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import com.navercorp.pinpoint.otlp.common.model.MetricType;
import com.navercorp.pinpoint.otlp.common.web.definition.property.AggregationFunction;
import com.navercorp.pinpoint.otlp.web.view.legacy.OtlpChartViewBuilder;
import com.navercorp.pinpoint.otlp.web.vo.FieldAttribute;
import com.navercorp.pinpoint.otlp.web.vo.OtlpMetricChartResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OtlpChartViewTest {

    private OtlpChartViewBuilder otlpChartViewBuilder;

    @BeforeEach
    public void setup() {
        otlpChartViewBuilder = OtlpChartViewBuilder.newBuilder(MetricType.GAUGE);
    }

    @Test
    public void shiftFillEmptyValueShouldAddTimestampAndValue() {
        FieldAttribute fieldAttribute = new FieldAttribute("test", MetricType.GAUGE, DataType.DOUBLE, AggregationFunction.AVG, AggreTemporality.DELTA, "description", "unit", "version");
        List<OtlpMetricChartResult> dataPoints = List.of(new OtlpMetricChartResult(123456789L, "", 100));

        otlpChartViewBuilder.add(fieldAttribute, dataPoints);
        otlpChartViewBuilder.shiftFillEmptyValue(0, 123456789L);
        assertEquals(2, otlpChartViewBuilder.getTimestamp().size());
        assertEquals(123456789L, otlpChartViewBuilder.getTimestamp().get(1));
    }

    @Test
    public void setTimestampShouldSetTimestamp() {
        List<Long> timestamps = Arrays.asList(123456789L, 987654321L);
        otlpChartViewBuilder.setTimestamp(timestamps);
        assertEquals(timestamps, otlpChartViewBuilder.getTimestamp());
    }

    @Test
    public void addShouldAddFieldData() {
        FieldAttribute fieldAttribute = new FieldAttribute("test", MetricType.GAUGE, DataType.DOUBLE, AggregationFunction.AVG, AggreTemporality.DELTA, "description", "unit", "version");
        List<OtlpMetricChartResult> dataPoints1 = List.of(new OtlpMetricChartResult(123456789L, "", 100));
        otlpChartViewBuilder.add(fieldAttribute, dataPoints1);
        assertEquals(1, otlpChartViewBuilder.getFields().size());

        List<OtlpMetricChartResult> dataPoints2 = List.of(new OtlpMetricChartResult(123456789L, "", 100));
        otlpChartViewBuilder.add(fieldAttribute, dataPoints2);
        assertEquals(2, otlpChartViewBuilder.getFields().size());
    }

}