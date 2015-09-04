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

package com.navercorp.pinpoint.web.applicationmap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.applicationmap.link.MatcherGroup;
import com.navercorp.pinpoint.web.applicationmap.link.ServerMatcher;
import com.navercorp.pinpoint.web.view.AgentLifeCycleStateSerializer;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;

/**
 *
 * @author netspider
 * @author emeroad
 * @author HyunGil Jeong
 */
public class ServerInstance {

    private final String hostName;

    private final String name;
    private final ServiceType serviceType;

    private final ServerType serverType;

    private final AgentLifeCycleState status;

    // it is better for something else to inject this.
    // it's difficult to do that since it is new'ed within logic
    private static final MatcherGroup MATCHER_GROUP = new MatcherGroup();

    private ServerMatcher match;

    public ServerInstance(AgentInfo agentInfo) {
        if (agentInfo == null) {
            throw new NullPointerException("agentInfo must not be null");
        }
        this.hostName = agentInfo.getHostName();
        this.name = agentInfo.getAgentId();
        this.serviceType = agentInfo.getServiceType();
        AgentStatus agentStatus = agentInfo.getStatus();
        if (agentStatus != null) {
            this.status = agentStatus.getState();
        } else {
            this.status = AgentLifeCycleState.UNKNOWN;
        }
        this.serverType = ServerType.Physical;
        this.match = MATCHER_GROUP.match(hostName);
    }

    public ServerInstance(String hostName, String physicalName, ServiceType serviceType) {
        if (hostName == null) {
            throw new NullPointerException("hostName must not be null");
        }
        if (physicalName == null) {
            throw new NullPointerException("logicalName must not be null");
        }
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.hostName = hostName;
        this.name = physicalName;
        this.serviceType = serviceType;
        this.status = AgentLifeCycleState.UNKNOWN;
        this.serverType = ServerType.Logical;
        this.match = MATCHER_GROUP.match(hostName);
    }

    @JsonIgnore
    public String getHostName() {
        return hostName;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("serviceType")
    public ServiceType getServiceType() {
        return serviceType;
    }

    @JsonProperty("status")
    @JsonSerialize(using = AgentLifeCycleStateSerializer.class)
    public AgentLifeCycleState getStatus() {
        return status;
    }

    @JsonIgnore
    public ServerType getServerType() {
        return serverType;
    }

    @JsonProperty("linkName")
    public String getLinkName() {
        return match.getLinkName();
    }

    @JsonProperty("linkURL")
    public String getLinkURL() {
        return match.getLink(hostName);
    }

    @JsonProperty("hasInspector")
    public boolean hasInspector() {
        if (serviceType.isWas()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ServerInstance that = (ServerInstance)o;

        if (!name.equals(that.name))
            return false;
        if (serverType != that.serverType)
            return false;
        if (serviceType != that.serviceType)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + serverType.hashCode();
        result = 31 * result + serviceType.hashCode();
        return result;
    }

}
