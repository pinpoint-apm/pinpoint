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
public enum AgentIdSourceType {
    SYSTEM("SystemProperties(-D)", "pinpoint.", "agentId", "agentName", "applicationName"),
    SYSTEM_ENV("EnvironmentVariable", "PINPOINT_", "AGENT_ID", "AGENT_NAME", "APPLICATION_NAME"),
    AGENT_ARGUMENT("AgentArgument", "", SYSTEM.agentId, SYSTEM.agentName, SYSTEM.applicationName);

    private final String desc;

    private final String agentId;
    private final String agentName;
    private final String applicationName;

    AgentIdSourceType(String desc, String prefix, String agentId, String agentName, String applicationName) {
        this.desc = Objects.requireNonNull(desc, "desc");
        this.agentId = prefix + Objects.requireNonNull(agentId, "agentId");
        this.agentName = prefix + Objects.requireNonNull(agentName, "agentName");
        this.applicationName = prefix + Objects.requireNonNull(applicationName, "applicationName");
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

    @Override
    public String toString() {
        return "AgentIdSourceType{" +
                "desc='" + desc + '\'' +
                ", agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                '}';
    }
}
