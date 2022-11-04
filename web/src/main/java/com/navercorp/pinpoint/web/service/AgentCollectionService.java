package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByApplication;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.tree.ApplicationAgentHostList;
import com.navercorp.pinpoint.web.vo.tree.SortByAgentInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author intr3p1d
 */
public interface AgentCollectionService {
    int NO_DURATION = -1;

    AgentsMapByApplication getAllAgentsList(AgentInfoFilter filter, long timestamp);

    AgentsMapByHost getAgentsListByApplicationName(AgentInfoFilter filter, String applicationName, long timestamp);

    AgentsMapByHost getAgentsListByApplicationName(AgentInfoFilter filter, String applicationName, long timestamp, SortByAgentInfo.Rules sortBy);

    ApplicationAgentHostList getApplicationAgentHostList(int offset, int limit, int durationDays);

    Set<AgentAndStatus> getAgentsByApplicationName(String applicationName, long timestamp);

    Set<AgentInfo> getAgentsByApplicationNameWithoutStatus(String applicationName, long timestamp);

    Set<AgentAndStatus> getRecentAgentsByApplicationName(String applicationName, long timestamp, long timeDiff);

    List<Optional<AgentStatus>> getAgentStatusList(AgentStatusQuery query);
}
