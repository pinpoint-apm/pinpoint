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

package com.navercorp.pinpoint.grpc;

import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.protocol.ProtocolVersion;
import io.grpc.Metadata;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class ClientHeaderFactoryV4 implements HeaderFactory {
    private final ProtocolVersion protocolVersion;
    private final String agentId;
    private final String agentName;
    private final String applicationName;
    private final String serviceName;
    private final long agentStartTime;
    private final int serviceType;

    private final String apiKey;

    public ClientHeaderFactoryV4(String agentId, String agentName, String applicationName, String serviceName, int serviceType, long agentStartTime, String apiKey) {
        this.protocolVersion = ProtocolVersion.V4;
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentName = Objects.requireNonNull(agentName, "agentName");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.serviceType = serviceType;
        this.agentStartTime = agentStartTime;
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
    }

    public Metadata newHeader() {
        Metadata headers = new Metadata();
        headers.put(Header.AGENT_ID_KEY, agentId);
        headers.put(Header.APPLICATION_NAME_KEY, applicationName);
        headers.put(Header.AGENT_NAME_KEY, agentName);
        headers.put(Header.SERVICE_NAME_KEY, serviceName);

        headers.put(Header.PROTOCOL_VERSION_NAME_KEY, Integer.toString(protocolVersion.version()));

        headers.put(Header.SERVICE_TYPE_KEY, Integer.toString(serviceType));
        headers.put(Header.AGENT_START_TIME_KEY, Long.toString(agentStartTime));

        headers.put(Header.API_KEY, apiKey);
        return headers;
    }
}
