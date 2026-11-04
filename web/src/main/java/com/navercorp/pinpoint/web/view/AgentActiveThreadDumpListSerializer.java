/*
 * Copyright 2016 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.threaddump.TThreadState;
import com.navercorp.pinpoint.web.vo.activethread.AgentActiveThreadDump;
import com.navercorp.pinpoint.web.vo.activethread.AgentActiveThreadDumpList;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentActiveThreadDumpListSerializer extends ValueSerializer<AgentActiveThreadDumpList> {

    @Override
    public void serialize(AgentActiveThreadDumpList agentActiveThreadDumpList, JsonGenerator jgen, SerializationContext serializers) {
        List<AgentActiveThreadDump> agentActiveThreadDumpRepository = agentActiveThreadDumpList.getSortOldestAgentActiveThreadDumpRepository();

        jgen.writeStartArray();
        for (AgentActiveThreadDump agentActiveThreadDump : agentActiveThreadDumpRepository) {
            jgen.writeStartObject();

            String hexStringThreadId = Long.toHexString(agentActiveThreadDump.getThreadId());
            jgen.writeStringProperty("threadId", "0x" + hexStringThreadId);

            jgen.writeStringProperty("threadName", agentActiveThreadDump.getThreadName());

            TThreadState threadState = agentActiveThreadDump.getThreadState();
            if (threadState == null) {
                jgen.writeStringProperty("threadState", TThreadState.UNKNOWN.name());
            } else {
                jgen.writeStringProperty("threadState", agentActiveThreadDump.getThreadState().name());
            }

            jgen.writeNumberProperty("startTime", agentActiveThreadDump.getStartTime());
            jgen.writeNumberProperty("execTime", agentActiveThreadDump.getExecTime());
            jgen.writeNumberProperty("localTraceId", agentActiveThreadDump.getLocalTraceId());

            jgen.writeBooleanProperty("sampled", agentActiveThreadDump.isSampled());
            jgen.writeStringProperty("transactionId", agentActiveThreadDump.getTransactionId());
            jgen.writeStringProperty("entryPoint", agentActiveThreadDump.getEntryPoint());

            jgen.writeStringProperty("detailMessage", agentActiveThreadDump.getDetailMessage());

            jgen.writeEndObject();
        }
        jgen.writeEndArray();
    }

}
