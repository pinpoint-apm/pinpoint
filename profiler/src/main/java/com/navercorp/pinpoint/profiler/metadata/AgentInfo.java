/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.JvmInformation;

/**
 * @author jaehong.kim
 */
public class AgentInfo {
    private final AgentInformation agentInformation;
    private final ServerMetaData serverMetaData;
    private final JvmInformation jvmInformation;

    public AgentInfo(AgentInformation agentInformation, ServerMetaData serverMetaData, JvmInformation jvmInformation) {
        this.agentInformation = Assert.requireNonNull(agentInformation, "agentInformation");
        this.serverMetaData = Assert.requireNonNull(serverMetaData, "serverMetaData");
        this.jvmInformation = Assert.requireNonNull(jvmInformation, "jvmInformation");
    }

    public AgentInformation getAgentInformation() {
        return agentInformation;
    }

    public ServerMetaData getServerMetaData() {
        return serverMetaData;
    }

    public JvmInformation getJvmInfo() {
        return jvmInformation;
    }

    @Override
    public String toString() {
        return "AgentInfo{" +
                "agentInformation=" + agentInformation +
                ", serverMetaData=" + serverMetaData +
                ", jvmInformation=" + jvmInformation +
                '}';
    }
}
