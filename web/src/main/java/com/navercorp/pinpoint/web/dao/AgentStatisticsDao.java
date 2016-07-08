package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.List;

/**
 * @author Taejin Koo
 */
public interface AgentStatisticsDao {

    boolean insertAgentCount(AgentCountStatistics agentCountStatistics);

    List<AgentCountStatistics> selectAgentCount(Range range);

}
