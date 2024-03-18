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
    private final String agentNameKey;
    private final String applicationNameKey;
    private final String serviceNameKey;

    public AgentProperties(AgentIdSourceType type, Properties properties, String agentNameKey, String applicationNameKey, String serviceNameKey) {
        this.type = Objects.requireNonNull(type, "type");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.agentNameKey = Objects.requireNonNull(agentNameKey, "agentNameKey");
        this.applicationNameKey = Objects.requireNonNull(applicationNameKey, "applicationNameKey");
        this.serviceNameKey = Objects.requireNonNull(serviceNameKey, "serviceNameKey");
    }

    public AgentProperties(AgentIdSourceType type, Map<String, String> properties, String agentNameKey, String applicationNameKey, String serviceNameKey) {
        this(type, toProperties(properties), agentNameKey, applicationNameKey, serviceNameKey);
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

    public String getAgentName() {
        return trim(this.properties.getProperty(agentNameKey));
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

    public String getServiceName() {
        return trim(this.properties.getProperty(serviceNameKey));
    }

    public String getServiceNameKey() {
        return serviceNameKey;
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
                ", agentNameKey='" + agentNameKey + '\'' +
                ", applicationNameKey='" + applicationNameKey + '\'' +
                ", serviceNameKey='" + serviceNameKey + '\'' +
                '}';
    }
}
