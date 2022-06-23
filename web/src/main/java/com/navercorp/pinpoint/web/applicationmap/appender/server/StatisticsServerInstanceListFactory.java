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

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.Application;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author jaehong.kim
 */
public class StatisticsServerInstanceListFactory implements ServerInstanceListFactory {
    private final ServerInstanceListDataSource serverInstanceListDataSource;

    public StatisticsServerInstanceListFactory(ServerInstanceListDataSource serverInstanceListDataSource) {
        this.serverInstanceListDataSource = serverInstanceListDataSource;
    }

    @Override
    public ServerInstanceList createWasNodeInstanceList(Node wasNode, Range range) {
        ServerInstanceList serverInstanceList = createWasNodeInstanceListFromHistogram(wasNode, range.getToInstant());
        if (serverInstanceList.getServerInstanceList().isEmpty()) {
            // When there is no transaction information, agentInfo information is used.
            serverInstanceList = createWasNodeInstanceListFromAgentInfo(wasNode, range);
        }
        return serverInstanceList;
    }

    ServerInstanceList createWasNodeInstanceListFromHistogram(Node wasNode, Instant timestamp) {
        Objects.requireNonNull(wasNode, "wasNode");
        if (timestamp.toEpochMilli() < 0) {
            return new ServerInstanceList();
        }

        final ServerBuilder builder = new ServerBuilder();
        final Set<AgentInfo> agentInfoSet = new HashSet<>();
        final NodeHistogram nodeHistogram = wasNode.getNodeHistogram();
        if (nodeHistogram != null && nodeHistogram.getAgentHistogramMap() != null) {
            for (String agentId : nodeHistogram.getAgentHistogramMap().keySet()) {
                AgentInfo agentInfo = new AgentInfo();
                agentInfo.setAgentId(agentId);
                agentInfo.setHostName(agentId);
                agentInfo.setIp("");
                agentInfo.setServiceTypeCode(wasNode.getServiceType().getCode());
                agentInfoSet.add(agentInfo);
            }
        }
        builder.addAgentInfo(agentInfoSet);
        return builder.build();
    }

    ServerInstanceList createWasNodeInstanceListFromAgentInfo(Node wasNode, Range range) {
        return serverInstanceListDataSource.createServerInstanceList(wasNode, range);
    }

    @Override
    public ServerInstanceList createTerminalNodeInstanceList(Node terminalNode, LinkDataDuplexMap linkDataDuplexMap) {
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
    public ServerInstanceList createQueueNodeInstanceList(Node queueNode, LinkDataDuplexMap linkDataDuplexMap) {
        return createEmptyNodeInstanceList();
    }

    @Override
    public ServerInstanceList createUserNodeInstanceList() {
        return createEmptyNodeInstanceList();
    }

    @Override
    public ServerInstanceList createEmptyNodeInstanceList() {
        return new ServerInstanceList();
    }
}
