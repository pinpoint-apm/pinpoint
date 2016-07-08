package com.navercorp.pinpoint.web.batch.job;

import com.navercorp.pinpoint.web.dao.AgentStatisticsDao;
import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentCountWriter implements ItemWriter<AgentCountStatistics> {

    @Autowired
    AgentStatisticsDao agentStatisticsDao;

    @Override
    public void write(List<? extends AgentCountStatistics> items) throws Exception {
        if (items.size() == 1) {
            AgentCountStatistics agentCountStatistics = items.get(0);
            if (agentCountStatistics == null || agentCountStatistics.getAgentCount() < 0) {
                throw new JobExecutionException("Bad parameter");
            }
            boolean success = agentStatisticsDao.insertAgentCount(agentCountStatistics);
            if (!success) {
                throw new JobExecutionException("insert AgentCount failed.");
            }
        } else {
            throw new JobExecutionException("Bad parameter");
        }
    }

}
