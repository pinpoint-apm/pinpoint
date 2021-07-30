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
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
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

    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final AgentLifeCycleStateSerializer agentLifeCycleStateSerializer;

    public ServerInstanceSerializer(ServiceTypeRegistryService serviceTypeRegistryService, AgentLifeCycleStateSerializer agentLifeCycleStateSerializer) {
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.agentLifeCycleStateSerializer = Objects.requireNonNull(agentLifeCycleStateSerializer, "agentLifeCycleStateSerializer");
    }

    @Override
    public void serialize(ServerInstance serverInstance, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {

        jgen.writeStartObject();
        final short serviceTypeCode = serverInstance.getServiceTypeCode();
        final ServiceType serviceType = serviceTypeRegistryService.findServiceType(serviceTypeCode);

        jgen.writeBooleanField("hasInspector", hasInspector(serviceType));
        jgen.writeStringField("name", serverInstance.getName());
        jgen.writeStringField("agentName", serverInstance.getAgentName());
        jgen.writeStringField("serviceType", serviceType.getName());

        jgen.writeFieldName("status");
        write(serverInstance.getStatus(), jgen, provider);

        jgen.writeEndObject();

    }

    public void write(AgentLifeCycleState value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        this.agentLifeCycleStateSerializer.serialize(value, jgen, provider);
    }


    public boolean hasInspector(ServiceType serviceType) {
        if (serviceType.isWas()) {
            return true;
        } else {
            return false;
        }
    }


}
