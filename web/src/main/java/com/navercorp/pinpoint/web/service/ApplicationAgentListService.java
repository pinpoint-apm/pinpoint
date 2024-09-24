package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;

import java.util.List;

public interface ApplicationAgentListService {

    List<AgentAndStatus> allAgentList(Application application, Range range, AgentInfoFilter agentInfoFilter);

    List<AgentAndStatus> activeStatusAgentList(Application application, Range range, AgentInfoFilter agentInfoFilter);

    List<AgentAndStatus> activeResponseAgentList(Application application, Range range, AgentInfoFilter agentInfoFilter);
}
