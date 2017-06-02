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
import com.navercorp.pinpoint.web.scatter.DotGroup;
import com.navercorp.pinpoint.web.scatter.DotGroups;
import com.navercorp.pinpoint.web.scatter.ScatterAgentMetaData;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.scatter.Dot;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class ScatterDataSerializer extends JsonSerializer<ScatterData> {

    @Override
    public void serialize(ScatterData value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();

        ScatterAgentMetaData metadata = value.getScatterAgentMetadata();
        writeScatterAgentMetaData(metadata, jgen);
        wrtieScatterData(value, metadata, jgen);

        jgen.writeEndObject();
    }

    private void writeScatterAgentMetaData(ScatterAgentMetaData metaData, JsonGenerator jgen) throws IOException {
        jgen.writeObjectField("metadata", metaData);
    }

    private void wrtieScatterData(ScatterData scatterData, ScatterAgentMetaData metaData, JsonGenerator jgen) throws IOException {
        jgen.writeArrayFieldStart("dotList");

        Map<Long, DotGroups> sortedScatterDataMap = scatterData.getSortedScatterDataMap();
        for (Map.Entry<Long, DotGroups> entry : sortedScatterDataMap.entrySet()) {
            DotGroups dotGroups = entry.getValue();
            writeDotSet(dotGroups, metaData, jgen);
        }

        jgen.writeEndArray();
    }

    private void writeDotSet(DotGroups dotGroups, ScatterAgentMetaData metaData, JsonGenerator jgen) throws IOException {
        Map<Dot, DotGroup> dotGroupLeaders = dotGroups.getDotGroupLeaders();

        Set<Dot> dotSet = dotGroups.getSortedDotSet();
        for (Dot dot : dotSet) {
            if (dotGroupLeaders.containsKey(dot)) {
                writeDot(dot, dotGroupLeaders.get(dot).getDotSize(), metaData, jgen);
            } else {
                writeDot(dot, 0, metaData, jgen);
            }
        }
    }

    private void writeDot(Dot dot, int thick, ScatterAgentMetaData metaData, JsonGenerator jgen) throws IOException {
        jgen.writeStartArray();

        jgen.writeNumber(dot.getAcceptedTime());
        jgen.writeNumber(dot.getElapsedTime());

        int agentId = metaData.getId(dot);
        jgen.writeNumber(agentId);

        if (agentId == -1) {
            jgen.writeString(dot.getTransactionIdAsString());
        } else {
            jgen.writeNumber(dot.getTransactionId().getTransactionSequence());
        }

        jgen.writeNumber(dot.getSimpleExceptionCode());
        jgen.writeNumber(thick);

        jgen.writeEndArray();
    }

}
