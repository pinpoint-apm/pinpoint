/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.SystemPropertyKey;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.DefaultAgentInformation;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentName;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.ApplicationName;
import com.navercorp.pinpoint.profiler.context.module.ApplicationServerType;
import com.navercorp.pinpoint.profiler.context.module.Container;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentInformationProvider implements Provider<AgentInformation> {

    private final String agentId;
    private final String agentName;
    private final String applicationName;
    private final boolean isContainer;
    private final long agentStartTime;
    private final ServiceType serverType;

    @Inject
    public AgentInformationProvider(@AgentId String agentId, @AgentName String agentName, @ApplicationName String applicationName,
                                    @Container boolean isContainer, @AgentStartTime long agentStartTime, @ApplicationServerType ServiceType serverType) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(applicationName, "applicationName");

        this.agentId = checkId("agentId", agentId, PinpointConstants.AGENT_ID_MAX_LEN);
        this.applicationName = checkId("applicationName", applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN);
        this.agentName = agentName;
        this.isContainer = isContainer;
        this.agentStartTime = agentStartTime;
        this.serverType = Objects.requireNonNull(serverType, "serverType");

    }

    public AgentInformation get() {
        return createAgentInformation();
    }

    public AgentInformation createAgentInformation() {
        final String machineName = NetworkUtils.getHostName();
        final String hostIp = NetworkUtils.getRepresentationHostIp();

        final int pid = RuntimeMXBeanUtils.getPid();
        final String jvmVersion = JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VERSION);
        return new DefaultAgentInformation(agentId, agentName, applicationName, isContainer, agentStartTime, pid, machineName, hostIp, serverType, jvmVersion, Version.VERSION);
    }

    private String checkId(String keyName, String id, int maxLen) {
        if (!IdValidateUtils.validateId(id, maxLen)) {
            throw new IllegalArgumentException("invalid " + keyName + "=" + id);
        }
        return id;
    }

}
