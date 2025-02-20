/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.name;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public enum IdSourceType {
    SYSTEM("SystemProperties(-D)", "pinpoint.%s", "agentId", "agentName", "applicationName", "serviceName"),
    SYSTEM_ENV("EnvironmentVariable", "PINPOINT_%s", "AGENT_ID", "AGENT_NAME", "APPLICATION_NAME", "SERVICE_NAME"),
    AGENT_ARGUMENT("AgentArgument", "%s", SYSTEM.agentId, SYSTEM.agentName, SYSTEM.applicationName, SYSTEM.serviceName);

    private final String desc;

    private final String agentId;
    private final String agentName;
    private final String applicationName;
    private final String serviceName;

    IdSourceType(String desc, String format, String agentId, String agentName, String applicationName, String serviceName) {
        this.desc = Objects.requireNonNull(desc, "desc");
        this.agentId = String.format(format, Objects.requireNonNull(agentId, "agentId"));
        this.agentName = String.format(format, Objects.requireNonNull(agentName, "agentName"));
        this.applicationName = String.format(format, Objects.requireNonNull(applicationName, "applicationName"));
        this.serviceName = String.format(format, Objects.requireNonNull(serviceName, "serviceName"));
    }

    public String getDesc() {
        return desc;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String toString() {
        return "AgentIdSourceType{" +
                "desc='" + desc + '\'' +
                ", agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }
}
