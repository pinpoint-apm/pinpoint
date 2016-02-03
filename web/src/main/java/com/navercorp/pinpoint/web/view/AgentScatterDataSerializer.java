/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.scatter.AgentScatterData;
import com.navercorp.pinpoint.web.scatter.ScatterAgentInfo;
import com.navercorp.pinpoint.web.scatter.TransactionAgentScatterData;

import java.io.IOException;
import java.util.Map;

/**
 * @Author Taejin Koo
 */
public class AgentScatterDataSerializer extends JsonSerializer<AgentScatterData> {

    @Override
    public void serialize(AgentScatterData value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartArray();
        writeAgentScatterDataGroup(value, jgen);
        jgen.writeEndArray();
    }

    private void writeAgentScatterDataGroup(AgentScatterData value, JsonGenerator jgen) throws IOException {
        Map<ScatterAgentInfo, TransactionAgentScatterData> transactionAgentScatterDataMap = value.getTransactionAgentScatterDataMap();

        for (Map.Entry<ScatterAgentInfo, TransactionAgentScatterData> entry : transactionAgentScatterDataMap.entrySet()) {
            jgen.writeStartObject();

            ScatterAgentInfo key = entry.getKey();

            jgen.writeStringField("agentId", key.getAgentId());
            jgen.writeObjectField("startTime", key.getAgentStartTime());
            jgen.writeObjectField("dotGroup", entry.getValue());

            jgen.writeEndObject();
        }
  }

}
