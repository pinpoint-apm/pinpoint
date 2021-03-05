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
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentIdResolverBuilder {
    private final List<AgentProperties> agentProperties = new ArrayList<AgentProperties>();
    
    public void addSystemProperties(Properties system) {
        Objects.requireNonNull(system, "system");

        AgentProperties systemProperties = new AgentProperties(AgentIdSourceType.SYSTEM, system,
                AgentIdResolver.AGENT_ID_SYSTEM_PROPERTY, AgentIdResolver.APPLICATION_NAME_SYSTEM_PROPERTY);
        this.agentProperties.add(systemProperties);
    }
    
    public void addEnvProperties(Map<String, String> env) {
        Objects.requireNonNull(env, "env");

        AgentProperties envProperties = new AgentProperties(AgentIdSourceType.SYSTEM_ENV, env,
                AgentIdResolver.AGENT_ID_ENV_PROPERTY, AgentIdResolver.APPLICATION_NAME_ENV_PROPERTY);
        this.agentProperties.add(envProperties);
    }

    public void addAgentArgument(Map<String, String> agentArguments) {
        Objects.requireNonNull(agentArguments, "agentArguments");

        AgentProperties agentArgument = new AgentProperties(AgentIdSourceType.AGENT_ARGUMENT, agentArguments,
                AgentIdResolver.AGENT_ID, AgentIdResolver.APPLICATION_NAME);
        this.agentProperties.add(agentArgument);
    }


    public AgentIdResolver build() {
        List<AgentProperties> copy = new ArrayList<AgentProperties>(this.agentProperties);
        return new AgentIdResolver(copy);
    }
}
