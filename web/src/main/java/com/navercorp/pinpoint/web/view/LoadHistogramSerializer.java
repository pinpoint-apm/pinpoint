/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import com.navercorp.pinpoint.web.applicationmap.histogram.LoadHistogram;

import java.io.IOException;

public class LoadHistogramSerializer extends JsonSerializer<LoadHistogram> {

    @Override
    public void serialize(final LoadHistogram histogram, final JsonGenerator jgen, final SerializerProvider serializerProvider) throws IOException {
        // 1s, 3s, 5s, Slow, Error, Avg, Max, Sum, Tot
        jgen.writeStartArray();
        jgen.writeNumber(histogram.getFastCount());
        jgen.writeNumber(histogram.getNormalCount());
        jgen.writeNumber(histogram.getSlowCount());
        jgen.writeNumber(histogram.getVerySlowCount());
        jgen.writeNumber(histogram.getTotalErrorCount());
        jgen.writeNumber(histogram.getAvgValue());
        jgen.writeNumber(histogram.getMaxValue());
        jgen.writeNumber(histogram.getSumValue());
        jgen.writeNumber(histogram.getTotalCount());
        jgen.writeEndArray();
    }
}
