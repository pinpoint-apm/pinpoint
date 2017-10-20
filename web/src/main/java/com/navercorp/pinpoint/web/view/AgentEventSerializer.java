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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.web.util.ThreadDumpUtils;
import com.navercorp.pinpoint.web.vo.AgentEvent;

import java.io.IOException;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentEventSerializer extends JsonSerializer<AgentEvent> {

    @Override
    public void serialize(AgentEvent agentEvent, JsonGenerator jgen, SerializerProvider serializers) throws IOException {
        jgen.writeStartObject();

        if (agentEvent.getAgentId() != null) {
            jgen.writeStringField("agentId", agentEvent.getAgentId());
        }

        jgen.writeNumberField("startTimestamp", agentEvent.getStartTimestamp());
        jgen.writeNumberField("eventTimestamp", agentEvent.getEventTimestamp());
        jgen.writeNumberField("eventTypeCode", agentEvent.getEventTypeCode());

        if (agentEvent.getEventTypeDesc() != null) {
            jgen.writeStringField("eventTypeDesc", agentEvent.getEventTypeDesc());
        }

        jgen.writeBooleanField("hasEventMessage", agentEvent.hasEventMessage());

        if (agentEvent.getEventMessage() != null) {
            Object eventMessage = agentEvent.getEventMessage();

            if (eventMessage instanceof TDeadlock) {
                StringBuilder message = new StringBuilder();

                List<TThreadDump> deadlockedThreadList = ((TDeadlock) eventMessage).getDeadlockedThreadList();

                for (TThreadDump threadDump : deadlockedThreadList) {
                    message.append(ThreadDumpUtils.createDumpMessage(threadDump));
                }

                jgen.writeObjectField("eventMessage", message.toString());
            } else {
                jgen.writeObjectField("eventMessage", eventMessage);
            }
        }

        jgen.writeEndObject();
    }

}
