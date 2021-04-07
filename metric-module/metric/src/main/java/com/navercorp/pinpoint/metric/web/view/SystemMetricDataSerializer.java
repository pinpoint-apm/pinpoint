/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.model.MetricValue;
import com.navercorp.pinpoint.metric.web.model.SystemMetricData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Deprecated
public class SystemMetricDataSerializer extends JsonSerializer<SystemMetricData> {
    @Override
    public void serialize(SystemMetricData systemMetricData, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject();

        writeTitle(jgen, systemMetricData.getTitle());
        writeTimestampList(jgen, systemMetricData.getTimeStampList());
//        writeMetricValueList(jgen, systemMetricData.getMetricValueList());


        jgen.writeEndObject();
    }

    private void writeMetricValueList(JsonGenerator jgen, List<MetricValue> metricValueList) throws IOException {
        jgen.writeFieldName("metricValues");
        jgen.writeStartArray();

        for (MetricValue metricValue : metricValueList) {
            writeMetricValue(jgen, metricValue);
        }

        jgen.writeEndArray();

    }

    private void writeMetricValue(JsonGenerator jgen, MetricValue metricValue) throws IOException {
        jgen.writeStartObject();

        List<Tag> tagList = metricValue.getTagList();
        List<String> tags = new ArrayList<>(tagList.size());
        for (Tag tag : tagList) {
            tags.add(tag.toString());
        }
        jgen.writeObjectField("fieldName", metricValue.getFieldName());
        jgen.writeObjectField("tags", tags);
        jgen.writeObjectField("values", metricValue.getValueList());

        jgen.writeEndObject();
    }

    private void writeTimestampList(JsonGenerator jgen, List<Long> timeStampList) throws IOException {
        jgen.writeObjectField("timestamp", timeStampList);
    }

    private void writeTitle(JsonGenerator jgen, String title) throws IOException {
        jgen.writeObjectField("title", title);
    }
}
