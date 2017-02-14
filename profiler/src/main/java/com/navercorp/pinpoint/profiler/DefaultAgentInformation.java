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

import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class DefaultAgentInformation implements AgentInformation {
    private final String agentId;
    private final String applicationName;
    private final long startTime;
    private final int pid;
    private final String machineName;
    private final String hostIp;
    private final ServiceType serverType;
    private final String jvmVersion;
    private final String agentVersion;

    public DefaultAgentInformation(
            String agentId,
            String applicationName,
            long startTime,
            int pid,
            String machineName,
            String hostIp,
            ServiceType serverType,
            String jvmVersion,
            String agentVersion) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (machineName == null) {
            throw new NullPointerException("machineName must not be null");
        }
        if (agentVersion == null) {
            throw new NullPointerException("version must not be null");
        }
        this.agentId = agentId;
        this.applicationName = applicationName;
        this.startTime = startTime;
        this.pid = pid;
        this.machineName = machineName;
        this.hostIp = hostIp;
        this.serverType = serverType;
        this.jvmVersion = jvmVersion;
        this.agentVersion = agentVersion;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
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
        final StringBuilder sb = new StringBuilder("AgentInformation{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", applicationName='").append(applicationName).append('\'');
        sb.append(", startTime=").append(startTime);
        sb.append(", pid=").append(pid);
        sb.append(", machineName='").append(machineName).append('\'');
        sb.append(", hostIp='").append(hostIp).append('\'');
        sb.append(", serverType=").append(serverType);
        sb.append(", jvmVersion='").append(jvmVersion).append('\'');
        sb.append(", agentVersion='").append(agentVersion).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
