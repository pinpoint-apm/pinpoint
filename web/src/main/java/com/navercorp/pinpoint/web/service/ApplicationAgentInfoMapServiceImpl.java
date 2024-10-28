package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.tree.SortByAgentInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ApplicationAgentInfoMapServiceImpl implements ApplicationAgentInfoMapService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationAgentListService applicationAgentListService;
    private final HyperLinkFactory hyperLinkFactory;

    public ApplicationAgentInfoMapServiceImpl(ApplicationAgentListService applicationAgentListService, HyperLinkFactory hyperLinkFactory) {
        this.applicationAgentListService = Objects.requireNonNull(applicationAgentListService, "activeAgentInfoListService");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }

    @Override
    public AgentsMapByHost getAgentsListByApplicationName(Application application,
                                                          Range range,
                                                          SortByAgentInfo.Rules sortBy,
                                                          ApplicationAgentListQueryRule applicationAgentListQueryRule,
                                                          AgentInfoFilter agentInfoPredicate) {
        Objects.requireNonNull(agentInfoPredicate, "agentInfoPredicate");
        Objects.requireNonNull(application, "applicationName");

        List<AgentAndStatus> agentInfoAndStatuses = getActivateAgentInfoList(application, range, applicationAgentListQueryRule, agentInfoPredicate);
        if (agentInfoAndStatuses.isEmpty()) {
            logger.warn("agent list is empty for application:{}", application);
        }

        AgentsMapByHost agentsMapByHost = AgentsMapByHost.newAgentsMapByHost(
                agentAndStatus -> true,
                SortByAgentInfo.comparing(AgentStatusAndLink::getAgentInfo, sortBy.getRule()),
                hyperLinkFactory,
                agentInfoAndStatuses
        );

        final int totalAgentCount = agentsMapByHost.size();
        if (logger.isInfoEnabled()) {
            logger.info("getAgentsMapByHostname size:{}", totalAgentCount);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("getAgentsMapByHostname size:{} data:{}", totalAgentCount, agentsMapByHost);
        }
        return agentsMapByHost;
    }

    private List<AgentAndStatus> getActivateAgentInfoList(Application application,
                                                          Range range,
                                                          ApplicationAgentListQueryRule applicationAgentListQueryRule,
                                                          AgentInfoFilter agentInfoFilter) {
        final String applicationName = application.getName();
        final ServiceType serviceType = application.getServiceType();
        return switch (applicationAgentListQueryRule) {
            case ACTIVE_STATUS -> applicationAgentListService.activeStatusAgentList(applicationName, serviceType, range, agentInfoFilter);
            case ACTIVE_STATISTICS -> applicationAgentListService.activeStatisticsAgentList(applicationName, serviceType, range, agentInfoFilter);
            case ACTIVE_ALL -> applicationAgentListService.activeAllAgentList(applicationName, serviceType, range, agentInfoFilter);
            default -> applicationAgentListService.allAgentList(applicationName, serviceType, range, agentInfoFilter);
        };
    }
}
