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

package com.navercorp.pinpoint.common.server.bo.metric;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class DefaultCustomMetricType implements CustomMetricType {

    private final AgentStatType agentStatType;
    private final FieldDescriptors fieldDescriptors;

    public DefaultCustomMetricType(AgentStatType agentStatType, FieldDescriptors fieldDescriptors) {
        this.agentStatType = Objects.requireNonNull(agentStatType, "agentStatType");
        this.fieldDescriptors = Objects.requireNonNull(fieldDescriptors, "fieldDescriptors");
    }

    @Override
    public AgentStatType getAgentStatType() {
        return agentStatType;
    }

    @Override
    public FieldDescriptors getFieldDescriptors() {
        return fieldDescriptors;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultCustomMetricType{");
        sb.append("agentStatType=").append(agentStatType);
        sb.append(", fieldDescriptors=").append(fieldDescriptors);
        sb.append('}');
        return sb.toString();
    }

}
