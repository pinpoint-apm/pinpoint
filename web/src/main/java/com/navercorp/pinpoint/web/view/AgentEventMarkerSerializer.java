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
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentEventMarker;

import java.io.IOException;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class AgentEventMarkerSerializer extends JsonSerializer<AgentEventMarker> {

    @Override
    public void serialize(AgentEventMarker agentEventMarker, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("totalCount", agentEventMarker.getTotalCount());
        jsonGenerator.writeFieldName("typeCounts");
        jsonGenerator.writeStartArray(agentEventMarker.getTypeCounts().size());
        for (Map.Entry<AgentEventType, Integer> e : agentEventMarker.getTypeCounts().entrySet()) {
            writeAgentEventTypeCount(jsonGenerator, e);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }

    private void writeAgentEventTypeCount(JsonGenerator jsonGenerator, Map.Entry<AgentEventType, Integer> entry) throws IOException {
        AgentEventType agentEventType = entry.getKey();
        int count = entry.getValue();
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("code", agentEventType.getCode());
        jsonGenerator.writeStringField("desc", agentEventType.getDesc());
        jsonGenerator.writeNumberField("count", count);
        jsonGenerator.writeEndObject();
    }
}
