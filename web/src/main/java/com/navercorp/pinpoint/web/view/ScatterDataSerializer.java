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

import com.navercorp.pinpoint.web.scatter.DotGroup;
import com.navercorp.pinpoint.web.scatter.DotGroups;
import com.navercorp.pinpoint.web.scatter.ScatterAgentMetaData;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.scatter.Dot;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ScatterDataSerializer extends JsonSerializer<ScatterData> {

    @Override
    public void serialize(ScatterData value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();

        ScatterAgentMetaData metadata = value.getScatterAgentMetadata();
        writeScatterAgentMetaData(metadata, jgen);
        writeScatterData(value, metadata, jgen);

        jgen.writeEndObject();
    }

    private void writeScatterAgentMetaData(ScatterAgentMetaData metaData, JsonGenerator jgen) throws IOException {
        jgen.writeObjectField("metadata", metaData);
    }

    private void writeScatterData(ScatterData scatterData, ScatterAgentMetaData metaData, JsonGenerator jgen) throws IOException {
        jgen.writeArrayFieldStart("dotList");

        List<DotGroups> sortedScatterDataMap = scatterData.getScatterData();
        for (DotGroups dotGroups : sortedScatterDataMap) {
            writeDotSet(dotGroups, metaData, jgen);
        }

        jgen.writeEndArray();
    }

    private void writeDotSet(DotGroups dotGroups, ScatterAgentMetaData metaData, JsonGenerator jgen) throws IOException {
        Map<Dot, DotGroup> dotGroupLeaders = dotGroups.getDotGroupLeaders();

        List<Dot> dotSet = dotGroups.getSortedDotSet();
        for (Dot dot : dotSet) {
            final DotGroup dotGroup = dotGroupLeaders.get(dot);
            if (dotGroup != null) {
                writeDot(dot, dotGroup.getDotSize(), metaData, jgen);
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

        jgen.writeNumber(dot.getStatus().getCode());
        jgen.writeNumber(thick);

        jgen.writeEndArray();
    }

}
