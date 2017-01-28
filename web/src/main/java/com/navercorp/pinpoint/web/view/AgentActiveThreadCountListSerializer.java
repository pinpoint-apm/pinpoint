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
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCount;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCountList;

import java.io.IOException;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentActiveThreadCountListSerializer extends JsonSerializer<AgentActiveThreadCountList> {

    @Override
    public void serialize(AgentActiveThreadCountList agentActiveThreadStatusList, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        List<AgentActiveThreadCount> agentActiveThreadRepository = agentActiveThreadStatusList.getAgentActiveThreadRepository();

        jgen.writeStartObject();

        for (AgentActiveThreadCount agentActiveThread : agentActiveThreadRepository) {
            jgen.writeFieldName(agentActiveThread.getAgentId());
            jgen.writeStartObject();

            jgen.writeNumberField("code", agentActiveThread.getCode());
            jgen.writeStringField("message", agentActiveThread.getCodeMessage());

            List<Integer> activeThreadCountList = agentActiveThread.getActiveThreadCountList();
            if (CollectionUtils.nullSafeSize(activeThreadCountList) == 4) {
                jgen.writeFieldName("status");
                jgen.writeStartArray();
                jgen.writeNumber(activeThreadCountList.get(0));
                jgen.writeNumber(activeThreadCountList.get(1));
                jgen.writeNumber(activeThreadCountList.get(2));
                jgen.writeNumber(activeThreadCountList.get(3));
                jgen.writeEndArray();
            }

            jgen.writeEndObject();
        }

        jgen.writeEndObject();
    }

}
