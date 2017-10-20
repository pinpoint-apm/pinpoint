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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadDump;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadDumpList;

import java.io.IOException;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class    AgentActiveThreadDumpListSerializer extends JsonSerializer<AgentActiveThreadDumpList> {

    @Override
    public void serialize(AgentActiveThreadDumpList agentActiveThreadDumpList, JsonGenerator jgen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        List<AgentActiveThreadDump> agentActiveThreadDumpRepository = agentActiveThreadDumpList.getSortOldestAgentActiveThreadDumpRepository();

        jgen.writeStartArray();
        for (AgentActiveThreadDump agentActiveThreadDump : agentActiveThreadDumpRepository) {
            jgen.writeStartObject();

            String hexStringThreadId = Long.toHexString(agentActiveThreadDump.getThreadId());
            jgen.writeStringField("threadId", "0x" + hexStringThreadId);

            jgen.writeStringField("threadName", agentActiveThreadDump.getThreadName());

            TThreadState threadState = agentActiveThreadDump.getThreadState();
            if (threadState == null) {
                jgen.writeStringField("threadState", TThreadState.UNKNOWN.name());
            } else {
                jgen.writeStringField("threadState", agentActiveThreadDump.getThreadState().name());
            }

            jgen.writeNumberField("startTime", agentActiveThreadDump.getStartTime());
            jgen.writeNumberField("execTime", agentActiveThreadDump.getExecTime());
            jgen.writeNumberField("localTraceId", agentActiveThreadDump.getLocalTraceId());

            jgen.writeBooleanField("sampled", agentActiveThreadDump.isSampled());
            jgen.writeStringField("transactionId", agentActiveThreadDump.getTransactionId());
            jgen.writeStringField("entryPoint", agentActiveThreadDump.getEntryPoint());

            jgen.writeStringField("detailMessage", agentActiveThreadDump.getDetailMessage());

            jgen.writeEndObject();
        }
        jgen.writeEndArray();
    }

}
