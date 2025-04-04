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
import com.navercorp.pinpoint.common.server.bo.event.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadDumpBo;
import com.navercorp.pinpoint.web.util.ThreadDumpUtils;
import com.navercorp.pinpoint.web.vo.AgentEvent;

import java.io.IOException;

/**
 * @author Taejin Koo
 * @author jaehong.kim - Add DeadlockBo logic
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

        final Object eventMessage = agentEvent.getEventMessage();
        if (eventMessage != null) {
            if (eventMessage instanceof DeadlockBo deadlock) {
                final String deadLockEvent = deadLockEvent(deadlock);
                jgen.writeObjectField("eventMessage", deadLockEvent);
            } else {
                jgen.writeObjectField("eventMessage", eventMessage);
            }
        }

        jgen.writeEndObject();
    }

    private String deadLockEvent(DeadlockBo deadlock) {
        final StringBuilder message = new StringBuilder(128);
        for (ThreadDumpBo threadDump : deadlock.getThreadDumpBoList()) {
            message.append(ThreadDumpUtils.createDumpMessage(threadDump));
        }
        return message.toString();
    }

}
