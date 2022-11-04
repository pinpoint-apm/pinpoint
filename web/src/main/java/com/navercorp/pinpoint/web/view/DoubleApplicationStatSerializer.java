/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.web.vo.stat.chart.application.DoubleApplicationStatPoint;

import java.io.IOException;

/**
 * @author Taejin Koo
 */
public class DoubleApplicationStatSerializer extends JsonSerializer<DoubleApplicationStatPoint> {

    @Override
    public void serialize(DoubleApplicationStatPoint doubleApplicationStatPoint, JsonGenerator jgen, SerializerProvider serializers) throws IOException {
        jgen.writeStartArray();
        JoinDoubleFieldBo doubleFieldBo = doubleApplicationStatPoint.getDoubleFieldBo();
        jgen.writeNumber(doubleFieldBo.getMin());
        jgen.writeString(doubleFieldBo.getMinAgentId());
        jgen.writeNumber(doubleFieldBo.getMax());
        jgen.writeString(doubleFieldBo.getMaxAgentId());
        jgen.writeNumber(doubleFieldBo.getAvg());
        jgen.writeEndArray();
    }
}
