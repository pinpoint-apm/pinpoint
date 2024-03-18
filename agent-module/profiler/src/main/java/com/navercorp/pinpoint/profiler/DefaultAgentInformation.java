/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class DefaultAgentInformation implements AgentInformation {
    private final AgentId agentId;
    private final String agentName;
    private final String applicationName;
    private final String serviceName;
    private final boolean isContainer;
    private final long startTime;
    private final int pid;
    private final String machineName;
    private final String hostIp;
    private final ServiceType serverType;
    private final String jvmVersion;
    private final String agentVersion;

    public DefaultAgentInformation(
            AgentId agentId,
            String agentName,
            String applicationName,
            String serviceName,
            boolean isContainer,
            long startTime,
            int pid,
            String machineName,
            String hostIp,
            ServiceType serverType,
            String jvmVersion,
            String agentVersion) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentName = Objects.requireNonNull(agentName, "agentName");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.isContainer = isContainer;
        this.startTime = startTime;
        this.pid = pid;
        this.machineName = Objects.requireNonNull(machineName, "machineName");
        this.hostIp = hostIp;
        this.serverType = serverType;
        this.jvmVersion = jvmVersion;
        this.agentVersion = Objects.requireNonNull(agentVersion, "agentVersion");
    }

    @Override
    public AgentId getAgentId() {
        return agentId;
    }

    @Override
    public String getAgentName() {
        return agentName;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean isContainer() {
        return isContainer;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public int getPid() {
        return pid;
    }

    @Override
    public String getMachineName() {
        return machineName;
    }

    @Override
    public String getHostIp() {
        return hostIp;
    }

    @Override
    public ServiceType getServerType() {
        return serverType;
    }
    
    @Override
    public String getJvmVersion() {
        return this.jvmVersion;
    }

    @Override
    public String getAgentVersion() {
        return agentVersion;
    }

    @Override
    public String toString() {
        return "{" + "agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", isContainer=" + isContainer +
                ", startTime=" + startTime +
                ", pid=" + pid +
                ", machineName='" + machineName + '\'' +
                ", hostIp='" + hostIp + '\'' +
                ", serverType=" + serverType +
                ", jvmVersion='" + jvmVersion + '\'' +
                ", agentVersion='" + agentVersion + '\'' +
                '}';
    }
}
