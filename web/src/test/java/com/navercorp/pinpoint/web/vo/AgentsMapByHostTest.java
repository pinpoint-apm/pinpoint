package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.tree.InstancesList;
import com.navercorp.pinpoint.web.vo.tree.SortByAgentInfo;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AgentsMapByHostTest {

    private final HyperLinkFactory hyperLinkFactory = HyperLinkFactory.empty();

    @Test
    public void groupByHostNameShouldHaveContainersFirstAndGroupedSeparatelyByAgentIdAscendingOrder() {
        AgentAndStatus host1Agent1 = createAgentInfo("APP_1", "host1-agent1", "Host1", false);
        AgentAndStatus host2Agent1 = createAgentInfo("APP_1", "host2-agent1", "Host2", false);
        AgentAndStatus containerAgent1 = createAgentInfo("APP_1", "container-agent1", "Host3", true, 1);
        AgentAndStatus containerAgent2 = createAgentInfo("APP_1", "container-agent2", "Host4", true, 2);
        List<AgentAndStatus> agentAndStatusList = shuffleAgentInfos(containerAgent1, host1Agent1, host2Agent1, containerAgent2);


        SortByAgentInfo<AgentStatusAndLink> sortBy = SortByAgentInfo.agentIdAsc(AgentStatusAndLink::getAgentInfo);
        AgentsMapByHost agentsMapByHost = AgentsMapByHost.newAgentsMapByHost(agent -> true, sortBy, hyperLinkFactory, agentAndStatusList);
        List<InstancesList<AgentStatusAndLink>> instancesLists = agentsMapByHost.getAgentsListsList();

        assertThat(instancesLists).hasSize(3);

        InstancesList<AgentStatusAndLink> containerInstancesList = instancesLists.get(0);
        assertEquals(AgentsMapByHost.CONTAINER, containerInstancesList.getGroupName());
        List<AgentStatusAndLink> containerAgents = containerInstancesList.getInstancesList();
        assertThat(containerAgents)
                .map(AgentStatusAndLink::getAgentInfo)
                .containsExactly(containerAgent1.getAgentInfo(), containerAgent2.getAgentInfo());

        InstancesList<AgentStatusAndLink> host1InstancesList = instancesLists.get(1);
        assertEquals("Host1", host1InstancesList.getGroupName());
        List<AgentStatusAndLink> host1Agents = host1InstancesList.getInstancesList();
        assertThat(host1Agents)
                .map(AgentStatusAndLink::getAgentInfo)
                .containsExactly(host1Agent1.getAgentInfo());

        InstancesList<AgentStatusAndLink> host2InstancesList = instancesLists.get(2);
        assertEquals("Host2", host2InstancesList.getGroupName());
        List<AgentStatusAndLink> host2Agents = host2InstancesList.getInstancesList();
        assertThat(host2Agents)
                .map(AgentStatusAndLink::getAgentInfo)
                .containsExactly(host2Agent1.getAgentInfo());
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
