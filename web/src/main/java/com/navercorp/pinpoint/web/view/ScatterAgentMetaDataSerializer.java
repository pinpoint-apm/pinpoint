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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.scatter.ScatterAgentMetaData;
import com.navercorp.pinpoint.web.vo.scatter.DotAgentInfo;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class ScatterAgentMetaDataSerializer extends JsonSerializer<ScatterAgentMetaData> {

    @Override
    public void serialize(ScatterAgentMetaData value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();

        Set<Map.Entry<Integer, DotAgentInfo>> entries = value.entrySet();
        for (Map.Entry<Integer, DotAgentInfo> entry : entries) {
            writeMetadata(entry.getKey(), entry.getValue(), jgen);
        }

        jgen.writeEndObject();
    }


    private void writeMetadata(Integer key, DotAgentInfo agentInfo, JsonGenerator jgen) throws IOException {
        jgen.writeArrayFieldStart(key.toString());

        jgen.writeString(agentInfo.getAgentId());
        jgen.writeString(agentInfo.getTransactionAgentId());
        jgen.writeNumber(agentInfo.getTransactionAgentStartTime());

        jgen.writeEndArray();
    }

}