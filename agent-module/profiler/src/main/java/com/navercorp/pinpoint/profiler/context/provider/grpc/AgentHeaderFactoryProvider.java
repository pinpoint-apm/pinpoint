/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.provider.grpc;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.grpc.ClientHeaderFactoryV1;
import com.navercorp.pinpoint.grpc.ClientHeaderFactoryV4;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.protocol.ProtocolVersion;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import com.navercorp.pinpoint.profiler.name.v4.ObjectNameV4;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentHeaderFactoryProvider implements Provider<HeaderFactory> {
    private final ObjectName objectName;
    private final AgentInformation agentInformation;

    @Inject
    public AgentHeaderFactoryProvider(ObjectName objectName, AgentInformation agentInformation) {
        this.objectName = Objects.requireNonNull(objectName, "objectName");
        this.agentInformation = Objects.requireNonNull(agentInformation, "agentInformation");
    }

    @Override
    public HeaderFactory get() {
        ProtocolVersion version = objectName.getVersion();
        if (version == ProtocolVersion.V4) {
            return createV4();
        }
        return createV1();
    }

    private HeaderFactory createV1() {
        String agentId = objectName.getAgentId();
        String agentName = objectName.getAgentName();
        String applicationName = objectName.getApplicationName();
        return new ClientHeaderFactoryV1(agentId, agentName, applicationName, agentInformation.getServerType().getCode(), agentInformation.getStartTime());
    }

    private HeaderFactory createV4() {
        if (objectName instanceof ObjectNameV4) {
            ObjectNameV4 objectName = (ObjectNameV4) this.objectName;
            String agentId = objectName.getAgentId();
            String agentName = objectName.getAgentName();
            String applicationName = objectName.getApplicationName();
            String serviceName = objectName.getServiceName();
            String apiKey = objectName.getApiKey();
            return new ClientHeaderFactoryV4(agentId, agentName, applicationName, serviceName, agentInformation.getServerType().getCode(), agentInformation.getStartTime(), apiKey);
        }
        throw new IllegalStateException("unsupported ObjectType:" + objectName);
    }
}
