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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentIdResolverBuilder {
    private final List<AgentProperties> agentProperties = new ArrayList<>();
    
    public void addSystemProperties(Function<String, String> system) {
        Objects.requireNonNull(system, "system");

        AgentProperties properties = new AgentProperties(AgentIdSourceType.SYSTEM, system);
        this.agentProperties.add(properties);
    }
    
    public void addEnvProperties(Function<String, String> env) {
        Objects.requireNonNull(env, "env");

        AgentProperties properties = new AgentProperties(AgentIdSourceType.SYSTEM_ENV, env);
        this.agentProperties.add(properties);
    }

    public void addAgentArgument(Function<String, String> agentArguments) {
        Objects.requireNonNull(agentArguments, "agentArguments");

        AgentProperties properties = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, agentArguments);
        this.agentProperties.add(properties);
    }


    public AgentIdResolver build() {
        List<AgentProperties> copy = new ArrayList<>(this.agentProperties);
        return new AgentIdResolver(copy);
    }
}
