/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.model.chart;

import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.model.SampledSystemMetric;
import com.navercorp.pinpoint.metric.web.util.Range;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSlotCentricSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hyunjoon Cho
 */
public class SystemMetricChartTest {

    @Test
    public void createSystemMetricChartTest() {
        long time = System.currentTimeMillis();
        Range range = Range.newRange(time, time + 30000);
        TimeWindowSampler sampler = new TimeWindowSlotCentricSampler();
        TimeWindow timeWindow = new TimeWindow(range, sampler);

        List<Tag> tagList1 = List.of(new Tag("cpu", "cpu0"));
        List<Tag> tagList2 = List.of(new Tag("cpu", "cpu1"));


        SystemMetricPoint<Double> systemMetricPoint1 = new SystemMetricPoint<>(time, 1.11);
        SampledSystemMetric<Double> sampledSystemMetric1 = new SampledSystemMetric<>(systemMetricPoint1, tagList1);
        SampledSystemMetric<Double> sampledSystemMetric2 = new SampledSystemMetric<>(systemMetricPoint1, tagList2);

        SystemMetricPoint<Double> systemMetricPoint2 = new SystemMetricPoint<>(time + 10000, 2.22);
        SampledSystemMetric<Double> sampledSystemMetric3 = new SampledSystemMetric<>(systemMetricPoint2, tagList1);
        SampledSystemMetric<Double> sampledSystemMetric4 = new SampledSystemMetric<>(systemMetricPoint2, tagList2);

        SystemMetricPoint<Double> systemMetricPoint3 = new SystemMetricPoint<>(time + 20000, 3.33);
        SampledSystemMetric<Double> sampledSystemMetric5 = new SampledSystemMetric<>(systemMetricPoint3, tagList1);
        SampledSystemMetric<Double> sampledSystemMetric6 = new SampledSystemMetric<>(systemMetricPoint3, tagList2);

        SystemMetricPoint<Double> systemMetricPoint4 = new SystemMetricPoint<>(time + 30000, 4.44);
        SampledSystemMetric<Double> sampledSystemMetric7 = new SampledSystemMetric<>(systemMetricPoint4, tagList1);
        SampledSystemMetric<Double> sampledSystemMetric8 = new SampledSystemMetric<>(systemMetricPoint4, tagList2);

        List<SampledSystemMetric<Double>> sampledSystemMetricList = List.of(
                sampledSystemMetric1, sampledSystemMetric2, sampledSystemMetric3, sampledSystemMetric4,
                sampledSystemMetric5, sampledSystemMetric6, sampledSystemMetric7, sampledSystemMetric8
        );

        SystemMetricChart systemMetricChart = new SystemMetricChart(timeWindow, "cpu_usage_user", sampledSystemMetricList);
        SystemMetricChart.SystemMetricChartGroup systemMetricChartGroup = systemMetricChart.getSystemMetricChartGroup();

        List<List<Tag>> tagsList = systemMetricChartGroup.getTagsList();

        assertThat(tagsList)
                .containsExactly(tagList1, tagList2);

        List<Chart> chartList = systemMetricChartGroup.getCharts();
        assertThat(chartList)
                .hasSize(2);

        List<Point> pointsFromChart1 = chartList.get(0).getPoints();
        int index = 0;
        for (Point point : pointsFromChart1) {
            Assertions.assertTrue(point.equals(sampledSystemMetricList.get(index * 2).getPoint()));
            index++;
        }

        List<Point> pointsFromChart2 = chartList.get(1).getPoints();
        index = 0;
        for (Point point : pointsFromChart2) {
            Assertions.assertTrue(point.equals(sampledSystemMetricList.get(index * 2 + 1).getPoint()));
            index++;
        }
    }
}
