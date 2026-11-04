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

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentEventMarker;

import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class AgentEventMarkerSerializer extends ValueSerializer<AgentEventMarker> {

    @Override
    public void serialize(AgentEventMarker agentEventMarker, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberProperty("totalCount", agentEventMarker.getTotalCount());
        jsonGenerator.writeName("typeCounts");
        jsonGenerator.writeStartArray();
        for (Map.Entry<AgentEventType, Integer> e : agentEventMarker.getTypeCounts().entrySet()) {
            writeAgentEventTypeCount(jsonGenerator, e);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }

    private void writeAgentEventTypeCount(JsonGenerator jsonGenerator, Map.Entry<AgentEventType, Integer> entry) {
        AgentEventType agentEventType = entry.getKey();
        int count = entry.getValue();
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberProperty("code", agentEventType.getCode());
        jsonGenerator.writeStringProperty("desc", agentEventType.getDesc());
        jsonGenerator.writeNumberProperty("count", count);
        jsonGenerator.writeEndObject();
    }
}
