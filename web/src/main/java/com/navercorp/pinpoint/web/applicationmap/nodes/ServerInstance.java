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

package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.view.ServerInstanceSerializer;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;

import java.util.Objects;

/**
 *
 * @author netspider
 * @author emeroad
 * @author HyunGil Jeong
 */
@JsonSerialize(using = ServerInstanceSerializer.class)
public class ServerInstance {

    private final String hostName;
    private final String ip;

    private final String name;
    private final String agentName;
    private final ServiceType serviceType;

    private final ServerType serverType;

    private final AgentLifeCycleState status;


    public ServerInstance(AgentInfo agentInfo, AgentStatus agentStatus) {
        Objects.requireNonNull(agentInfo, "agentInfo");
        this.hostName = agentInfo.getHostName();
        this.ip = agentInfo.getIp();
        this.name = agentInfo.getAgentId().value();
        this.agentName = agentInfo.getAgentName();
        this.serviceType = agentInfo.getServiceType();
        this.status = getAgentLifeCycleState(agentStatus);
        this.serverType = ServerType.Physical;
    }

    private AgentLifeCycleState getAgentLifeCycleState(AgentStatus agentStatus) {
        if (agentStatus != null) {
            return agentStatus.getState();
        } else {
            return AgentLifeCycleState.UNKNOWN;
        }
    }

    public ServerInstance(String hostName, String physicalName, ServiceType serviceType) {
        this.hostName = Objects.requireNonNull(hostName, "hostName");
        this.ip = null;
        this.agentName = null;
        this.name = Objects.requireNonNull(physicalName, "physicalName");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
        this.status = AgentLifeCycleState.UNKNOWN;
        this.serverType = ServerType.Logical;
    }

    public String getHostName() {
        return hostName;
    }

    public String getName() {
        return name;
    }

    public String getAgentName() {
        return agentName;
    }

    public short getServiceTypeCode() {
        return serviceType.getCode();
    }

    @JsonIgnore
    public ServiceType getServiceType() {
        return serviceType;
    }

    public AgentLifeCycleState getStatus() {
        return status;
    }

    public ServerType getServerType() {
        return serverType;
    }
    
    public String getIp() {
        return ip;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerInstance that = (ServerInstance) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return serviceType != null ? serviceType.equals(that.serviceType) : that.serviceType == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (serviceType != null ? serviceType.hashCode() : 0);
        return result;
    }

}
