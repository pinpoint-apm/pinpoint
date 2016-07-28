package com.navercorp.pinpoint.web.batch.job;

import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.ApplicationAgentList;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class AgentCountProcessor implements ItemProcessor<ApplicationAgentList, AgentCountStatistics> {

    @Override
    public AgentCountStatistics process(ApplicationAgentList item) throws Exception {
        if (item == null) {
            return null;
        }

        int agentCount = getAgentCount(item.getApplicationAgentList());
        AgentCountStatistics agentCountStatistics = new AgentCountStatistics(agentCount, DateUtils.timestampToMidNight(System.currentTimeMillis()));
        return agentCountStatistics;
    }

    private int getAgentCount(Map<String, List<AgentInfo>> applicationAgentListMap) {
        int agentCount = 0;
        for (Map.Entry<String, List<AgentInfo>> eachApplicationAgentList : applicationAgentListMap.entrySet()) {
            agentCount += eachApplicationAgentList.getValue().size();
        }

        return agentCount;
    }

}
