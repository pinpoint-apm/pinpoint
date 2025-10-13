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

package com.navercorp.pinpoint.otlp.collector.mapper;

import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricData;
import com.navercorp.pinpoint.otlp.common.model.MetricName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author minwoo-jung
 */
class OtlpMetricDataMapperTest {

    private OtlpMetricData.Builder newBuilder() {
        OtlpMetricData.Builder builder = OtlpMetricData.newBuilder();
        builder.setAgentId("agentId");
        builder.setServiceName("applicationName-cafe");
        return builder;
    }

    @Test
    public void setMetricNameTest1() {
        OtlpMetricData.Builder builder = newBuilder();

        SummaryMapper SummaryMapper = new SummaryMapper();
        SummaryMapper.setMetricName(builder, "");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals(MetricName.EMPTY_METRIC_GROUP_NAME, otlpMetricData.getMetricGroupName());
        assertEquals(MetricName.EMPTY_METRIC_NAME, otlpMetricData.getMetricName());
    }

    @Test
    public void setMetricNameTest2() {
        OtlpMetricData.Builder builder = newBuilder();

        SummaryMapper SummaryMapper = new SummaryMapper();
        SummaryMapper.setMetricName(builder, "metricGroupName.metricName");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("metricGroupName", otlpMetricData.getMetricGroupName());
        assertEquals("metricName", otlpMetricData.getMetricName());
    }

    @Test
    public void setMetricNameTest3() {
        OtlpMetricData.Builder builder = newBuilder();

        SummaryMapper SummaryMapper = new SummaryMapper();
        SummaryMapper.setMetricName(builder, "metricGroupName...");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("metricGroupName", otlpMetricData.getMetricGroupName());
        assertEquals(MetricName.EMPTY_METRIC_NAME, otlpMetricData.getMetricName());
    }

    @Test
    public void setMetricNameTest4() {
        OtlpMetricData.Builder builder = newBuilder();

        SummaryMapper SummaryMapper = new SummaryMapper();
        SummaryMapper.setMetricName(builder, "...metric");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("..", otlpMetricData.getMetricGroupName());
        assertEquals("metric", otlpMetricData.getMetricName());
    }

    @Test
    public void setMetricNameTest5() {
        OtlpMetricData.Builder builder = newBuilder();

        SummaryMapper SummaryMapper = new SummaryMapper();
        SummaryMapper.setMetricName(builder, "first.second.third.fourth");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("first.second.third", otlpMetricData.getMetricGroupName());
        assertEquals("fourth", otlpMetricData.getMetricName());
    }

    @Test
    public void setMetricNameTest6() {
        OtlpMetricData.Builder builder = newBuilder();

        SummaryMapper SummaryMapper = new SummaryMapper();
        SummaryMapper.setMetricName(builder, "hikaricp.connections.creation");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("hikaricp.connections", otlpMetricData.getMetricGroupName());
        assertEquals("creation", otlpMetricData.getMetricName());
    }

    @Test
    public void setMetricNameAndGetFieldTest1() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = newBuilder();

        String fieldName = gaugeMapper.setMetricNameAndGetField(builder, "metricGroupName.metricName");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("metricGroupName", otlpMetricData.getMetricGroupName());
        assertEquals("metricName", otlpMetricData.getMetricName());
        assertEquals(MetricName.EMPTY_FIELD_NAME, fieldName);
    }

    @Test
    public void setMetricNameAndGetFieldTest2() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = newBuilder();

        String fieldName = gaugeMapper.setMetricNameAndGetField(builder, "metricGroupName");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("metricGroupName", otlpMetricData.getMetricGroupName());
        assertEquals(MetricName.EMPTY_METRIC_NAME, otlpMetricData.getMetricName());
        assertEquals(MetricName.EMPTY_FIELD_NAME, fieldName);

    }

    @Test
    public void setMetricNameAndGetFieldTest3() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = newBuilder();

        String fieldName = gaugeMapper.setMetricNameAndGetField(builder, "metricGroupName.metricName.fieldName");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("metricGroupName", otlpMetricData.getMetricGroupName());
        assertEquals("metricName", otlpMetricData.getMetricName());
        assertEquals("fieldName", fieldName);
    }

    @Test
    public void setMetricNameAndGetFieldTest4() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = newBuilder();

        String fieldName = gaugeMapper.setMetricNameAndGetField(builder, "");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals(MetricName.EMPTY_METRIC_GROUP_NAME, otlpMetricData.getMetricGroupName());
        assertEquals(MetricName.EMPTY_METRIC_NAME, otlpMetricData.getMetricName());
        assertEquals(MetricName.EMPTY_FIELD_NAME, fieldName);
    }

    @Test
    public void setMetricNameAndGetFieldTest5() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = newBuilder();

        String fieldName = gaugeMapper.setMetricNameAndGetField(builder, "...");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals(MetricName.EMPTY_METRIC_GROUP_NAME, otlpMetricData.getMetricGroupName());
        assertEquals(MetricName.EMPTY_METRIC_NAME, otlpMetricData.getMetricName());
        assertEquals(MetricName.EMPTY_FIELD_NAME, fieldName);
    }

    @Test
    public void setMetricNameAndGetFieldTest6() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = newBuilder();

        String fieldName = gaugeMapper.setMetricNameAndGetField(builder, "...metric");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals(".", otlpMetricData.getMetricGroupName());
        assertEquals(MetricName.EMPTY_METRIC_NAME, otlpMetricData.getMetricName());
        assertEquals("metric", fieldName);
    }

    @Test
    public void setMetricNameAndGetFieldTest7() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = newBuilder();

        String fieldName = gaugeMapper.setMetricNameAndGetField(builder, "process.info.cpu.jvm");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("process.info", otlpMetricData.getMetricGroupName());
        assertEquals("cpu", otlpMetricData.getMetricName());
        assertEquals("jvm", fieldName);
    }

    @Test
    public void setMetricNameAndGetFieldTest8() {
        GaugeMapper gaugeMapper = new GaugeMapper();
        OtlpMetricData.Builder builder = newBuilder();

        String fieldName = gaugeMapper.setMetricNameAndGetField(builder, "process.info.resource.cpu.jvm");
        OtlpMetricData otlpMetricData = builder.build();
        assertEquals("process.info.resource", otlpMetricData.getMetricGroupName());
        assertEquals("cpu", otlpMetricData.getMetricName());
        assertEquals("jvm", fieldName);
    }
}