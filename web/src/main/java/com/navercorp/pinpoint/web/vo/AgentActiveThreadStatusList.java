/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Taejin Koo
 */
@JsonSerialize(using = AgentActiveThreadStatusListSerializer.class)
public class AgentActiveThreadStatusList {

    private final List<AgentActiveThreadStatus> agentActiveThreadRepository;

    public AgentActiveThreadStatusList(int initialCapacity) {
        agentActiveThreadRepository = new ArrayList<AgentActiveThreadStatus>(initialCapacity);
    }

    public void add(AgentActiveThreadStatus agentActiveThreadStatus) {
        agentActiveThreadRepository.add(agentActiveThreadStatus);
    }

    public List<AgentActiveThreadStatus> getAgentActiveThreadRepository() {
        return agentActiveThreadRepository;
    }

}

class AgentActiveThreadStatusListSerializer extends JsonSerializer<AgentActiveThreadStatusList>
{
    @Override
    public void serialize(AgentActiveThreadStatusList agentActiveThreadStatusList, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        List<AgentActiveThreadStatus> agentActiveThreadRepository = agentActiveThreadStatusList.getAgentActiveThreadRepository();

        jgen.writeStartObject();

        for (AgentActiveThreadStatus agentActiveThread : agentActiveThreadRepository) {
            jgen.writeFieldName(agentActiveThread.getHostname());
            jgen.writeStartObject();

            TRouteResult routeResult = agentActiveThread.getRouteResult(TRouteResult.UNKNOWN);
            jgen.writeNumberField("code", routeResult.getValue());
            jgen.writeStringField("message", routeResult.name());

            TActiveThreadResponse activeThreadStatus = agentActiveThread.getActiveThreadStatus();
            if (activeThreadStatus != null && activeThreadStatus.getActiveThreadCountSize() >= 4) {
                List<Integer> activeThreadCount = activeThreadStatus.getActiveThreadCount();

                jgen.writeFieldName("status");
                jgen.writeStartArray();
                jgen.writeNumber(activeThreadCount.get(0));
                jgen.writeNumber(activeThreadCount.get(1));
                jgen.writeNumber(activeThreadCount.get(2));
                jgen.writeNumber(activeThreadCount.get(3));
                jgen.writeEndArray();
            }

            jgen.writeEndObject();
        }

        jgen.writeEndObject();
    }
}
