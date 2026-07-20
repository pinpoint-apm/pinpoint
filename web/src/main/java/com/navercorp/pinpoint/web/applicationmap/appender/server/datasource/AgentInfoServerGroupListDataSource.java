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
import com.navercorp.pinpoint.common.timeseries.time.Range;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public class AgentInfoServerGroupListDataSource implements ServerGroupListDataSource {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentInfoService agentInfoService;
    private final AgentListV2Service agentListV2Service;

    public AgentInfoServerGroupListDataSource(AgentInfoService agentInfoService, AgentListV2Service agentListV2Service) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.agentListV2Service = Objects.requireNonNull(agentListV2Service, "agentListV2Service");
    }

    public ServerGroupList createServerGroupList(Node node, Range range) {
        Objects.requireNonNull(node, "node");
        if (range.getTo() < 0) {
            return ServerGroupList.empty();
        }
        return createServerGroupListV2(node, range);
    }

    private ServerGroupList createServerGroupListV2(Node node, Range range) {
        Application application = node.getApplication();
        List<AgentIdEntry> agentIdEntries = agentListV2Service.getActiveAgentList(
                application.getService(), application.getApplicationName(), application.getServiceType(), range);

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
}
