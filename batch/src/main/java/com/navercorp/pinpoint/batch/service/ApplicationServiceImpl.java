/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.service;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilterChain;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.tree.SortByAgentInfo.Rules;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final Range INF_RANGE = Range.between(0L, Long.MAX_VALUE);

    private final ApplicationIndexDao applicationIndexDao;
    private final ApplicationTraceIndexDao applicationTraceIndexDao;
    private final AgentInfoService agentInfoService;

    public ApplicationServiceImpl(
            ApplicationIndexDao applicationIndexDao,
            ApplicationTraceIndexDao applicationTraceIndexDao,
            AgentInfoService agentInfoService
    ) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
    }

    @Override
    public List<String> getApplicationNames() {
        return this.applicationIndexDao.selectAllApplicationNames()
                .stream()
                .map(Application::getName)
                .toList();
    }

    @Override
    public boolean isApplicationEmpty(String applicationName, Duration duration) {
        long now = System.currentTimeMillis();
        Range range = Range.between(now - duration.toMillis(), now);
        return !isAliveAgentExist(applicationName, range) && !isTraceExist(applicationName, range);
    }

    private boolean isAliveAgentExist(String applicationName, Range range) {
        AgentsMapByHost agents = this.agentInfoService.getAgentsListByApplicationName(
                new AgentStatusFilterChain(AgentStatusFilter::filterRunning),
                applicationName,
                range,
                Rules.AGENT_NAME_ASC
        );
        return !agents.getAgentsListsList().isEmpty();
    }

    private boolean isTraceExist(String applicationName, Range range) {
        LimitedScanResult<List<TransactionId>> result = this.applicationTraceIndexDao.scanTraceIndex(
                applicationName, range, 1, false);
        return !result.getScanData().isEmpty();
    }

    @Override
    public void removeApplication(String applicationName) {
        this.applicationIndexDao.deleteApplicationName(applicationName);
    }
}
