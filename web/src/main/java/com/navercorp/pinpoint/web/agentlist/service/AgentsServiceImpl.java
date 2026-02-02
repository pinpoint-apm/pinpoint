/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.web.agentlist.service;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.service.ApplicationAgentListQueryRule;
import com.navercorp.pinpoint.web.service.ApplicationAgentListService;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import com.navercorp.pinpoint.web.agentlist.AgentsFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class AgentsServiceImpl implements AgentsService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationAgentListService applicationAgentListService;
    private final HyperLinkFactory hyperLinkFactory;

    public AgentsServiceImpl(ApplicationAgentListService applicationAgentListService, HyperLinkFactory hyperLinkFactory) {
        this.applicationAgentListService = Objects.requireNonNull(applicationAgentListService, "activeAgentInfoListService");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }


    @Override
    public List<AgentStatusAndLink> getAgentsByApplicationName(
            Application application,
            TimeWindow timeWindow,
            ApplicationAgentListQueryRule applicationAgentListQueryRule,
            AgentInfoFilter agentInfoPredicate
    ) {
        Objects.requireNonNull(application, "application");
        Objects.requireNonNull(timeWindow, "timeWindow");
        Objects.requireNonNull(applicationAgentListQueryRule, "applicationAgentListQueryRule");
        Objects.requireNonNull(agentInfoPredicate, "agentInfoPredicate");

        List<AgentAndStatus> agentInfoAndStatuses = getActivateAgentInfoList(application, timeWindow, applicationAgentListQueryRule, agentInfoPredicate);
        if (agentInfoAndStatuses.isEmpty()) {
            logger.warn("agent list is empty for application:{}", application);
        }

        return AgentsFactory.addLinks(
                hyperLinkFactory,
                agentInfoAndStatuses
        );
    }

    private List<AgentAndStatus> getActivateAgentInfoList(
            Application application,
            TimeWindow timeWindow,
            ApplicationAgentListQueryRule applicationAgentListQueryRule,
            AgentInfoFilter agentInfoFilter
    ) {
        final String applicationName = application.getName();
        final ServiceType serviceType = application.getServiceType();
        final Range windowRange = timeWindow.getWindowRange();
        return switch (applicationAgentListQueryRule) {
            case ACTIVE_STATUS ->
                    applicationAgentListService.activeStatusAgentList(applicationName, serviceType, timeWindow, agentInfoFilter);
            case ACTIVE_STATISTICS ->
                    applicationAgentListService.activeStatisticsAgentList(applicationName, serviceType, timeWindow, agentInfoFilter);
            case ACTIVE_ALL ->
                    applicationAgentListService.activeAllAgentList(applicationName, serviceType, timeWindow, agentInfoFilter);
            default ->
                    applicationAgentListService.allAgentList(applicationName, serviceType, windowRange, agentInfoFilter);
        };
    }
}
