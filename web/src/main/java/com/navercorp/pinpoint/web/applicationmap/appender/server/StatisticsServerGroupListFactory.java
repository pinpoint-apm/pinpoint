/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.appender.server;

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerGroupListDataSource;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author jaehong.kim
 */
public class StatisticsServerGroupListFactory implements ServerGroupListFactory {
    private final ServerGroupListDataSource serverGroupListDataSource;

    public StatisticsServerGroupListFactory(ServerGroupListDataSource serverGroupListDataSource) {
        this.serverGroupListDataSource = serverGroupListDataSource;
    }

    @Override
    public ServerGroupList createWasNodeInstanceList(Node wasNode, Instant timestamp) {
        ServerGroupList serverGroupList = createWasNodeInstanceListFromHistogram(wasNode, timestamp);
        if (serverGroupList.getServerGroupList().isEmpty()) {
            // When there is no transaction information, agentInfo information is used.
            serverGroupList = createWasNodeInstanceListFromAgentInfo(wasNode, timestamp);
        }
        return serverGroupList;
    }

    ServerGroupList createWasNodeInstanceListFromHistogram(Node wasNode, Instant timestamp) {
        Objects.requireNonNull(wasNode, "wasNode");
        if (timestamp.toEpochMilli() < 0) {
            return ServerGroupList.empty();
        }

        final ServerBuilder builder = new ServerBuilder();
        final Set<AgentAndStatus> agentInfoSet = new HashSet<>();
        final NodeHistogram nodeHistogram = wasNode.getNodeHistogram();
        if (nodeHistogram != null && nodeHistogram.getAgentHistogramMap() != null) {
            for (String agentId : nodeHistogram.getAgentHistogramMap().keySet()) {
                AgentInfo agentInfo = new AgentInfo();
                agentInfo.setAgentId(AgentId.of(agentId));
                agentInfo.setHostName(agentId);
                agentInfo.setIp("");
                agentInfo.setAgentName("");
                agentInfo.setServiceType(wasNode.getServiceType());
                agentInfoSet.add(new AgentAndStatus(agentInfo));
            }
        }
        builder.addAgentInfo(agentInfoSet);
        return builder.build();
    }

    ServerGroupList createWasNodeInstanceListFromAgentInfo(Node wasNode, Instant timestamp) {
        return serverGroupListDataSource.createServerGroupList(wasNode, timestamp);
    }

    @Override
    public ServerGroupList createTerminalNodeInstanceList(Node terminalNode, LinkDataDuplexMap linkDataDuplexMap) {
        // extract information about the terminal node
        ServerBuilder builder = new ServerBuilder();
        for (LinkData linkData : linkDataDuplexMap.getSourceLinkDataList()) {
            Application toApplication = linkData.getToApplication();
            if (terminalNode.getApplication().equals(toApplication)) {
                builder.addCallHistogramList(linkData.getTargetList());
            }
        }
        return builder.build();
    }

    @Override
    public ServerGroupList createQueueNodeInstanceList(Node queueNode, LinkDataDuplexMap linkDataDuplexMap) {
        return createEmptyNodeInstanceList();
    }

    @Override
    public ServerGroupList createUserNodeInstanceList() {
        return createEmptyNodeInstanceList();
    }

    @Override
    public ServerGroupList createEmptyNodeInstanceList() {
        return ServerGroupList.empty();
    }
}
