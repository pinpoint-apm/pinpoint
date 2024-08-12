package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.service.component.ActiveAgentValidator;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    AdminService adminService;

    @Mock
    ApplicationIndexDao applicationIndexDao;

    @Mock
    ActiveAgentValidator activeAgentValidator;

    @BeforeEach
    public void setUp() {
        adminService = new AdminServiceImpl(applicationIndexDao, activeAgentValidator);
    }

    @Test
    public void constructorRequireNonNullTest() {

        assertThatThrownBy(() -> new AdminServiceImpl(null, activeAgentValidator))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("applicationIndexDao");

        assertThatThrownBy(() -> new AdminServiceImpl(applicationIndexDao, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("activeAgentValidator");

        assertThatThrownBy(() -> new AdminServiceImpl(null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("applicationIndexDao");
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
    public void whenApplicationDoesNotHaveAnyAgentIdsGetAgentIdMapReturnsEmptyMap() {
        // given
        List<Application> emptyApplicationList = List.of();
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

        assertThat(duplicateAgentIdMap).hasSize(3);
        assertThat(duplicateAgentIdMap.get(AGENT_ID1)).hasSize(2);
        assertThat(duplicateAgentIdMap.get(AGENT_ID2)).hasSize(2);
        assertThat(duplicateAgentIdMap.get(AGENT_ID3)).hasSize(2);

        // check the application names
        List<String> applicationNamesOfAgentId1 = duplicateAgentIdMap.get(AGENT_ID1).stream().map(Application::getName).collect(Collectors.toList());
        assertThat(applicationNamesOfAgentId1).containsAll(List.of(APPLICATION_NAME1, APPLICATION_NAME3));

        List<String> applicationNamesOfAgentId2 = duplicateAgentIdMap.get(AGENT_ID2).stream().map(Application::getName).collect(Collectors.toList());
        assertThat(applicationNamesOfAgentId2).containsAll(List.of(APPLICATION_NAME1, APPLICATION_NAME2));

        List<String> applicationNamesOfAgentId3 = duplicateAgentIdMap.get(AGENT_ID3).stream().map(Application::getName).collect(Collectors.toList());
        assertThat(applicationNamesOfAgentId3).containsAll(List.of(APPLICATION_NAME1, APPLICATION_NAME2));
    }

}