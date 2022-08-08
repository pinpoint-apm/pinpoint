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
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class ServerBuilder {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentHistogramList agentHistogramList = new AgentHistogramList();
    private final Set<AgentAndStatus> agentSet = new HashSet<>();
    private final HyperLinkFactory hyperLinkFactory;

    public ServerBuilder() {
        this.hyperLinkFactory = HyperLinkFactory.empty();
    }

    public ServerBuilder(HyperLinkFactory hyperLinkFactory) {
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }

    public void addCallHistogramList(AgentHistogramList agentHistogramList) {
        if (agentHistogramList == null) {
            return;
        }
        this.agentHistogramList.addAgentHistogram(agentHistogramList);
    }

    public void addAgentInfo(Set<AgentAndStatus> agentInfo) {
        if (agentInfo == null) {
            return;
        }
        this.agentSet.addAll(agentInfo);
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
    public ServerGroupList buildLogicalServer(final AgentHistogramList hostHistogram) {
        ServerGroupList.Builder builder = ServerGroupList.newBuilder(hyperLinkFactory);

        for (AgentHistogram agentHistogram : hostHistogram.getAgentHistogramList()) {
            final String instanceName = agentHistogram.getId();
            final String hostName = getHostName(agentHistogram.getId());
            final ServiceType serviceType = agentHistogram.getServiceType();

            final ServerInstance serverInstance = new ServerInstance(hostName, instanceName, serviceType);

            builder.addServerInstance(serverInstance);
        }
        return builder.build();
    }

    public ServerGroupList buildPhysicalServer(final Set<AgentAndStatus> agentSet) {
        final ServerGroupList.Builder builder = ServerGroupList.newBuilder(hyperLinkFactory);
        for (AgentAndStatus agentAndStatus : agentSet) {
            final ServerInstance serverInstance = new ServerInstance(agentAndStatus.getAgentInfo(), agentAndStatus.getStatus());
            builder.addServerInstance(serverInstance);
        }
        return builder.build();
    }

    public ServerGroupList build() {
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
