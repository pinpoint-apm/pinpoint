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

import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.link.MatcherGroup;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 * @author minwoo.jung
 */
public class ServerBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentHistogramList agentHistogramList;
    private final Set<AgentInfoBo> agentSet;
    private final MatcherGroup matcherGroup;

    public ServerBuilder() {
        // TODO FIX
        this(null);
    }

    public ServerBuilder(MatcherGroup matcherGroup) {
        this.agentHistogramList = new AgentHistogramList();
        this.agentSet = new HashSet<AgentInfoBo>();
        // TODO avoid null
        this.matcherGroup = matcherGroup;
    }

    public void addCallHistogramList(AgentHistogramList agentHistogramList) {
        if (agentHistogramList == null) {
            return;
        }
        this.agentHistogramList.addAgentHistogram(agentHistogramList);
    }

    public void addAgentInfo(Set<AgentInfoBo> agentInfoBo) {
        if (agentInfoBo == null) {
            return;
        }
        this.agentSet.addAll(agentInfoBo);
    }

    public void addServerInstance(ServerBuilder copy) {
        if (copy == null) {
            throw new NullPointerException("copy must not be null");
        }
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

            final ServerInstance serverInstance = new ServerInstance(hostName, instanceName, serviceType);
            serverInstanceList.addServerInstance(serverInstance);
        }
        return serverInstanceList;
    }

    public ServerInstanceList buildPhysicalServer(final Set<AgentInfoBo> agentSet) {
        final ServerInstanceList serverInstanceList = new ServerInstanceList(matcherGroup);
        for (AgentInfoBo agent : agentSet) {
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
