package com.navercorp.pinpoint.web.service.component;

import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.web.service.AgentEventService;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultActiveAgentValidatorTest {

    @Mock
    AgentEventService agentEventService;

    String agentId = "testNodeApp";
    ServiceType node = ServiceTypeFactory.of(1400, "node");
//    Application java = new Application("testJavaApp", ServiceTypeFactory.of(1010, "java"));

    @Test
    void isActiveAgent_legacy_node() {
        LegacyAgentCompatibility agentCompatibility = new DefaultLegacyAgentCompatibility();
        ActiveAgentValidator validator = new DefaultActiveAgentValidator(agentEventService, agentCompatibility);

        Assertions.assertFalse(validator.isActiveAgent(agentId, node.getCode(), "0.7.0", Range.between(0, 1)));
    }

    @Test
    void isActiveAgent_legacy_node_with_gc() {
        LegacyAgentCompatibility agentCompatibility = new DefaultLegacyAgentCompatibility();
        ActiveAgentValidator validator = new DefaultActiveAgentValidator(agentEventService, agentCompatibility);

        AgentEvent gc = new AgentEvent("test", 1, 1, AgentEventType.AGENT_PING);
        when(agentEventService.getAgentEvents(any(), any(), any())).thenReturn(List.of(gc));

        Assertions.assertTrue(validator.isActiveAgent(agentId, node.getCode(), "5.0.0", Range.between(0, 1)));
    }

    @Test
    void isActiveAgent_new_node_without_ping() {
        LegacyAgentCompatibility agentCompatibility = new DefaultLegacyAgentCompatibility();
        ActiveAgentValidator validator = new DefaultActiveAgentValidator(agentEventService, agentCompatibility);

        Assertions.assertFalse(validator.isActiveAgent(agentId, node.getCode(), "0.8.0", Range.between(0, 1)));

        verify(agentEventService).getAgentEvents(any(), any(), any());
    }

    @Test
    void isActiveAgent_new_node_with_event() {
        AgentEvent ping = new AgentEvent("test", 1, 1, AgentEventType.AGENT_PING);
        when(agentEventService.getAgentEvents(any(), any(), any())).thenReturn(List.of(ping));

        LegacyAgentCompatibility agentCompatibility = new DefaultLegacyAgentCompatibility();
        ActiveAgentValidator validator = new DefaultActiveAgentValidator(agentEventService, agentCompatibility);

        Assertions.assertTrue(validator.isActiveAgent(agentId, node.getCode(), "0.8.0", Range.between(0, 1)));
    }
}