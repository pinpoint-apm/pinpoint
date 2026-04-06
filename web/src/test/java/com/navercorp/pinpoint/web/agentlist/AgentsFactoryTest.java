package com.navercorp.pinpoint.web.agentlist;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentNameGroupView;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgentsFactoryTest {

    @Test
    void groupByAgentName_singleAgent() {
        AgentStatusAndLink agent = agentWithName("myAgent", "agent-001", 1000L);

        List<AgentNameGroupView> result = AgentsFactory.groupByAgentName(List.of(agent));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAgentInfo().getAgentName()).isEqualTo("myAgent");
        assertThat(result.get(0).getAgentIds()).containsExactly("agent-001");
    }

    @Test
    void groupByAgentName_multipleAgentsSameName() {
        AgentStatusAndLink older = agentWithName("myAgent", "agent-001", 1000L);
        AgentStatusAndLink newer = agentWithName("myAgent", "agent-002", 2000L);

        List<AgentNameGroupView> result = AgentsFactory.groupByAgentName(List.of(older, newer));

        assertThat(result).hasSize(1);
        AgentNameGroupView group = result.get(0);
        assertThat(group.getAgentInfo().getAgentId()).isEqualTo("agent-002");
        assertThat(group.getAgentIds()).containsExactlyInAnyOrder("agent-001", "agent-002");
    }

    @Test
    void groupByAgentName_differentNames() {
        AgentStatusAndLink a = agentWithName("alpha", "agent-001", 1000L);
        AgentStatusAndLink b = agentWithName("beta", "agent-002", 1000L);

        List<AgentNameGroupView> result = AgentsFactory.groupByAgentName(List.of(a, b));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(v -> v.getAgentInfo().getAgentName())
                .containsExactlyInAnyOrder("alpha", "beta");
    }

    @Test
    void groupByAgentName_nullAgentName_fallbackToAgentId() {
        AgentStatusAndLink agent = agentWithName(null, "agent-001", 1000L);

        List<AgentNameGroupView> result = AgentsFactory.groupByAgentName(List.of(agent));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAgentIds()).containsExactly("agent-001");
    }

    @Test
    void groupByAgentName_emptyAgentName_fallbackToAgentId() {
        AgentStatusAndLink agent = agentWithName("", "agent-001", 1000L);

        List<AgentNameGroupView> result = AgentsFactory.groupByAgentName(List.of(agent));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAgentIds()).containsExactly("agent-001");
    }

    @Test
    void groupByAgentName_mixedNullAndNonNullNames() {
        AgentStatusAndLink named = agentWithName("myAgent", "agent-001", 1000L);
        AgentStatusAndLink notFound = agentWithName(null, "agent-002", 1000L);

        List<AgentNameGroupView> result = AgentsFactory.groupByAgentName(List.of(named, notFound));

        assertThat(result).hasSize(2);
    }

    @Test
    void groupByAgentName_empty() {
        List<AgentNameGroupView> result = AgentsFactory.groupByAgentName(List.of());

        assertThat(result).isEmpty();
    }

    private AgentStatusAndLink agentWithName(String agentName, String agentId, long startTimestamp) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setAgentId(agentId);
        agentInfo.setAgentName(agentName);
        agentInfo.setStartTimestamp(startTimestamp);
        agentInfo.setServiceType(ServiceType.STAND_ALONE);
        AgentStatus status = new AgentStatus(agentId, AgentLifeCycleState.RUNNING, startTimestamp);
        return new AgentStatusAndLink(agentInfo, status, List.of());
    }
}
