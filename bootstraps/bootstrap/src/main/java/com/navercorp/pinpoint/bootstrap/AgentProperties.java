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

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentProperties {
    private final AgentIdSourceType type;
    private final Properties properties;
    private final String agentIdKey;
    private final String agentNameKey;
    private final String applicationNameKey;
    private final String serviceIdKey;

    public AgentProperties(
            AgentIdSourceType type,
            Properties properties,
            String agentIdKey,
            String agentNameKey,
            String applicationNameKey,
            String serviceIdKey
    ) {
        this.type = Objects.requireNonNull(type, "type");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.agentIdKey = Objects.requireNonNull(agentIdKey, "agentIdKey");
        this.agentNameKey = Objects.requireNonNull(agentNameKey, "agentNameKey");
        this.applicationNameKey = Objects.requireNonNull(applicationNameKey, "applicationNameKey");
        this.serviceIdKey = Objects.requireNonNull(serviceIdKey, "serviceIdKey");
    }

    public AgentProperties(
            AgentIdSourceType type,
            Map<String, String> properties,
            String agentIdKey,
            String agentNameKey,
            String applicationNameKey,
            String serviceIdKey
    ) {
        this(type, toProperties(properties), agentIdKey, agentNameKey, applicationNameKey, serviceIdKey);
    }

    private static Properties toProperties(Map<String, String> properties) {
        final Properties copy = new Properties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            copy.setProperty(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    public AgentIdSourceType getType() {
        return type;
    }

    public String getAgentId() {
        return trim(this.properties.getProperty(agentIdKey));
    }

    public String getAgentName() {
        return trim(this.properties.getProperty(agentNameKey));
    }

    public String getAgentIdKey() {
        return agentIdKey;
    }

    public String getAgentNameKey() {
        return agentNameKey;
    }

    public String getApplicationName() {
        return trim(this.properties.getProperty(applicationNameKey));
    }

    public String getApplicationNameKey() {
        return applicationNameKey;
    }

    public String getServiceId() {
        return trim(this.properties.getProperty(serviceIdKey));
    }

    public String getServiceIdKey() {
        return serviceIdKey;
    }

    private String trim(String string) {
        if (string == null) {
            return null;
        }
        return string.trim();
    }

    @Override
    public String toString() {
        return "AgentProperties{" + "type=" + type +
                ", properties=" + properties +
                ", agentIdKey='" + agentIdKey + '\'' +
                ", agentNameKey='" + agentNameKey + '\'' +
                ", applicationNameKey='" + applicationNameKey + '\'' +
                ", serviceIdKey='" + serviceIdKey + '\'' +
                '}';
    }
}
