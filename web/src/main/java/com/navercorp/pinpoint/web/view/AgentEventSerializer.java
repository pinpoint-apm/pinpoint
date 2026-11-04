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
import com.navercorp.pinpoint.common.server.bo.event.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadDumpBo;
import com.navercorp.pinpoint.web.util.ThreadDumpUtils;
import com.navercorp.pinpoint.web.vo.AgentEvent;

/**
 * @author Taejin Koo
 * @author jaehong.kim - Add DeadlockBo logic
 */
public class AgentEventSerializer extends ValueSerializer<AgentEvent> {

    @Override
    public void serialize(AgentEvent agentEvent, JsonGenerator jgen, SerializationContext serializers) {
        jgen.writeStartObject();

        if (agentEvent.getAgentId() != null) {
            jgen.writeStringProperty("agentId", agentEvent.getAgentId());
        }

        jgen.writeNumberProperty("startTimestamp", agentEvent.getStartTimestamp());
        jgen.writeNumberProperty("eventTimestamp", agentEvent.getEventTimestamp());
        jgen.writeNumberProperty("eventTypeCode", agentEvent.getEventTypeCode());

        if (agentEvent.getEventTypeDesc() != null) {
            jgen.writeStringProperty("eventTypeDesc", agentEvent.getEventTypeDesc());
        }

        jgen.writeBooleanProperty("hasEventMessage", agentEvent.hasEventMessage());

        final Object eventMessage = agentEvent.getEventMessage();
        if (eventMessage != null) {
            if (eventMessage instanceof DeadlockBo deadlock) {
                final String deadLockEvent = deadLockEvent(deadlock);
                jgen.writePOJOProperty("eventMessage", deadLockEvent);
            } else {
                jgen.writePOJOProperty("eventMessage", eventMessage);
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
