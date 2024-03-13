package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {

    final String AGENT_ID1 = "TEST_AGENT_ID1";
    final String AGENT_ID2 = "TEST_AGENT_ID2";
    final String AGENT_ID3 = "TEST_AGENT_ID3";

    final String APPLICATION_NAME1 = "TEST_APP1";
    final String APPLICATION_NAME2 = "TEST_APP2";
    final String APPLICATION_NAME3 = "TEST_APP3";

    final UUID APPLICATION_UUID1 = new UUID(1, 1);
    final UUID APPLICATION_UUID2 = new UUID(2, 2);
    final UUID APPLICATION_UUID3 = new UUID(3, 3);

    AdminService adminService;

    @Mock
    ApplicationInfoService applicationInfoService;

    @Mock
    ApplicationService applicationService;

    @Mock
    AgentInfoService agentInfoService;

    @BeforeEach
    public void setUp() {
        adminService = new AdminServiceImpl(applicationInfoService, applicationService, agentInfoService);
    }

    @Test
    public void removeApplicationName() {
        // given
        when(applicationInfoService.getApplicationId(APPLICATION_NAME1)).thenReturn(APPLICATION_UUID1);
        doNothing().when(applicationService).deleteApplication(APPLICATION_UUID1);

        // when
        adminService.removeApplicationName(APPLICATION_NAME1);

        // then
        verify(applicationService).deleteApplication(APPLICATION_UUID1);
    }

    @Test
    public void removeAgentId() {
        // given
        when(applicationInfoService.getApplicationId(APPLICATION_NAME1)).thenReturn(APPLICATION_UUID1);
        doNothing().when(applicationService).deleteAgent(APPLICATION_UUID1, AGENT_ID1);

        // when
        adminService.removeAgentId(APPLICATION_NAME1, AGENT_ID1);

        // then
        verify(applicationService).deleteAgent(APPLICATION_UUID1, AGENT_ID1);
    }

    @Test
    public void whenApplicationDoesNotHaveAnyAgentIdsGetAgentIdMapReturnsEmptyMap() {
        // given
        List<Application> emptyApplicationList = List.of();
        when(applicationService.getApplications()).thenReturn(emptyApplicationList);

        // when
        Map<String, List<Application>> agentIdMap = adminService.getAgentIdMap();

        // then
        assertThat(agentIdMap).isNotNull().isEmpty();
    }

    @Test
    public void testDuplicateAgentIdMap() {
        // given
        when(applicationService.getApplications())
                .thenReturn(List.of(
                        new Application(APPLICATION_UUID1, APPLICATION_NAME1, ServiceType.UNDEFINED),
                        new Application(APPLICATION_UUID2, APPLICATION_NAME2, ServiceType.UNDEFINED),
                        new Application(APPLICATION_UUID3, APPLICATION_NAME3, ServiceType.UNDEFINED)));

        when(applicationService.getAgents(eq(APPLICATION_UUID1)))
                .thenReturn(List.of(AGENT_ID1, AGENT_ID2, AGENT_ID3));
        when(applicationService.getAgents(eq(APPLICATION_UUID2)))
                .thenReturn(List.of(AGENT_ID2, AGENT_ID3));
        when(applicationService.getAgents(eq(APPLICATION_UUID3)))
                .thenReturn(List.of(AGENT_ID1));

        // then
        Map<String, List<Application>> duplicateAgentIdMap = adminService.getDuplicateAgentIdMap();

        assertThat(duplicateAgentIdMap).hasSize(3);
        assertThat(duplicateAgentIdMap.get(AGENT_ID1)).hasSize(2);
        assertThat(duplicateAgentIdMap.get(AGENT_ID2)).hasSize(2);
        assertThat(duplicateAgentIdMap.get(AGENT_ID3)).hasSize(2);

        // check the application names
        List<String> applicationNamesOfAgentId1 = duplicateAgentIdMap.get(AGENT_ID1).stream().map(Application::name).collect(Collectors.toList());
        assertThat(applicationNamesOfAgentId1).containsAll(List.of(APPLICATION_NAME1, APPLICATION_NAME3));

        List<String> applicationNamesOfAgentId2 = duplicateAgentIdMap.get(AGENT_ID2).stream().map(Application::name).collect(Collectors.toList());
        assertThat(applicationNamesOfAgentId2).containsAll(List.of(APPLICATION_NAME1, APPLICATION_NAME2));

        List<String> applicationNamesOfAgentId3 = duplicateAgentIdMap.get(AGENT_ID3).stream().map(Application::name).collect(Collectors.toList());
        assertThat(applicationNamesOfAgentId3).containsAll(List.of(APPLICATION_NAME1, APPLICATION_NAME2));
    }

}