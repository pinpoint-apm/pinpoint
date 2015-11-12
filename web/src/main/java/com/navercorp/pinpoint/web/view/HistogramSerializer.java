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

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author emeroad
 */
public class HistogramSerializer extends JsonSerializer<Histogram> {


    @Override
    public void serialize(Histogram histogram, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        final HistogramSchema schema = histogram.getHistogramSchema();

        jgen.writeFieldName(schema.getFastSlot().getSlotName());
        jgen.writeNumber(histogram.getFastCount());

        jgen.writeFieldName(schema.getNormalSlot().getSlotName());
        jgen.writeNumber(histogram.getNormalCount());

        jgen.writeFieldName(schema.getSlowSlot().getSlotName());
        jgen.writeNumber(histogram.getSlowCount());

        jgen.writeFieldName(schema.getVerySlowSlot().getSlotName());
        jgen.writeNumber(histogram.getVerySlowCount());

        jgen.writeFieldName(schema.getErrorSlot().getSlotName());
        jgen.writeNumber(histogram.getTotalErrorCount());

        jgen.writeEndObject();
    }
}