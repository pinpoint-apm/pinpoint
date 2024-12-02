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

package com.navercorp.pinpoint.bootstrap;

/**
 * @author Woonduk Kang(emeroad)
 */
public enum AgentIdSourceType {
    SYSTEM("SystemProperties(-D)", SystemPropertiesKey.PREFIX, SystemPropertiesKey.AGENT_ID, SystemPropertiesKey.AGENT_NAME, SystemPropertiesKey.APPLICATION_NAME),
    SYSTEM_ENV("EnvironmentVariable", SystemEnvKey.PREFIX, SystemEnvKey.AGENT_ID, SystemEnvKey.AGENT_NAME, SystemEnvKey.APPLICATION_NAME),
    AGENT_ARGUMENT("AgentArgument", AgentArgumentKey.PREFIX, AgentArgumentKey.AGENT_ID, AgentArgumentKey.AGENT_NAME, AgentArgumentKey.APPLICATION_NAME);

    private final String desc;

    private final String prefix;
    private final String agentId;
    private final String agentName;
    private final String applicationName;

    AgentIdSourceType(String desc, String prefix, String agentId, String agentName, String applicationName) {
        this.desc = desc;
        this.prefix = prefix;
        this.agentId = agentId;
        this.agentName = agentName;
        this.applicationName = applicationName;
    }

    public String getDesc() {
        return desc;
    }

    public String getAgentId() {
        return prefix + agentId;
    }

    public String getAgentName() {
        return prefix + agentName;
    }

    public String getApplicationName() {
        return prefix + applicationName;
    }

    @Override
    public String toString() {
        return "AgentIdSourceType{" +
                "desc='" + desc + '\'' +
                ", agentId='" + getAgentId() + '\'' +
                ", agentName='" + getAgentName() + '\'' +
                ", applicationName='" + getAgentName() + '\'' +
                '}';
    }

    static class AgentArgumentKey {
        public static final String PREFIX = "";

        public static final String AGENT_ID = "agentId";
        public static final String AGENT_NAME = "agentName";
        public static final String APPLICATION_NAME = "applicationName";
    }

    static class SystemPropertiesKey {
        public static final String PREFIX = "pinpoint.";

        public static final String AGENT_ID = AgentArgumentKey.AGENT_ID;
        public static final String AGENT_NAME = AgentArgumentKey.AGENT_NAME;
        public static final String APPLICATION_NAME = AgentArgumentKey.APPLICATION_NAME;
    }

    static class SystemEnvKey {
        public static final String PREFIX = "PINPOINT_";

        public static final String AGENT_ID = "AGENT_ID";
        public static final String AGENT_NAME = "AGENT_NAME";
        public static final String APPLICATION_NAME = "APPLICATION_NAME";
    }

}
