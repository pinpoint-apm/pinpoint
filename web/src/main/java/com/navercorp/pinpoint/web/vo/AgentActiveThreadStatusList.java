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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Taejin Koo
 */
@JsonSerialize(using = AgentActiveThreadStatusListSerializer.class)
public class AgentActiveThreadStatusList {

    private final Map<String, TActiveThreadResponse> agentActiveThreadReposioty;

    public AgentActiveThreadStatusList(Map<String, TActiveThreadResponse> agentActiveThreadReposioty) {
        this.agentActiveThreadReposioty = new HashMap<String, TActiveThreadResponse>();
    }

    public AgentActiveThreadStatusList(int initialCapacity) {
        agentActiveThreadReposioty = new HashMap<String, TActiveThreadResponse>(initialCapacity);
    }

    public void add(String hostName, TActiveThreadResponse activeThreadStatus) {
        agentActiveThreadReposioty.put(hostName, activeThreadStatus);
    }

    public void addAll(Map<String, TActiveThreadResponse> activeThreadStatuses) {
        agentActiveThreadReposioty.putAll(activeThreadStatuses);
    }

    public Map<String, TActiveThreadResponse> getAgentActiveThreadReposioty() {
        return agentActiveThreadReposioty;
    }

}

class AgentActiveThreadStatusListSerializer extends JsonSerializer<AgentActiveThreadStatusList>
{
    @Override
    public void serialize(AgentActiveThreadStatusList agentActiveThreadStatusList, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        Map<String, TActiveThreadResponse> agentActiveThreadReposioty = agentActiveThreadStatusList.getAgentActiveThreadReposioty();

        jgen.writeStartObject();
        for (Map.Entry<String, TActiveThreadResponse> entry : agentActiveThreadReposioty.entrySet()) {
            List<Integer> activeThreadStatus = entry.getValue().getActiveThreadCount();
            if (activeThreadStatus == null || activeThreadStatus.size() < 4) {
                continue;
            }

            jgen.writeFieldName(entry.getKey());
            jgen.writeStartObject();

            jgen.writeFieldName("status");
            jgen.writeStartArray();
            jgen.writeNumber(activeThreadStatus.get(0));
            jgen.writeNumber(activeThreadStatus.get(1));
            jgen.writeNumber(activeThreadStatus.get(2));
            jgen.writeNumber(activeThreadStatus.get(3));
            jgen.writeEndArray();

            jgen.writeNumberField("code", 0);
            // will be added codeMessage

            jgen.writeEndObject();
        }

        jgen.writeEndObject();

    }
}
