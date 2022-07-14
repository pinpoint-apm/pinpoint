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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstance;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * @author emeroad
 * @author minwoo.jung
 */
@Component
public class ServerInstanceSerializer extends JsonSerializer<ServerInstance> {


    public ServerInstanceSerializer() {
    }

    @Override
    public void serialize(ServerInstance serverInstance, JsonGenerator jgen, SerializerProvider provider) throws IOException {

        jgen.writeStartObject();

        final ServiceType serviceType = serverInstance.getServiceType();
        jgen.writeBooleanField("hasInspector", hasInspector(serviceType));
        jgen.writeStringField("name", serverInstance.getName());
        jgen.writeStringField("agentName", serverInstance.getAgentName());
        jgen.writeStringField("serviceType", serviceType.getName());

        jgen.writeObjectField("status", serverInstance.getStatus());
        jgen.writeEndObject();

    }


    public boolean hasInspector(ServiceType serviceType) {
        if (serviceType.isWas()) {
            return true;
        } else {
            return false;
        }
    }

}
