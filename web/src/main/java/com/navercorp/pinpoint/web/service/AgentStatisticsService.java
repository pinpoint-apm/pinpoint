package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.List;

/**
 * @author Taejin Koo
 */
public interface AgentStatisticsService {

    boolean insertAgentCount(AgentCountStatistics agentCountStatistics);

    List<AgentCountStatistics> selectAgentCount(Range range);

}
