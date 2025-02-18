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
public class AgentProperties{
    private final IdSourceType type;
    private final Function<String, String> properties;

    public AgentProperties(IdSourceType type, Function<String, String> properties) {
        this.type = Objects.requireNonNull(type, "type");
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public IdSourceType getType() {
        return type;
    }

    public ObjectNameProperty getAgentId() {
        return getProperty(type.getAgentId(), type, AgentIdType.AgentId);
    }


    public ObjectNameProperty getAgentName() {
        return getProperty(type.getAgentName(), type, AgentIdType.AgentName);
    }

    public ObjectNameProperty getApplicationName() {
        return getProperty(type.getApplicationName(), type, AgentIdType.ApplicationName);
    }

    public ObjectNameProperty getServiceName() {
        return getProperty(type.getServiceName(), type, AgentIdType.ServiceName);
    }

    public ObjectNameProperty getProperty(String key, IdSourceType sourceType, AgentIdType idType) {
        String value = this.properties.apply(key);
        value = StringUtils.trim(value);
        return new ObjectNameProperty(key, value, sourceType, idType);
    }

    @Override
    public String toString() {
        return "AgentProperties{"
                + type + "="
                + properties +
                '}';
    }

}
