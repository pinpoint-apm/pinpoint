package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.tree.SortByAgentInfo;

public interface ApplicationAgentInfoMapService {

    AgentsMapByHost getAgentsListByApplicationName(String applicationName,
                                                   Range range,
                                                   SortByAgentInfo.Rules sortBy,
                                                   ApplicationAgentListQueryRule applicationAgentListQueryRule,
                                                   AgentInfoFilter agentInfoPredicate);
}
