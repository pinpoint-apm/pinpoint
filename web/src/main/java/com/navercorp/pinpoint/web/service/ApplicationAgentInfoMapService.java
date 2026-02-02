package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.tree.SortByAgentInfo;

import java.util.List;

public interface ApplicationAgentInfoMapService {

    AgentsMapByHost getAgentsListByApplicationName(
            Application application,
            TimeWindow timeWindow,
            SortByAgentInfo.Rules sortBy,
            ApplicationAgentListQueryRule applicationAgentListQueryRule,
            AgentInfoFilter agentInfoPredicate
    );

}
