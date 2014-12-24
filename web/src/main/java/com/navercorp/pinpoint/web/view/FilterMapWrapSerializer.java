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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.applicationmap.FilterMapWrap;
import com.navercorp.pinpoint.web.applicationmap.Node;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.scatter.ApplicationScatterScanResult;

import java.io.IOException;
import java.util.List;

/**
 * @author emeroad
 */
public class FilterMapWrapSerializer extends JsonSerializer<FilterMapWrap> {
    @Override
    public void serialize(FilterMapWrap wrap, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();

        jgen.writeObjectField("applicationMapData", wrap.getApplicationMap());

        jgen.writeNumberField("lastFetchedTimestamp", wrap.getLastFetchedTimestamp());

        final List<ApplicationScatterScanResult> applicationScatterScanResult = wrap.getApplicationScatterScanResult();

        jgen.writeFieldName("applicationScatterScanResult");
        jgen.writeStartObject();
        for (ApplicationScatterScanResult scatterScanResult : applicationScatterScanResult) {
            Application application = scatterScanResult.getApplication();
            String name = application.getName() + Node.NODE_DELIMITER + application.getServiceType().toString();
            jgen.writeObjectField(name, scatterScanResult.getScatterScanResult());
        }
        jgen.writeEndObject();


        jgen.writeEndObject();
    }
}
