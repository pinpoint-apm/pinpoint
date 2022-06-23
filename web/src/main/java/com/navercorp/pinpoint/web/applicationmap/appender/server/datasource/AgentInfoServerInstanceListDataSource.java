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

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.AgentInfoFilterChain;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import com.navercorp.pinpoint.web.vo.AgentStatusQuery;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.DefaultAgentInfoFilter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public class AgentInfoServerInstanceListDataSource implements ServerInstanceListDataSource {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentInfoService agentInfoService;

    public AgentInfoServerInstanceListDataSource(AgentInfoService agentInfoService) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
    }

    @Override
    public ServerInstanceList createServerInstanceList(Node node, Range range) {
        AgentInfoFilter runningFilter = new AgentInfoFilterChain(
                new DefaultAgentInfoFilter(range.getFrom())
        );
        return createServerInstanceList(node, range, runningFilter);
    }

    public ServerInstanceList createServerInstanceList(Node node, Range range, AgentInfoFilter filter) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(range, "range");
        Instant timestamp = range.getToInstant();
        if (timestamp.toEpochMilli() < 0) {
            return new ServerInstanceList();
        }

        Application application = node.getApplication();
        Set<AgentInfo> agentInfos = agentInfoService.getAgentsByApplicationNameWithoutStatus(application.getName(), timestamp.toEpochMilli());
        if (CollectionUtils.isEmpty(agentInfos)) {
            logger.warn("agentInfo not found. application:{}", application);
            return new ServerInstanceList();
        }

        logger.debug("unfiltered agentInfos {}", agentInfos);
        agentInfos = filterAgentInfos(agentInfos, range, filter);
        logger.debug("add agentInfos {} : {}", application, agentInfos);

        ServerBuilder builder = new ServerBuilder();
        builder.addAgentInfo(agentInfos);
        return builder.build();
    }

    // TODO Change to list of filters?
    private Set<AgentInfo> filterAgentInfos(Set<AgentInfo> agentInfos, Range range, AgentInfoFilter filter) {
        Set<AgentInfo> filteredAgentInfos = new HashSet<>();
        List<AgentInfo> agentsToCheckStatus = new ArrayList<>(agentInfos);
        AgentStatusQuery query = AgentStatusQuery.buildQuery(agentInfos, range.getToInstant());

        List<Optional<AgentStatus>> agentStatusList = agentInfoService.getAgentStatus(query);

        int idx = 0;
        for (AgentInfo agentInfo : agentsToCheckStatus) {
            Optional<AgentStatus> agentStatus = agentStatusList.get(idx++);
            if (agentStatus.isPresent()) {
                agentInfo.setStatus(agentStatus.get());
                if (filter.filter(agentInfo) == AgentInfoFilter.ACCEPT) {
                    filteredAgentInfos.add(agentInfo);
                }
            }
        }

        return filteredAgentInfos;
    }
}
