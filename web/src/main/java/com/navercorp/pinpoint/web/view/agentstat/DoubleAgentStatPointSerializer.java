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

package com.navercorp.pinpoint.web.view.agentstat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.DoubleAgentStatPoint;

import java.io.IOException;

/**
 * @author HyunGil Jeong
 */
public class DoubleAgentStatPointSerializer extends JsonSerializer<DoubleAgentStatPoint> {

    @Override
    public void serialize(DoubleAgentStatPoint agentStatPoint, JsonGenerator jgen, SerializerProvider serializers) throws IOException {
        jgen.writeStartArray();
        jgen.writeObject(agentStatPoint.getMin());
        jgen.writeObject(agentStatPoint.getMax());
        jgen.writeObject(agentStatPoint.getAvg());
        jgen.writeObject(agentStatPoint.getSum());
        jgen.writeEndArray();
    }
}
