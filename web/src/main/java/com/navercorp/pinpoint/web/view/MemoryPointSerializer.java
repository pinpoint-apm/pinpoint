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
import com.navercorp.pinpoint.web.vo.stat.chart.application.MemoryPoint;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

/**
 * @author HyunGil Jeong
 */
public class MemoryPointSerializer extends JsonSerializer<MemoryPoint> {

    @Deprecated
    @Value("#{pinpointWebProps['web.stat.chart.version'] ?: 'v1'}")
    private String version;

    @Override
    public void serialize(MemoryPoint memoryPoint, JsonGenerator jgen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        if ("v1".equalsIgnoreCase(version)) {
            jgen.writeStartObject();
            jgen.writeNumberField("xVal", memoryPoint.getxVal());
            jgen.writeNumberField("yValForMin", memoryPoint.getyValForMin());
            jgen.writeStringField("agentIdForMin", memoryPoint.getAgentIdForMin());
            jgen.writeNumberField("yValForMax", memoryPoint.getyValForMax());
            jgen.writeStringField("agentIdForMax", memoryPoint.getAgentIdForMax());
            jgen.writeNumberField("yValForAvg", memoryPoint.getyValForAvg());
            jgen.writeEndObject();
        } else {
            jgen.writeStartArray();
            jgen.writeNumber(memoryPoint.getyValForMin());
            jgen.writeString(memoryPoint.getAgentIdForMin());
            jgen.writeNumber(memoryPoint.getyValForMax());
            jgen.writeString(memoryPoint.getAgentIdForMax());
            jgen.writeNumber(memoryPoint.getyValForAvg());
            jgen.writeEndArray();
        }
    }
}
