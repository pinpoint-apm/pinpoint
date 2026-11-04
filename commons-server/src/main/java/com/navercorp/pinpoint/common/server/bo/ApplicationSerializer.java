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

package com.navercorp.pinpoint.common.server.bo;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;

/**
 * @author HyunGil Jeong
 */
public class ApplicationSerializer extends ValueSerializer<Application> {

    @Override
    public void serialize(Application application, JsonGenerator jgen, SerializationContext provider) {
        jgen.writeStartObject();
        jgen.writeStringProperty("applicationName", application.getApplicationName());
        jgen.writeStringProperty("serviceType", application.getServiceType().getDesc());
        jgen.writeNumberProperty("code", application.getServiceTypeCode());
        jgen.writeEndObject();
    }
}
