/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.name.v4;

import com.navercorp.pinpoint.common.profiler.name.Base64Utils;
import com.navercorp.pinpoint.grpc.protocol.ProtocolVersion;
import com.navercorp.pinpoint.profiler.name.ObjectName;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ObjectNameV4 implements ObjectName {
    private final UUID agentId;
    private final String agentIdStr;
    private final String agentName;
    private final String applicationName;
    private final String serviceName;

    private final String apiKey;

    public ObjectNameV4(UUID agentId, String agentName, String applicationName, String serviceName, String apiKey) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentIdStr = Base64Utils.encode(agentId);
        this.agentName = Objects.requireNonNull(agentName, "agentName");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");

        this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
    }

    @Override
    public ProtocolVersion getVersion() {
        return ProtocolVersion.V4;
    }

    public UUID getAgentUId() {
        return agentId;
    }

    @Override
    public String getAgentId() {
        return agentIdStr;
    }

    @Override
    public String getAgentName() {
        return agentName;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String toString() {
        return "ObjectNameV4{" +
                "agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", serviceName='" + serviceName + '\'' +
//                hide apiKey
//                ", apikey='" + apiKey + '\'' +
                '}';
    }
}
