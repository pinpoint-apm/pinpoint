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

package com.navercorp.pinpoint.metric.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.model.chart.Chart;
import com.navercorp.pinpoint.metric.web.model.chart.Point;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricChart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hyunjoon Cho
 */
public class SystemMetricChartSerializer extends JsonSerializer<SystemMetricChart> {

    @Override
    public void serialize(SystemMetricChart systemMetricChart, JsonGenerator jgen, SerializerProvider serializers) throws IOException {
        SystemMetricChart.SystemMetricChartGroup systemMetricChartGroup = systemMetricChart.getSystemMetricChartGroup();
        jgen.writeStartObject();
        jgen.writeObjectField("title", systemMetricChartGroup.getChartName());

        TimeWindow timeWindow = systemMetricChartGroup.getTimeWindow();
        writeTimestamp(jgen, timeWindow);

        List<List<Tag>> tags = systemMetricChartGroup.getTagsList();
        List<Chart<? extends Point>> charts = systemMetricChartGroup.getCharts();
        writeCharts(jgen, tags, charts);
        jgen.writeEndObject();
    }

    private void writeTimestamp(JsonGenerator jgen, TimeWindow timeWindow) throws IOException {
        List<Long> timestamps = new ArrayList<>((int) timeWindow.getWindowRangeCount());
        for (Long timestamp : timeWindow) {
            timestamps.add(timestamp);
        }
        jgen.writeObjectField("x", timestamps);
    }

    private void writeCharts(JsonGenerator jgen, List<List<Tag>> tagsList, List<Chart<? extends Point>> charts) throws IOException {
        jgen.writeFieldName("y");
        jgen.writeStartObject();
        for (int i = 0; i < tagsList.size(); i++) {
            Chart<? extends Point> chart = charts.get(i);
            List<? extends Point> points = chart.getPoints();
            List<Tag> tags = tagsList.get(i);
            jgen.writeObjectField(tags.toString(), points);
        }
        jgen.writeEndObject();
    }
}
