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
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.scatter.ApplicationScatterScanResult;

import java.io.IOException;

/**
 * @author emeroad
 */
public class ApplicationScatterScanResultSerializer extends JsonSerializer<ApplicationScatterScanResult> {
    @Override
    public void serialize(ApplicationScatterScanResult value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        Application application = value.getApplication();
        jgen.writeStringField("applicationName", application.getName());
        jgen.writeNumberField("applicationServiceType", application.getServiceTypeCode());

        jgen.writeFieldName("scatter");
        jgen.writeObject(value.getScatterScanResult());

        jgen.writeEndObject();
    }
}
