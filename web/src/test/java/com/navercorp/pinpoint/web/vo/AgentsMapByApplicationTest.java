package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.tree.InstancesList;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AgentsMapByApplicationTest {

    @Test
    public void groupByApplicationName() {
        AgentAndStatus app1Agent1 = createAgentInfo("APP_1", "app1-agent1", "Host11", true);
        AgentAndStatus app1Agent2 = createAgentInfo("APP_1", "app1-agent2", "Host12", false);
        AgentAndStatus app2Agent1 = createAgentInfo("APP_2", "app2-agent1", "Host21", false);
        AgentAndStatus app2Agent2 = createAgentInfo("APP_2", "app2-agent2", "Host22", true);
        List<AgentAndStatus> agentAndStatusList = shuffleAgentInfos(app1Agent1, app1Agent2, app2Agent1, app2Agent2);

        AgentsMapByApplication agentsMapByApplication = AgentsMapByApplication.newAgentsMapByApplication(AgentInfoFilter::accept, agentAndStatusList);
        List<InstancesList<AgentAndStatus>> instancesLists = agentsMapByApplication.getAgentsListsList();

        Assertions.assertEquals(2, instancesLists.size());

        InstancesList<AgentAndStatus> app1InstancesList = instancesLists.get(0);
        Assertions.assertEquals("APP_1", app1InstancesList.getGroupName());
        List<AgentAndStatus> app1AgentInfos = app1InstancesList.getInstancesList();
        Assertions.assertEquals(2, app1AgentInfos.size());
        Assertions.assertEquals(app1Agent1.getAgentInfo(), app1AgentInfos.get(0).getAgentInfo());
        Assertions.assertEquals(app1Agent2.getAgentInfo(), app1AgentInfos.get(1).getAgentInfo());

        InstancesList<AgentAndStatus> app2InstancesList = instancesLists.get(1);
        Assertions.assertEquals("APP_2", app2InstancesList.getGroupName());
        List<AgentAndStatus> app2AgentInfos = app2InstancesList.getInstancesList();
        Assertions.assertEquals(2, app2AgentInfos.size());
        Assertions.assertEquals(app2Agent1.getAgentInfo(), app2AgentInfos.get(0).getAgentInfo());
        Assertions.assertEquals(app2Agent2.getAgentInfo(), app2AgentInfos.get(1).getAgentInfo());
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
        agentInfo.setAgentId(agentId);
        agentInfo.setHostName(hostname);
        agentInfo.setContainer(container);
        agentInfo.setStartTimestamp(startTimestamp);
        return new AgentAndStatus(agentInfo);
    }
}
