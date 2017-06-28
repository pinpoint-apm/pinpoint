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

package com.navercorp.pinpoint.web.applicationmap.appender.server.datasource;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import com.navercorp.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public class AgentInfoServerInstanceListDataSource implements ServerInstanceListDataSource {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentInfoService agentInfoService;

    public AgentInfoServerInstanceListDataSource(AgentInfoService agentInfoService) {
        if (agentInfoService == null) {
            throw new NullPointerException("agentInfoService must not be null");
        }
        this.agentInfoService = agentInfoService;
    }

    public ServerInstanceList createServerInstanceList(Node node, long timestamp) {
        if (node == null) {
            throw new NullPointerException("node must not be null");
        }
        if (timestamp < 0) {
            return new ServerInstanceList();
        }

        Application application = node.getApplication();
        Set<AgentInfo> agentInfos = agentInfoService.getAgentsByApplicationNameWithoutStatus(application.getName(), timestamp);
        if (CollectionUtils.isEmpty(agentInfos)) {
            logger.warn("agentInfo not found. application:{}", application);
            return new ServerInstanceList();
        }

        logger.debug("unfiltered agentInfos {}", agentInfos);
        agentInfos = filterAgentInfos(agentInfos, timestamp, node);
        logger.debug("add agentInfos {} : {}", application, agentInfos);

        ServerBuilder builder = new ServerBuilder();
        builder.addAgentInfo(agentInfos);
        return builder.build();
    }

    // TODO Change to list of filters?
    private Set<AgentInfo> filterAgentInfos(Set<AgentInfo> agentInfos, long timestamp, Node node) {
        Set<AgentInfo> filteredAgentInfos = new HashSet<>();

        Map<String, Histogram> agentHistogramMap = Collections.emptyMap();
        NodeHistogram nodeHistogram = node.getNodeHistogram();
        if (nodeHistogram != null) {
            agentHistogramMap = nodeHistogram.getAgentHistogramMap();
        }

        List<AgentInfo> agentsToCheckStatus = new ArrayList<>();
        for (AgentInfo agentInfo : agentInfos) {
            String agentId = agentInfo.getAgentId();
            if (agentHistogramMap.containsKey(agentId)) {
                filteredAgentInfos.add(agentInfo);
            } else {
                agentsToCheckStatus.add(agentInfo);
            }
        }

        // TODO this could be called asynchronously using hbase client 2.x
        agentInfoService.populateAgentStatuses(agentsToCheckStatus, timestamp);
        for (AgentInfo agentInfo : agentsToCheckStatus) {
            AgentStatus agentStatus = agentInfo.getStatus();
            if (agentStatus != null) {
                if (agentStatus.getState() == AgentLifeCycleState.RUNNING) {
                    filteredAgentInfos.add(agentInfo);
                }
            }
        }

        return filteredAgentInfos;
    }
}
