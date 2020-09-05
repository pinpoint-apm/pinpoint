package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.stat.JvmGcDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdminServiceImplTest {

    final String AGENT_ID1 = "TEST_AGENT_ID1";
    final String AGENT_ID2 = "TEST_AGENT_ID2";
    final String AGENT_ID3 = "TEST_AGENT_ID3";

    final String APPLICATION_NAME1 = "TEST_APP1";
    final String APPLICATION_NAME2 = "TEST_APP2";
    final String APPLICATION_NAME3 = "TEST_APP3";

    final int MIN_DURATION_DAYS_FOR_INACTIVITY = 30;

    AdminService adminService;

    @Mock ApplicationIndexDao applicationIndexDao;

    @Mock JvmGcDao jvmGcDao;

    @Before
    public void setUp() {
        adminService = new AdminServiceImpl(applicationIndexDao, jvmGcDao);
    }

    @Test
    public void constructorRequireNonNullTest() {
        try {
            new AdminServiceImpl(null, jvmGcDao);
            fail("applicationIndexDao can not be null");
        } catch(NullPointerException e) {
            assertThat(e.getMessage(), is("applicationIndexDao"));
        }

        try {
            new AdminServiceImpl(applicationIndexDao, null);
            fail("jvmGcDao can not be null");
        } catch(NullPointerException e ) {
            assertThat(e.getMessage(), is("jvmGcDao"));
        }

        try {
            new AdminServiceImpl(null, null);
            fail("applicationIndexDao and jvmGcDao can not be null");
        } catch(NullPointerException e) {
            assertThat(e.getMessage(), is("applicationIndexDao"));
        }
    }

    @Test
    public void removeApplicationName() {
        // given
        doNothing().when(applicationIndexDao).deleteApplicationName(APPLICATION_NAME1);

        // when
        adminService.removeApplicationName(APPLICATION_NAME1);

        // then
        verify(applicationIndexDao, times(1)).deleteApplicationName(APPLICATION_NAME1);
    }

    @Test
    public void removeAgentId() {
        // given
        doNothing().when(applicationIndexDao).deleteAgentId(APPLICATION_NAME1, AGENT_ID1);

        // when
        adminService.removeAgentId(APPLICATION_NAME1, AGENT_ID1);

        // then
        verify(applicationIndexDao, times(1)).deleteAgentId(APPLICATION_NAME1, AGENT_ID1);
    }

    @Test
    public void whenMinDurationDaysForInActivityIsLessThanDurationDaysDoThrowIllegalArgumentException() {
        try {
            adminService.removeInactiveAgents(29);
            fail("Exception must be caught when durationDays is less than MIN_DURATION_DAYS_FOR_INACIVITY");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("duration may not be less than " + MIN_DURATION_DAYS_FOR_INACTIVITY + " days")) ;
        }

        try {
            adminService.removeInactiveAgents(30);
            adminService.removeInactiveAgents(31);
        } catch(Exception e) {
            fail("Exception can not be caught when durationDays is more than MIN_DURATION_DAYS_FOR_INACTIVITY");
        }
    }

    @Test
    public void whenAgentStatExistsWithInDurationDaysDoNotRemoveInactiveAgents() {
        // given
        int durationDays = 31;

        //// mocking
        when(applicationIndexDao.selectAgentIds(APPLICATION_NAME1)).thenReturn(Arrays.asList(AGENT_ID1));
        when(applicationIndexDao.selectAllApplicationNames()).thenReturn(Arrays.asList(new Application(APPLICATION_NAME1, ServiceType.TEST)));
        when(jvmGcDao.agentStatExists(eq(AGENT_ID1), any(Range.class))).thenReturn(true);

        // when
        adminService.removeInactiveAgents(durationDays);

        ArgumentCaptor<Map<String, List<String>>> inactiveAgentMapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(applicationIndexDao).deleteAgentIds(inactiveAgentMapArgumentCaptor.capture());

        List<String> actualInactiveAgentMapValues = inactiveAgentMapArgumentCaptor.getValue().get(APPLICATION_NAME1);

        // then
        assertThat(actualInactiveAgentMapValues == null, is(true));
    }

    @Test
    public void whenAgentStatExistsOutOfDurationDaysDoRemoveInactiveAgents() {
        // given
        int durationDays = 31;
        //// mocking
        when(applicationIndexDao.selectAgentIds(APPLICATION_NAME1)).thenReturn(Arrays.asList(AGENT_ID1, AGENT_ID2, AGENT_ID3));
        when(applicationIndexDao.selectAllApplicationNames()).thenReturn(Arrays.asList(new Application(APPLICATION_NAME1, ServiceType.TEST)));

        when(jvmGcDao.agentStatExists(eq(AGENT_ID1), any(Range.class))).thenReturn(false);

        // when
        adminService.removeInactiveAgents(durationDays);

        ArgumentCaptor<Map<String, List<String>>> inactiveAgentMapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(applicationIndexDao).deleteAgentIds(inactiveAgentMapArgumentCaptor.capture());

        // then
        List<String> actualInactiveAgentMapValues = inactiveAgentMapArgumentCaptor.getValue().get(APPLICATION_NAME1);

        assertThat(actualInactiveAgentMapValues.size(), is(3));
        assertThat(actualInactiveAgentMapValues.get(0), is(AGENT_ID1));
        assertThat(actualInactiveAgentMapValues.get(1), is(AGENT_ID2));
        assertThat(actualInactiveAgentMapValues.get(2), is(AGENT_ID3));
    }

    @Test
    public void whenApplicationDoesNotHaveAnyAgentIdsGetAgentIdMapReturnsEmptyMap() {
        // given
        List<Application> emptyApplicationList = new ArrayList<>();
        when(applicationIndexDao.selectAllApplicationNames()).thenReturn(emptyApplicationList);

        // when
        Map<String, List<Application>> agentIdMap = adminService.getAgentIdMap();

        // then
        assertThat(agentIdMap == null, is(false));
        assertThat(agentIdMap.size(), is(0));
    }

    @Test
    public void testDuplicateAgentIdMap() {
        // given
        when(applicationIndexDao.selectAllApplicationNames())
                .thenReturn(Arrays.asList(
                                new Application(APPLICATION_NAME1, ServiceType.UNDEFINED),
                                new Application(APPLICATION_NAME2, ServiceType.UNDEFINED),
                                new Application(APPLICATION_NAME3, ServiceType.UNDEFINED)));

        when(applicationIndexDao.selectAgentIds(eq(APPLICATION_NAME1)))
                .thenReturn(Arrays.asList(AGENT_ID1, AGENT_ID2, AGENT_ID3));
        when(applicationIndexDao.selectAgentIds(eq(APPLICATION_NAME2)))
                .thenReturn(Arrays.asList(AGENT_ID2, AGENT_ID3));
        when(applicationIndexDao.selectAgentIds(eq(APPLICATION_NAME3)))
                .thenReturn(Arrays.asList(AGENT_ID1));

        // then
        Map<String, List<Application>> duplicateAgentIdMap = adminService.getDuplicateAgentIdMap();

        assertThat(duplicateAgentIdMap.size(), is(3));
        assertThat(duplicateAgentIdMap.get(AGENT_ID1).size(), is(2));
        assertThat(duplicateAgentIdMap.get(AGENT_ID2).size(), is(2));
        assertThat(duplicateAgentIdMap.get(AGENT_ID3).size(), is(2));

        // check the application names
        List<String> applicationNamesOfAgentId1 = duplicateAgentIdMap.get(AGENT_ID1).stream().map(Application::getName).collect(Collectors.toList());
        assertTrue(applicationNamesOfAgentId1.containsAll(Arrays.asList(APPLICATION_NAME1, APPLICATION_NAME3)));

        List<String> applicationNamesOfAgentId2 = duplicateAgentIdMap.get(AGENT_ID2).stream().map(Application::getName).collect(Collectors.toList());
        assertTrue(applicationNamesOfAgentId2.containsAll(Arrays.asList(APPLICATION_NAME1, APPLICATION_NAME2)));

        List<String> applicationNamesOfAgentId3 = duplicateAgentIdMap.get(AGENT_ID3).stream().map(Application::getName).collect(Collectors.toList());
        assertTrue(applicationNamesOfAgentId3.containsAll(Arrays.asList(APPLICATION_NAME1, APPLICATION_NAME2)));
    }

}