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
import com.navercorp.pinpoint.web.scatter.Coordinates;
import com.navercorp.pinpoint.web.scatter.DotGroup;
import com.navercorp.pinpoint.web.scatter.TransactionAgentScatterData;
import com.navercorp.pinpoint.web.vo.scatter.Dot;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author Taejin Koo
 */
public class TransactionAgentScatterDataSerializer extends JsonSerializer<TransactionAgentScatterData> {

    @Override
    public void serialize(TransactionAgentScatterData value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartArray();
        writeDotGroup(value, jgen);
        jgen.writeEndArray();
    }

    private void writeDotGroup(TransactionAgentScatterData value, JsonGenerator jgen) throws IOException {
        Map<Coordinates, DotGroup> dotGroupMap = value.getSortedDotGroupMap();

        for (Map.Entry<Coordinates, DotGroup> entry : dotGroupMap.entrySet()) {
            jgen.writeStartArray();

            Coordinates coordinates = entry.getKey();
            jgen.writeNumber(coordinates.getX());
            jgen.writeNumber(coordinates.getY());

            DotGroup dotGroup = entry.getValue();

            List<Dot> dotList = dotGroup.getSortedDotList();
            writeDotList(coordinates.getX(), coordinates.getY(), dotList, jgen);
            jgen.writeEndArray();
        }
    }

    private void writeDotList(long x, long y, List<Dot> dotList, JsonGenerator jgen) throws IOException {

        for (Dot dot : dotList) {
            jgen.writeStartArray();
            writeDot(x, y, dot, jgen);
            jgen.writeEndArray();
        }

    }

    private void writeDot(long x, long y, Dot dot, JsonGenerator jgen) throws IOException {
        jgen.writeNumber(dot.getAcceptedTime() - x);
        jgen.writeNumber(dot.getElapsedTime() - y);
        jgen.writeNumber(dot.getTransactionId().getTransactionSequence());
        jgen.writeNumber(dot.getSimpleExceptionCode());
    }

}
