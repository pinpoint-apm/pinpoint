/*
 * Copyright 2014 NAVER Corp.
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.ApplicationAgentList;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class ApplicationAgentListSerializer extends JsonSerializer<ApplicationAgentList> {

    @Override
    public void serialize(ApplicationAgentList applicationAgentList, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        Map<String, List<AgentInfo>> map = applicationAgentList.getApplicationAgentList();

        for (Map.Entry<String, List<AgentInfo>> entry : map.entrySet()) {
            jgen.writeFieldName(entry.getKey());
            writeAgentList(jgen, entry.getValue());
        }

        jgen.writeEndObject();
    }

    private void writeAgentList(JsonGenerator jgen, List<AgentInfo> agentList) throws IOException {
        jgen.writeStartArray();
        for (AgentInfo agentInfo : agentList) {
            jgen.writeObject(agentInfo);
        }
        jgen.writeEndArray();
    }
}