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

package com.navercorp.pinpoint.otlp.common.web.vo.view;

import com.navercorp.pinpoint.otlp.common.web.definition.property.ChartType;
import com.navercorp.pinpoint.otlp.common.web.vo.MetricData;
import com.navercorp.pinpoint.otlp.common.web.vo.MetricValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author minwoo-jung
 */
class MetricDataViewTest {

    @Test
    public void test() {
        List<Long> timestampList = List.of(1747603560000L, 1747603570000L, 1747603580000L);
        MetricData metricData = new MetricData(timestampList, ChartType.LINE, "count", "message");
        List<List<Number>> valueGroupList = List.of(
                List.of(100, 200, 300),
                List.of(333, 444, 555),
                List.of(777, 888, 999)
        );

        metricData.addMetricValue(new MetricValue("legend1", valueGroupList.get(0), "v1"));
        metricData.addMetricValue(new MetricValue("legend2", valueGroupList.get(1), "v1"));
        metricData.addMetricValue(new MetricValue("legend3", valueGroupList.get(2), "v1"));

        MetricDataView metricDataView = new MetricDataView(metricData);

        assertEquals(timestampList, metricDataView.getTimestamp());
        assertEquals("line", metricDataView.getChartType());
        assertEquals("count", metricDataView.getUnit());
        assertEquals("message", metricDataView.getMessage());
        List<MetricDataView.MetricValueView> metricValues = metricDataView.getMetricValues();
        assertEquals(3, metricValues.size());

        for (int i = 0; i < metricValues.size(); i++) {
            MetricDataView.MetricValueView metricValueView = metricValues.get(i);
            assertEquals("legend" + (i + 1), metricValueView.getLegendName());
            assertEquals(valueGroupList.get(i), metricValueView.getValues());
            assertEquals("v1", metricValueView.getVersion());
        }

    }
}