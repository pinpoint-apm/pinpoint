package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceImplTest {

    final String AGENT_ID1 = "TEST_AGENT_ID1";
    final String AGENT_ID2 = "TEST_AGENT_ID2";
    final String AGENT_ID3 = "TEST_AGENT_ID3";

    final String APPLICATION_NAME1 = "TEST_APP1";
    final String APPLICATION_NAME2 = "TEST_APP2";
    final String APPLICATION_NAME3 = "TEST_APP3";

    final int MIN_DURATION_DAYS_FOR_INACTIVITY = 30;

    AdminService adminService;

    @Mock
    ApplicationIndexDao applicationIndexDao;

    @Mock
    AgentInfoService agentInfoService;

    @BeforeEach
    public void setUp() {
        adminService = new AdminServiceImpl(applicationIndexDao, agentInfoService);
    }

    @Test
    public void constructorRequireNonNullTest() {
        try {
            new AdminServiceImpl(null, agentInfoService);
            fail("applicationIndexDao can not be null");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("applicationIndexDao");
        }

        try {
            new AdminServiceImpl(applicationIndexDao, null);
            fail("agentInfoService can not be null");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("agentInfoService");
        }

        try {
            new AdminServiceImpl(null, null);
            fail("applicationIndexDao and jvmGcDao can not be null");
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).isEqualTo("applicationIndexDao");
        }
    }

    @Test
    public void removeApplicationName() {
        // given
        doNothing().when(applicationIndexDao).deleteApplicationName(APPLICATION_NAME1);

        // when
        adminService.removeApplicationName(APPLICATION_NAME1);

        // then
        verify(applicationIndexDao).deleteApplicationName(APPLICATION_NAME1);
    }

    @Test
    public void removeAgentId() {
        // given
        doNothing().when(applicationIndexDao).deleteAgentId(APPLICATION_NAME1, AGENT_ID1);

        // when
        adminService.removeAgentId(APPLICATION_NAME1, AGENT_ID1);

        // then
        verify(applicationIndexDao).deleteAgentId(APPLICATION_NAME1, AGENT_ID1);
    }

    @Test
    public void whenMinDurationDaysForInActivityIsLessThanDurationDaysDoThrowIllegalArgumentException() {
        try {
            adminService.removeInactiveAgents(29);
            fail("Exception must be caught when durationDays is less than MIN_DURATION_DAYS_FOR_INACIVITY");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("duration may not be less than " + MIN_DURATION_DAYS_FOR_INACTIVITY + " days");
        }

        try {
            adminService.removeInactiveAgents(30);
            adminService.removeInactiveAgents(31);
        } catch (Exception e) {
            fail("Exception can not be caught when durationDays is more than MIN_DURATION_DAYS_FOR_INACTIVITY");
        }
    }

    @Test
    public void whenAgentStatExistsWithInDurationDaysDoNotRemoveInactiveAgents() {
        // given
        int durationDays = 31;

        //// mocking
        when(applicationIndexDao.selectAgentIds(eq(APPLICATION_NAME1))).thenReturn(List.of(AGENT_ID1));
        when(applicationIndexDao.selectAllApplicationNames()).thenReturn(List.of(new Application(APPLICATION_NAME1, ServiceType.TEST)));
        when(agentInfoService.isActiveAgent(eq(AGENT_ID1), any(Range.class))).thenReturn(true);

        // when
        adminService.removeInactiveAgents(durationDays);

        verify(applicationIndexDao, never()).deleteAgentIds(any());
    }

    @Test
    public void whenAgentStatExistsOutOfDurationDaysDoRemoveInactiveAgents() {
        // given
        int durationDays = 31;

        //// mocking
        when(applicationIndexDao.selectAgentIds(anyString())).thenReturn(List.of(AGENT_ID1, AGENT_ID2, AGENT_ID3));
        when(applicationIndexDao.selectAllApplicationNames()).thenReturn(List.of(new Application(APPLICATION_NAME1, ServiceType.TEST)));
        doAnswer(invocation -> {
            Map<String, List<String>> inactiveAgentMap = invocation.getArgument(0);
            List<String> inactiveAgents = inactiveAgentMap.get(APPLICATION_NAME1);

            assertThat(inactiveAgents.size()).isEqualTo(2);
            assertThat(inactiveAgents.get(0)).isEqualTo(AGENT_ID1);
            assertThat(inactiveAgents.get(1)).isEqualTo(AGENT_ID2);

            return inactiveAgents;
        }).when(applicationIndexDao).deleteAgentIds(any());

        when(agentInfoService.isActiveAgent(eq(AGENT_ID1), any(Range.class))).thenReturn(false);
        when(agentInfoService.isActiveAgent(eq(AGENT_ID2), any(Range.class))).thenReturn(false);
        when(agentInfoService.isActiveAgent(eq(AGENT_ID3), any(Range.class))).thenReturn(true);

        // when
        adminService.removeInactiveAgents(durationDays);
    }

    @Test
    public void whenApplicationDoesNotHaveAnyAgentIdsGetAgentIdMapReturnsEmptyMap() {
        // given
        List<Application> emptyApplicationList = new ArrayList<>();
        when(applicationIndexDao.selectAllApplicationNames()).thenReturn(emptyApplicationList);

        // when
        Map<String, List<Application>> agentIdMap = adminService.getAgentIdMap();

        // then
        assertThat(agentIdMap).isNotNull().isEmpty();
    }

    @Test
    public void testDuplicateAgentIdMap() {
        // given
        when(applicationIndexDao.selectAllApplicationNames())
                .thenReturn(List.of(
                        new Application(APPLICATION_NAME1, ServiceType.UNDEFINED),
                        new Application(APPLICATION_NAME2, ServiceType.UNDEFINED),
                        new Application(APPLICATION_NAME3, ServiceType.UNDEFINED)));

        when(applicationIndexDao.selectAgentIds(eq(APPLICATION_NAME1)))
                .thenReturn(List.of(AGENT_ID1, AGENT_ID2, AGENT_ID3));
        when(applicationIndexDao.selectAgentIds(eq(APPLICATION_NAME2)))
                .thenReturn(List.of(AGENT_ID2, AGENT_ID3));
        when(applicationIndexDao.selectAgentIds(eq(APPLICATION_NAME3)))
                .thenReturn(List.of(AGENT_ID1));

        // then
        Map<String, List<Application>> duplicateAgentIdMap = adminService.getDuplicateAgentIdMap();

        assertThat(duplicateAgentIdMap.size()).isEqualTo(3);
        assertThat(duplicateAgentIdMap.get(AGENT_ID1).size()).isEqualTo(2);
        assertThat(duplicateAgentIdMap.get(AGENT_ID2).size()).isEqualTo(2);
        assertThat(duplicateAgentIdMap.get(AGENT_ID3).size()).isEqualTo(2);

        // check the application names
        List<String> applicationNamesOfAgentId1 = duplicateAgentIdMap.get(AGENT_ID1).stream().map(Application::getName).collect(Collectors.toList());
        assertTrue(applicationNamesOfAgentId1.containsAll(List.of(APPLICATION_NAME1, APPLICATION_NAME3)));

        List<String> applicationNamesOfAgentId2 = duplicateAgentIdMap.get(AGENT_ID2).stream().map(Application::getName).collect(Collectors.toList());
        assertTrue(applicationNamesOfAgentId2.containsAll(List.of(APPLICATION_NAME1, APPLICATION_NAME2)));

        List<String> applicationNamesOfAgentId3 = duplicateAgentIdMap.get(AGENT_ID3).stream().map(Application::getName).collect(Collectors.toList());
        assertTrue(applicationNamesOfAgentId3.containsAll(List.of(APPLICATION_NAME1, APPLICATION_NAME2)));
    }

}