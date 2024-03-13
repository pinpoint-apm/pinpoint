/*
 * Copyright 2016 NAVER Corp.
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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.vo.Application;

import java.io.IOException;

/**
 * @author HyunGil Jeong
 */
public class ApplicationSerializer extends JsonSerializer<Application> {

    @Override
    public void serialize(Application application, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (application.id() != null) {
            jgen.writeStringField("applicationId", application.id().toString());
        } else {
            jgen.writeNullField("applicationId");
        }
        jgen.writeStringField("applicationName", application.name());
        jgen.writeStringField("serviceType", application.serviceType().getDesc());
        jgen.writeNumberField("code", application.getServiceTypeCode());
        jgen.writeEndObject();
    }
}
