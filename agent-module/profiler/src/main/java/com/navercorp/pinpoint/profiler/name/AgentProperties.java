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

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentProperties {
    private final AgentIdSourceType type;
    private final Function<String, String> properties;

    public AgentProperties(AgentIdSourceType type, Function<String, String> properties) {
        this.type = Objects.requireNonNull(type, "type");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public AgentIdSourceType getType() {
        return type;
    }

    public KeyValue getAgentId() {
        return keyValue(type.getAgentId());
    }


    public KeyValue getAgentName() {
        return keyValue(type.getAgentName());
    }

    public KeyValue getApplicationName() {
        return keyValue(type.getApplicationName());
    }

    private KeyValue keyValue(String key) {
        String value = this.properties.apply(key);
        value = StringUtils.trim(value);
        return new KeyValue(key, value);
    }

    public static class KeyValue {
        private final String key;
        private final String name;

        public KeyValue(String key, String name) {
            this.key = Objects.requireNonNull(key, "key");
            this.name = name;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return name;
        }

        @Override
        public String toString() {
            return key + "=" + name;
        }
    }

    @Override
    public String toString() {
        return "AgentProperties{"
                + type + "="
                + properties +
                '}';
    }
}
