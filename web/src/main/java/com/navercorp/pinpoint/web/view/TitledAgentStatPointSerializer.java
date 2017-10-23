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
import com.navercorp.pinpoint.web.vo.stat.chart.agent.TitledAgentStatPoint;

import java.io.IOException;

/**
 * @author HyunGil Jeong
 */
public class TitledAgentStatPointSerializer extends JsonSerializer<TitledAgentStatPoint<? extends Number>> {

    @Override
    public void serialize(TitledAgentStatPoint<? extends Number> titledAgentStatPoint, JsonGenerator jgen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeObject(titledAgentStatPoint.getMinYVal());
        jgen.writeObject(titledAgentStatPoint.getMaxYVal());
        jgen.writeObject(titledAgentStatPoint.getAvgYVal());
        jgen.writeObject(titledAgentStatPoint.getSumYVal());
        jgen.writeObject(titledAgentStatPoint.getTitle());
        jgen.writeEndArray();
    }
}
