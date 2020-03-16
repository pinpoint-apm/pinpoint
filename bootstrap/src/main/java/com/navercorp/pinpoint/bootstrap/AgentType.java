/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Woonduk Kang(emeroad)
 */
public enum AgentType {
    DEFAULT_AGENT( "com.navercorp.pinpoint.profiler.DefaultAgent"),
    PLUGIN_TEST("com.navercorp.pinpoint.test.PluginTestAgent");

    private final String className;

    AgentType(String className) {
        this.className = Assert.requireNonNull(className, "className");
    }

    public String getClassName() {
        return className;
    }


    public static AgentType getAgentType(String agentTypeName) {
        if (agentTypeName == null) {
            return AgentType.DEFAULT_AGENT;
        }

        for (AgentType agentType : AgentType.values()) {
            if (agentType.name().equalsIgnoreCase(agentTypeName)) {
                return agentType;
            }
        }
        throw new IllegalArgumentException("Unknown AgentType:" + agentTypeName);
    }
}