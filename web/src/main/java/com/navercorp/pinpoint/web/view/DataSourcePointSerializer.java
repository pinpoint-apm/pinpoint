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
import com.navercorp.pinpoint.web.vo.stat.chart.application.DataSourcePoint;

import java.io.IOException;

/**
 * @author HyunGil Jeong
 */
public class DataSourcePointSerializer extends JsonSerializer<DataSourcePoint> {

    @Override
    public void serialize(DataSourcePoint dataSourcePoint, JsonGenerator jgen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeNumber(dataSourcePoint.getYValForMin());
        jgen.writeString(dataSourcePoint.getAgentIdForMin());
        jgen.writeNumber(dataSourcePoint.getYValForMax());
        jgen.writeString(dataSourcePoint.getAgentIdForMax());
        jgen.writeNumber(dataSourcePoint.getYValForAvg());
        jgen.writeEndArray();
    }
}
