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
import com.navercorp.pinpoint.web.vo.stat.chart.application.ResponseTimePoint;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

/**
 * @author HyunGil Jeong
 */
public class ResponseTimePointSerializer extends JsonSerializer<ResponseTimePoint> {

    @Deprecated
    @Value("#{pinpointWebProps['web.stat.chart.version'] ?: 'v1'}")
    private String version;

    @Override
    public void serialize(ResponseTimePoint responseTimePoint, JsonGenerator jgen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        if ("v1".equalsIgnoreCase(version)) {
            jgen.writeStartObject();
            jgen.writeNumberField("xVal", responseTimePoint.getxVal());
            jgen.writeNumberField("yValForMin", responseTimePoint.getyValForMin());
            jgen.writeStringField("agentIdForMin", responseTimePoint.getAgentIdForMin());
            jgen.writeNumberField("yValForMax", responseTimePoint.getyValForMax());
            jgen.writeStringField("agentIdForMax", responseTimePoint.getAgentIdForMax());
            jgen.writeNumberField("yValForAvg", responseTimePoint.getyValForAvg());
            jgen.writeEndObject();
        } else {
            jgen.writeStartArray();
            jgen.writeNumber(responseTimePoint.getyValForMin());
            jgen.writeString(responseTimePoint.getAgentIdForMin());
            jgen.writeNumber(responseTimePoint.getyValForMax());
            jgen.writeString(responseTimePoint.getAgentIdForMax());
            jgen.writeNumber(responseTimePoint.getyValForAvg());
            jgen.writeEndArray();
        }
    }
}
