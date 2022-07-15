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
import com.navercorp.pinpoint.web.hyperlink.HyperLink;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.hyperlink.LinkSources;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    private final Set<AgentInfo> agentSet = new HashSet<>();
    private final HyperLinkFactory hyperLinkFactory;

    public ServerBuilder() {
        this.hyperLinkFactory = null;
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

            final ServerInstance serverInstance = new ServerInstance(hostName, instanceName, serviceType, buildHyperLink(hostName));

            serverInstanceList.addServerInstance(serverInstance);
        }
        return serverInstanceList;
    }

    public ServerInstanceList buildPhysicalServer(final Set<AgentInfo> agentSet) {
        final ServerInstanceList serverInstanceList = new ServerInstanceList();
        for (AgentInfo agent : agentSet) {
            final ServerInstance serverInstance = new ServerInstance(agent, buildHyperLink(agent));
            serverInstanceList.addServerInstance(serverInstance);

        }
        return serverInstanceList;
    }

    private List<HyperLink> buildHyperLink(AgentInfo agentInfo) {
        if (hyperLinkFactory != null) {
            return hyperLinkFactory.build(LinkSources.from(agentInfo));
        } else {
            return Collections.emptyList();
        }
    }

    private List<HyperLink> buildHyperLink(String hostName) {
        if (hyperLinkFactory != null) {
            return hyperLinkFactory.build(LinkSources.from(hostName));
        } else {
            return Collections.emptyList();
        }
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
