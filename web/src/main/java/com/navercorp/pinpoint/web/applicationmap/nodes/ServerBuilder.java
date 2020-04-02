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

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.vo.AgentInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class ServerBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentHistogramList agentHistogramList;
    private final Set<AgentInfo> agentSet;

    public ServerBuilder() {
        this.agentHistogramList = new AgentHistogramList();
        this.agentSet = new HashSet<>();
    }

    public void addCallHistogramList(AgentHistogramList agentHistogramList) {
        if (agentHistogramList == null) {
            return;
        }
        this.agentHistogramList.addAgentHistogram(agentHistogramList);
    }

    public void addAgentInfo(Set<AgentInfo> agentInfo) {
        if (agentInfo == null) {
            return;
        }
        this.agentSet.addAll(agentInfo);
    }

    public void addServerInstance(ServerBuilder copy) {
        Objects.requireNonNull(copy, "copy");

        addCallHistogramList(copy.agentHistogramList);
        addAgentInfo(copy.agentSet);
    }

    private String getHostName(String instanceName) {
        final int pos = instanceName.indexOf(':');
        if (pos > 0) {
            return instanceName.substring(0, pos);
        } else {
            return instanceName;
        }
    }

    /**
     * filled with application information of physical server and service instance
     *
     * @param hostHistogram
     */
    public ServerInstanceList buildLogicalServer(final AgentHistogramList hostHistogram) {
        ServerInstanceList serverInstanceList = new ServerInstanceList();
        for (AgentHistogram agentHistogram : hostHistogram.getAgentHistogramList()) {
            final String instanceName = agentHistogram.getId();
            final String hostName = getHostName(agentHistogram.getId());
            final ServiceType serviceType = agentHistogram.getServiceType();

            final ServerInstance serverInstance = new ServerInstance(hostName, instanceName, serviceType.getCode());
            serverInstanceList.addServerInstance(serverInstance);
        }
        return serverInstanceList;
    }

    public ServerInstanceList buildPhysicalServer(final Set<AgentInfo> agentSet) {
        final ServerInstanceList serverInstanceList = new ServerInstanceList();
        for (AgentInfo agent : agentSet) {
            final ServerInstance serverInstance = new ServerInstance(agent);
            serverInstanceList.addServerInstance(serverInstance);

        }
        return serverInstanceList;
    }

    public ServerInstanceList build() {
        if (!agentSet.isEmpty()) {
            // if agent name exists (physical server exists)
            this.logger.debug("buildPhysicalServer:{}", agentSet);
            return buildPhysicalServer(agentSet);
        } else {
            // otherwise, create logical name
            this.logger.debug("buildLogicalServer");
            return buildLogicalServer(agentHistogramList);
        }
    }

}
