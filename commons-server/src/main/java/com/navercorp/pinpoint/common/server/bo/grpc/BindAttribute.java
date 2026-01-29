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

package com.navercorp.pinpoint.common.server.bo.grpc;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;

public class BindAttribute {
    private final String agentId;
    private final String agentName;
    private final String applicationName;
    private final String serviceName;

    private final long agentStartTime;
    private final long acceptedTime;

    public BindAttribute(String agentId,
                         String agentName,
                         String applicationName,
                         String serviceName,
                         long agentStartTime,
                         long acceptedTime) {
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        this.agentName = agentName;
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.serviceName = StringPrecondition.requireHasLength(serviceName, "serviceName");
        this.agentStartTime = agentStartTime;
        this.acceptedTime = acceptedTime;
    }

    public long getAcceptedTime() {
        return acceptedTime;
    }

    public String getAgentId() {
        return this.agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public long getAgentStartTime() {
        return this.agentStartTime;
    }

    @Override
    public String toString() {
        return "BindAttribute{" +
               "agentId='" + agentId + '\'' +
               ", agentName='" + agentName + '\'' +
               ", applicationName='" + applicationName + '\'' +
                ", serviceName='" + serviceName + '\'' +
               ", agentStartTime=" + agentStartTime +
               ", acceptedTime=" + acceptedTime +
               '}';
    }
}
