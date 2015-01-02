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

import com.navercorp.pinpoint.common.ServiceType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class AgentInformation {
    private final String agentId;
    private final String applicationName;
    private final long startTime;
    private final int pid;
    private final String machineName;
    private final String hostIp;
    private final short serverType;
    private final ServiceType serverServiceType;
    private final String version;

    public AgentInformation(String agentId, String applicationName, long startTime, int pid, String machineName, String hostIp, short serverType, String version) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (machineName == null) {
            throw new NullPointerException("machineName must not be null");
        }
        if (version == null) {
            throw new NullPointerException("version must not be null");
        }
        this.agentId = agentId;
        this.applicationName = applicationName;
        this.startTime = startTime;
        this.pid = pid;
        this.machineName = machineName;
        this.hostIp = hostIp;
        this.serverType = serverType;
        this.serverServiceType =  ServiceType.findServiceType(serverType);
        this.version = version;
    }



    public String getAgentId() {
        return agentId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getPid() {
        return pid;
    }

    public String getMachineName() {
        return machineName;
    }
    
    public String getHostIp() {
        return hostIp;
    }

    public short getServerType() {
        return serverType;
    }

    public ServiceType getServerServiceType() {
        return serverServiceType;
    }

    public String getVersion() {
        return version;
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();

    	map.put(AgentHandshakePropertyType.AGENT_ID.getName(), this.agent    d);
    	map.put(AgentHandshakePropertyType.APPLICATION_NAME.getName(), this.applicatio    Name);
    	map.put(AgentHandshakePropertyType.HOSTNAME.getName(), this.mac    ineName);
    	map.put(AgentHandshakePropertyType.IP.getName(),     his.hostIp);
    	map.put(AgentHandshakePropertyType.PID.getNa    e(), this.pid);
    	map.put(AgentHandshakePropertyType.SERVICE_TYPE.getName()     this.serverType);
    	map.put(AgentHandshakePropertyType.START_TIMESTAMP.getNa    e(), this.startTime);
    	map.put(AgentHandshakePropertyType.VERSION.    et    ame(), this.version);
    	
    	return map;
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
        sb.append(", version='").append(version).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
