/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.JvmInformation;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.metadata.AgentInfo;

/**
 * @author HyunGil Jeong
 */
public class AgentInfoFactory {

    private final AgentInformation agentInformation;
    private final ServerMetaDataRegistryService serverMetaDataRegistryService;
    private final JvmInformation jvmInformation;

    public AgentInfoFactory(AgentInformation agentInformation, ServerMetaDataRegistryService serverMetaDataRegistryService, JvmInformation jvmInformation) {
        if (agentInformation == null) {
            throw new NullPointerException("agentInformation must not be null");
        }
        if (serverMetaDataRegistryService == null) {
            throw new NullPointerException("serverMetaDataRegistryService must not be null");
        }
        if (jvmInformation == null) {
            throw new NullPointerException("jvmInformation must not be null");
        }
        this.agentInformation = agentInformation;
        this.serverMetaDataRegistryService = serverMetaDataRegistryService;
        this.jvmInformation = jvmInformation;
    }

    public AgentInfo createAgentInfo() {
        final AgentInfo agentInfo = new AgentInfo();
        agentInfo.setAgentInformation(agentInformation);
        final ServerMetaData serverMetaData = serverMetaDataRegistryService.getServerMetaData();
        agentInfo.setServerMetaData(serverMetaData);
        agentInfo.setJvmInfo(jvmInformation);
        return agentInfo;
    }
}