package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.function.Predicate;

public interface ApplicationAgentListService {

    // add exclamatory mark to indicate that the agent info is not available and prioritize it higher in the natural order comparison.
    String AGENT_INFO_NOT_FOUND_HOSTNAME = "!noAgentInfo";
    Predicate<AgentInfo> ACTUAL_AGENT_INFO_PREDICATE = agentInfo -> !AGENT_INFO_NOT_FOUND_HOSTNAME.equals(agentInfo.getHostName());

    List<AgentAndStatus> allAgentList(String applicationName,
                                      @Nullable ServiceType serviceType,
                                      Range range,
                                      Predicate<AgentInfo> agentInfoPredicate);

    List<AgentAndStatus> activeStatusAgentList(String applicationName,
                                               @Nullable ServiceType serviceType,
                                               TimeWindow timeWindow,
                                               Predicate<AgentInfo> agentInfoPredicate);

    List<AgentAndStatus> activeStatisticsAgentList(String applicationName,
                                                   @Nullable ServiceType serviceType,
                                                   TimeWindow timeWindow,
                                                   Predicate<AgentInfo> agentInfoPredicate);

    List<AgentAndStatus> activeAllAgentList(String applicationName,
                                            @Nullable ServiceType serviceType,
                                            TimeWindow timeWindow,
                                            Predicate<AgentInfo> agentInfoPredicate);
}
