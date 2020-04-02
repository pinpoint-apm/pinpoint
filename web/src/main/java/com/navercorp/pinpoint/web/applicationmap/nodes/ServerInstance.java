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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.view.AgentLifeCycleStateSerializer;
import com.navercorp.pinpoint.web.view.ServerInstanceSerializer;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;

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
    private final short serviceTypeCode;

    private final ServerType serverType;

    @JsonSerialize(using = AgentLifeCycleStateSerializer.class)
    private final AgentLifeCycleState status;

    public ServerInstance(AgentInfo agentInfo) {
        Objects.requireNonNull(agentInfo, "agentInfo");

        this.hostName = agentInfo.getHostName();
        this.ip = agentInfo.getIp();
        this.name = agentInfo.getAgentId();
        this.serviceTypeCode = agentInfo.getServiceTypeCode();
        AgentStatus agentStatus = agentInfo.getStatus();
        if (agentStatus != null) {
            this.status = agentStatus.getState();
        } else {
            this.status = AgentLifeCycleState.UNKNOWN;
        }
        this.serverType = ServerType.Physical;
    }

    public ServerInstance(String hostName, String physicalName, short serviceTypeCode) {
        this.hostName = Objects.requireNonNull(hostName, "hostName");
        this.ip = null;
        this.name = Objects.requireNonNull(physicalName, "physicalName");
        this.serviceTypeCode = serviceTypeCode;
        this.status = AgentLifeCycleState.UNKNOWN;
        this.serverType = ServerType.Logical;
    }

    public String getHostName() {
        return hostName;
    }

    public String getName() {
        return name;
    }


    public short getServiceTypeCode() {
        return serviceTypeCode;
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

        if (serviceTypeCode != that.serviceTypeCode) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return serverType == that.serverType;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (int) serviceTypeCode;
        result = 31 * result + (serverType != null ? serverType.hashCode() : 0);
        return result;
    }

}
