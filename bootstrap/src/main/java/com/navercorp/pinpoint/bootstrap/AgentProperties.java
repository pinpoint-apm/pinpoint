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

import com.navercorp.pinpoint.bootstrap.agentdir.Assert;

import java.util.Map;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentProperties {
    private final AgentIdSourceType type;
    private final Properties properties;
    private final String agentKey;
    private final String applicationNameKey;

    public AgentProperties(AgentIdSourceType type, Properties properties, String agentKey, String applicationNameKey) {
        this.type = Assert.requireNonNull(type, "type");
        this.properties = Assert.requireNonNull(properties, "properties");
        this.agentKey = Assert.requireNonNull(agentKey, "agentKey");
        this.applicationNameKey = Assert.requireNonNull(applicationNameKey, "applicationNameKey");
    }

    public AgentProperties(AgentIdSourceType type, Map<String, String> properties, String agentKey, String applicationNameKey) {
        this(type, toProperties(properties), agentKey, applicationNameKey);
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
        return trim(this.properties.getProperty(agentKey));
    }

    public String getAgentKey() {
        return agentKey;
    }

    public String getApplicationName() {
        return trim(this.properties.getProperty(applicationNameKey));
    }

    public String getApplicationNameKey() {
        return applicationNameKey;
    }

    private String trim(String string) {
        if (string == null) {
            return null;
        }
        return string.trim();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentProperties{");
        sb.append("type=").append(type);
        sb.append(", properties=").append(properties);
        sb.append(", agentKey='").append(agentKey).append('\'');
        sb.append(", applicationNameKey='").append(applicationNameKey).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
