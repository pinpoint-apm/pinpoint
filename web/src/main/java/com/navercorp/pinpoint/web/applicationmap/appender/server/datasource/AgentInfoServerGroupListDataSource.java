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

import com.navercorp.pinpoint.common.server.bo.SimpleAgentKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.AgentListV2Service;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author HyunGil Jeong
 */
public class AgentInfoServerGroupListDataSource implements ServerGroupListDataSource {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentInfoService agentInfoService;
    private final AgentListV2Service agentListV2Service;
    private final boolean agentReadV2;

    public AgentInfoServerGroupListDataSource(AgentInfoService agentInfoService, AgentListV2Service agentListV2Service, boolean agentReadV2) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.agentListV2Service = Objects.requireNonNull(agentListV2Service, "agentListV2Service");
        this.agentReadV2 = agentReadV2;
    }

    public ServerGroupList createServerGroupList(Node node, Range range) {
        Objects.requireNonNull(node, "node");
        if (range.getTo() < 0) {
            return ServerGroupList.empty();
        }
        if (agentReadV2) {
            return createServerGroupListV2(node, range);
        }
        return createServerGroupListV1(node, range.getTo());
    }

    private ServerGroupList createServerGroupListV1(Node node, long timestamp) {
        Application application = node.getApplication();
        Set<AgentInfo> agentInfos = new HashSet<>(agentInfoService.getAgentInfoByApplicationName(application.getApplicationName(), timestamp));
        if (CollectionUtils.isEmpty(agentInfos)) {
            logger.info("agentInfo not found. application:{}", application);
            return ServerGroupList.empty();
        }

        logger.debug("unfiltered agentInfos {}", agentInfos);
        agentInfos = filterAgentInfosV1(agentInfos, timestamp, node);
        logger.debug("add agentInfos {} : {}", application, agentInfos);

        Set<AgentAndStatus> agentAndStatusSet = agentInfos.stream()
                .map(AgentAndStatus::new)
                .collect(Collectors.toSet());

        ServerBuilder builder = new ServerBuilder();
        builder.addAgentInfo(agentAndStatusSet);
        return builder.build();
    }

    // TODO Change to list of filters?
    private Set<AgentInfo> filterAgentInfosV1(Set<AgentInfo> agentInfos, long timestamp, Node node) {

        final Map<String, Histogram> agentHistogramMap = getAgentHistogramMap(node);

        Set<AgentInfo> filteredAgentInfos = new HashSet<>();
        List<AgentInfo> agentsToCheckStatus = new ArrayList<>();
        for (AgentInfo agentInfo : agentInfos) {
            String agentId = agentInfo.getAgentId();
            if (agentHistogramMap.containsKey(agentId)) {
                filteredAgentInfos.add(agentInfo);
            } else {
                agentsToCheckStatus.add(agentInfo);
            }
        }
        AgentStatusQuery query = AgentStatusQuery.buildQuery(agentsToCheckStatus, timestamp);

        List<Optional<AgentStatus>> agentStatusList = agentInfoService.getAgentStatus(query);

        int idx = 0;
        for (AgentInfo agentInfo : agentsToCheckStatus) {
            Optional<AgentStatus> agentStatus = agentStatusList.get(idx++);
            if (agentStatus.isPresent()) {
                if (agentStatus.get().getState() == AgentLifeCycleState.RUNNING) {
                    filteredAgentInfos.add(agentInfo);
                }
            }
        }

        return filteredAgentInfos;
    }

    private ServerGroupList createServerGroupListV2(Node node, Range range) {
        Application application = node.getApplication();
        List<AgentIdEntry> agentIdEntries = agentListV2Service.getActiveAgentList(
                ServiceUid.DEFAULT, application.getApplicationName(), application.getServiceType(), range);

        if (CollectionUtils.isEmpty(agentIdEntries)) {
            logger.info("agentIdEntry not found. application:{}", application);
            return ServerGroupList.empty();
        }

        List<SimpleAgentKey> simpleAgentKeys = agentIdEntries.stream()
                .map(entry -> new SimpleAgentKey(entry.getAgentId(), entry.getAgentStartTime()))
                .toList();
        List<AgentInfo> agentInfoList = agentInfoService.getAgentInfos(simpleAgentKeys);

        Set<AgentAndStatus> agentAndStatusSet = new HashSet<>();
        for (int i = 0; i < agentIdEntries.size(); i++) {
            AgentInfo agentInfo = agentInfoList.get(i);
            if (agentInfo == null) {
                continue;
            }
            AgentIdEntry entry = agentIdEntries.get(i);
            AgentStatus agentStatus = new AgentStatus(entry.getAgentId(), entry.getCurrentState(), entry.getCurrentStateTimestamp());
            agentAndStatusSet.add(new AgentAndStatus(agentInfo, agentStatus));
        }
        logger.debug("add agentAndStatusSet {} : {}", application, agentAndStatusSet);

        ServerBuilder builder = new ServerBuilder();
        builder.addAgentInfo(agentAndStatusSet);
        return builder.build();
    }

    private Map<String, Histogram> getAgentHistogramMap(Node node) {
        NodeHistogram nodeHistogram = node.getNodeHistogram();
        if (nodeHistogram != null) {
            return nodeHistogram.getAgentHistogramMap();
        }
        return Collections.emptyMap();
    }
}
