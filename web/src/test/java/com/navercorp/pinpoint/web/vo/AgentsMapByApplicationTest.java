package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilters;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByApplication;
import com.navercorp.pinpoint.web.vo.tree.InstancesList;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AgentsMapByApplicationTest {

    @Test
    public void groupByApplicationName() {
        AgentAndStatus app1Agent1 = createAgentInfo("APP_1", "app1-agent1", "Host11", true);
        AgentAndStatus app1Agent2 = createAgentInfo("APP_1", "app1-agent2", "Host12", false);
        AgentAndStatus app2Agent1 = createAgentInfo("APP_2", "app2-agent1", "Host21", false);
        AgentAndStatus app2Agent2 = createAgentInfo("APP_2", "app2-agent2", "Host22", true);
        List<AgentAndStatus> agentAndStatusList = shuffleAgentInfos(app1Agent1, app1Agent2, app2Agent1, app2Agent2);

        AgentsMapByApplication<AgentAndStatus> agentsMapByApplication = AgentsMapByApplication.newAgentAndStatusMap(AgentStatusFilters.acceptAll(), agentAndStatusList);
        List<InstancesList<AgentAndStatus>> instancesLists = agentsMapByApplication.getAgentsListsList();

        assertThat(instancesLists).hasSize(2);

        InstancesList<AgentAndStatus> app1InstancesList = instancesLists.get(0);
        assertEquals("APP_1", app1InstancesList.getGroupName());

        List<AgentAndStatus> app1AgentInfos = app1InstancesList.getInstancesList();
        assertThat(app1AgentInfos)
                .map(AgentAndStatus::getAgentInfo)
                .containsExactly(app1Agent1.getAgentInfo(), app1Agent2.getAgentInfo());

        InstancesList<AgentAndStatus> app2InstancesList = instancesLists.get(1);
        assertEquals("APP_2", app2InstancesList.getGroupName());

        List<AgentAndStatus> app2AgentInfos = app2InstancesList.getInstancesList();
        assertThat(app2AgentInfos)
                .map(AgentAndStatus::getAgentInfo)
                .containsExactly(app2Agent1.getAgentInfo(), app2Agent2.getAgentInfo());
    }

    private static List<AgentAndStatus> shuffleAgentInfos(AgentAndStatus... agentInfos) {
        List<AgentAndStatus> agentInfoList = Arrays.asList(agentInfos);
        Collections.shuffle(agentInfoList);
        return agentInfoList;
    }

    private static AgentAndStatus createAgentInfo(String applicationName, String agentId, String hostname, boolean container) {
        return createAgentInfo(applicationName, agentId, hostname, container, System.currentTimeMillis());
    }

    private static AgentAndStatus createAgentInfo(String applicationName, String agentId, String hostname, boolean container, long startTimestamp) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setApplicationName(applicationName);
        agentInfo.setAgentId(AgentId.of(agentId));
        agentInfo.setHostName(hostname);
        agentInfo.setContainer(container);
        agentInfo.setStartTimestamp(startTimestamp);
        return new AgentAndStatus(agentInfo);
    }
}
