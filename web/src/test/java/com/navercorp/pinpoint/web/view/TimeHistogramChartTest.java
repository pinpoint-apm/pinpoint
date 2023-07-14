/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.view;

import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.view.histogram.TimeHistogramChartBuilder;
import com.navercorp.pinpoint.web.view.histogram.TimeHistogramType;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesValueGroupView;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesValueView;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author emeroad
 */
public class TimeHistogramChartTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private List<TimeHistogram> timeHistogramList;

    @BeforeEach
    public void setUp() {
        HistogramSchema schema = BaseHistogramSchema.NORMAL_SCHEMA;
        TimeHistogram timeHistogram1 = new TimeHistogram(schema, 0);
        TimeHistogram timeHistogram2 = new TimeHistogram(schema, 60000);

        timeHistogram1.addCallCount(schema.getFastSlot().getSlotTime(), 1);
        timeHistogram1.addCallCount(schema.getNormalSlot().getSlotTime(), 2);
        timeHistogram1.addCallCount(schema.getSlowSlot().getSlotTime(), 3);
        timeHistogram1.addCallCount(schema.getVerySlowSlot().getSlotTime(), 4);
        timeHistogram1.addCallCount(schema.getErrorSlot().getSlotTime(), 5);

        timeHistogram2.addCallCount(schema.getFastSlot().getSlotTime(), 6);
        timeHistogram2.addCallCount(schema.getNormalSlot().getSlotTime(), 7);
        timeHistogram2.addCallCount(schema.getSlowSlot().getSlotTime(), 8);
        timeHistogram2.addCallCount(schema.getVerySlowSlot().getSlotTime(), 9);
        timeHistogram2.addCallCount(schema.getErrorSlot().getSlotTime(), 10);

        timeHistogramList = List.of(timeHistogram1, timeHistogram2);
    }

    @Test
    public void LoadChartBuildTest() {
        TimeHistogramChartBuilder builder = new TimeHistogramChartBuilder(timeHistogramList);
        TimeSeriesView loadChart = builder.build(TimeHistogramType.load);

        Assertions.assertThat(loadChart.getTimestamp()).isEqualTo(List.of(0L, 60000L));
        List<TimeSeriesValueGroupView> metricValueGroups = loadChart.getMetricValueGroups();
        Assertions.assertThat(metricValueGroups).isNotEmpty();

        TimeSeriesValueGroupView groupView = metricValueGroups.get(0);
        List<TimeSeriesValueView> metricValues = groupView.getMetricValues();
        Assertions.assertThat(metricValues.size()).isEqualTo(5);

        HistogramSchema schema = BaseHistogramSchema.NORMAL_SCHEMA;
        Assertions.assertThat(metricValues.get(0).getFieldName()).isEqualTo(schema.getFastSlot().getSlotName());
        Assertions.assertThat(metricValues.get(0).getValues()).isEqualTo(List.of(1L, 6L));
        Assertions.assertThat(metricValues.get(4).getFieldName()).isEqualTo(schema.getErrorSlot().getSlotName());
        Assertions.assertThat(metricValues.get(4).getValues()).isEqualTo(List.of(5L, 10L));
    }
}
