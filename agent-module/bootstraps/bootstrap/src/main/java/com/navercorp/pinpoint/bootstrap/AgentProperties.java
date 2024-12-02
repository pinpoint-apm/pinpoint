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

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentProperties {
    private final AgentIdSourceType type;
    private final Map<String, String> properties;

    public AgentProperties(AgentIdSourceType type, Map<String, String> properties) {
        this.type = Objects.requireNonNull(type, "type");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public static Map<String, String> fromProperties(Properties properties) {
        final Map<String, String> copy = new LinkedHashMap<>();
        final Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            String value = properties.getProperty(key);
            copy.put(key, value);
        }
        return copy;
    }

    public AgentIdSourceType getType() {
        return type;
    }

    public String getAgentId() {
        return trim(this.properties.get(type.getAgentId()));
    }

    public String getAgentName() {
        return trim(this.properties.get(type.getAgentName()));
    }

    public String getAgentIdKey() {
        return type.getAgentId();
    }

    public String getAgentNameKey() {
        return type.getAgentName();
    }

    public String getApplicationName() {
        return trim(this.properties.get(type.getApplicationName()));
    }

    public String getApplicationNameKey() {
        return type.getApplicationName();
    }

    private String trim(String string) {
        if (string == null) {
            return null;
        }
        return string.trim();
    }

    @Override
    public String toString() {
        return "AgentProperties{" +
                "type=" + type +
                ", properties=" + properties +
                '}';
    }
}
