/*
 * Copyright 2017 NAVER Corp.
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public class StatChartGroupSerializer extends JsonSerializer<StatChartGroup> {

    @Override
    public void serialize(StatChartGroup statChartGroup, JsonGenerator jgen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        Map<StatChartGroup.ChartType, Chart<? extends Point>> charts = statChartGroup.getCharts();
        writeSchema(jgen, charts.keySet());

        TimeWindow timeWindow = statChartGroup.getTimeWindow();
        writeTimestamp(jgen, timeWindow);

        writeCharts(jgen, charts);
        jgen.writeEndObject();
    }

    private void writeSchema(JsonGenerator jgen, Set<StatChartGroup.ChartType> chartTypes) throws IOException {
        jgen.writeFieldName("schema");
        jgen.writeStartObject();
        jgen.writeStringField("x", "time");
        for (StatChartGroup.ChartType chartType : chartTypes) {
            jgen.writeObjectField(chartType.toString(), chartType.getSchema());
        }

        jgen.writeEndObject();
    }

    private void writeTimestamp(JsonGenerator jgen, TimeWindow timeWindow) throws IOException {
        List<Long> timestamps = new ArrayList<>((int) timeWindow.getWindowRangeCount());
        for (Long timestamp : timeWindow) {
            timestamps.add(timestamp);
        }
        jgen.writeObjectField("x", timestamps);
    }

    private void writeCharts(JsonGenerator jgen, Map<StatChartGroup.ChartType, Chart<? extends Point>> charts) throws IOException {
        jgen.writeFieldName("y");
        jgen.writeStartObject();
        for (Map.Entry<StatChartGroup.ChartType, Chart<? extends Point>> e : charts.entrySet()) {
            StatChartGroup.ChartType chartType = e.getKey();
            Chart<? extends Point> chart = e.getValue();
            List<? extends Point> points = chart.getPoints();
            jgen.writeObjectField(chartType.toString(), points);
        }
        jgen.writeEndObject();
    }
}
