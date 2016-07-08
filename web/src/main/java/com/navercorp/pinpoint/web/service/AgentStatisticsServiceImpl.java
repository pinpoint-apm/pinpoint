package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.AgentStatisticsDao;
import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Taejin Koo
 */
@Service
public class AgentStatisticsServiceImpl implements AgentStatisticsService {

    @Autowired
    AgentStatisticsDao agentStatisticsDao;

    @Override
    public boolean insertAgentCount(AgentCountStatistics agentCountStatistics) {
        return agentStatisticsDao.insertAgentCount(agentCountStatistics);
    }

    @Override
    public List<AgentCountStatistics> selectAgentCount(Range range) {
        return agentStatisticsDao.selectAgentCount(range);
    }

}
