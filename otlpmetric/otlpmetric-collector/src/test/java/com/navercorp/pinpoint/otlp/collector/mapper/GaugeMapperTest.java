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

package com.navercorp.pinpoint.otlp.collector.mapper;

import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricData;
import com.navercorp.pinpoint.otlp.common.model.MetricName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author minwoo-jung
 */
class GaugeMapperTest {

    @Test
    public void setMetricNameTest() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = new OtlpMetricData.Builder();
        String fieldName = gaugeMapper.setMetricName(builder, "metricGroupName.metricName");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("metricName", otlpMetricData.getMetricName());
        assertEquals("metricGroupName", otlpMetricData.getMetricGroupName());
        assertEquals(MetricName.EMPTY_FIELD_NAME, fieldName);
    }

    @Test
    public void setMetricNameTest2() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = new OtlpMetricData.Builder();
        String fieldName = gaugeMapper.setMetricName(builder, "metricGroupName");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("metricGroupName", otlpMetricData.getMetricGroupName());
        assertEquals(MetricName.EMPTY_METRIC_NAME, otlpMetricData.getMetricName());
        assertEquals(MetricName.EMPTY_FIELD_NAME, fieldName);

    }

    @Test
    public void setMetricNameTest3() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = new OtlpMetricData.Builder();
        String fieldName = gaugeMapper.setMetricName(builder, "metricGroupName.metricName.fieldName");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("metricGroupName", otlpMetricData.getMetricGroupName());
        assertEquals("metricName", otlpMetricData.getMetricName());
        assertEquals("fieldName", fieldName);
    }

    @Test
    public void setMetricNameTest4() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = new OtlpMetricData.Builder();
        String fieldName = gaugeMapper.setMetricName(builder, "");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals(MetricName.EMPTY_METRIC_GROUP_NAME, otlpMetricData.getMetricGroupName());
        assertEquals(MetricName.EMPTY_METRIC_NAME, otlpMetricData.getMetricName());
        assertEquals(MetricName.EMPTY_FIELD_NAME, fieldName);
    }

    @Test
    public void setMetricNameTest5() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = new OtlpMetricData.Builder();
        String fieldName = gaugeMapper.setMetricName(builder, "...");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals(MetricName.EMPTY_METRIC_GROUP_NAME, otlpMetricData.getMetricGroupName());
        assertEquals(MetricName.EMPTY_METRIC_NAME, otlpMetricData.getMetricName());
        assertEquals(MetricName.EMPTY_FIELD_NAME, fieldName);
    }

    @Test
    public void setMetricNameTest6() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = new OtlpMetricData.Builder();
        String fieldName = gaugeMapper.setMetricName(builder, "...metric");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals(".", otlpMetricData.getMetricGroupName());
        assertEquals(MetricName.EMPTY_METRIC_NAME, otlpMetricData.getMetricName());
        assertEquals("metric", fieldName);
    }

    @Test
    public void setMetricNameTest7() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = new OtlpMetricData.Builder();
        String fieldName = gaugeMapper.setMetricName(builder, "process.info.cpu.jvm");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("process.info", otlpMetricData.getMetricGroupName());
        assertEquals("cpu", otlpMetricData.getMetricName());
        assertEquals("jvm", fieldName);
    }
}