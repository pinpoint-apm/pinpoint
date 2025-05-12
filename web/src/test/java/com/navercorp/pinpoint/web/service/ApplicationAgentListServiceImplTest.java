package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicationAgentListServiceImplTest {

    private final String testApplicationName = "testApplicationName";
    private final String testAgentId = "testAgentId";
    private final ServiceType testApplicationServiceType = ServiceType.TEST;

    private Application testApplication;
    private AgentInfo testAgentInfo;
    private AgentStatus testAgentStatus;
    private ApplicationResponse applicationResponse;

    @Mock
    ApplicationIndexDao applicationIndexDao;
    @Mock
    AgentInfoDao agentInfoDao;

    @Mock
    AgentLifeCycleDao agentLifeCycleDao;

    @Mock
    MapResponseDao mapResponseDao;

    ApplicationAgentListService applicationAgentListService;

    @BeforeEach
    public void setup() {
        testApplication = new Application(testApplicationName, ServiceType.TEST);

        testAgentInfo = new AgentInfo();
        testAgentInfo.setApplicationName("testApplicationName");
        testAgentInfo.setAgentId(testAgentId);
        testAgentInfo.setAgentName("testAgentName");
        testAgentInfo.setStartTimestamp(1000);
        testAgentInfo.setHostName("testHostName");
        testAgentInfo.setIp("testIp");
        testAgentInfo.setPorts("testPorts");
        testAgentInfo.setServiceType(ServiceType.TEST);
        testAgentInfo.setPid(1);
        testAgentInfo.setVmVersion("testVmVersion");
        testAgentInfo.setAgentVersion("testAgentVersion");
        testAgentInfo.setContainer(false);

        testAgentStatus = new AgentStatus(testAgentId, AgentLifeCycleState.RUNNING, 2000);

        Histogram histogram = new Histogram(ServiceType.TEST);
        ApplicationResponse.Builder appBuilder = ApplicationResponse.newBuilder(testApplication);
        appBuilder.addResponseTime(testAgentId, 3000, histogram);
        applicationResponse = appBuilder.build();

        applicationAgentListService = new ApplicationAgentListServiceImpl(applicationIndexDao, agentInfoDao, agentLifeCycleDao, mapResponseDao);
    }

    @Test
    public void allAgentListTest() {
        Range range = Range.between(0, 60_000);
        List<String> agentIds = List.of(testAgentId);
        when(applicationIndexDao.selectAgentIds(testApplicationName)).thenReturn(agentIds);
        when(agentInfoDao.getSimpleAgentInfos(agentIds, range.getTo())).thenReturn(List.of(testAgentInfo));

        List<AgentAndStatus> agentAndStatusList = applicationAgentListService.allAgentList(testApplicationName, testApplicationServiceType, range, AgentInfoFilters.acceptAll());

        Assertions.assertThat(agentAndStatusList).hasSize(1);
        AgentAndStatus agentAndStatus = agentAndStatusList.get(0);
        Assertions.assertThat(agentAndStatus.getAgentInfo()).isEqualTo(testAgentInfo);
    }

    @Test
    public void allAgentListTest2() {
        Range range = Range.between(0, 60_000);
        List<String> agentIds = List.of(testAgentId);
        when(applicationIndexDao.selectAgentIds(testApplicationName)).thenReturn(agentIds);
        when(agentInfoDao.getSimpleAgentInfos(agentIds, range.getTo())).thenReturn(List.of(testAgentInfo));

        List<AgentAndStatus> agentAndStatusList = applicationAgentListService.allAgentList(testApplicationName, null, range, AgentInfoFilters.acceptAll());

        Assertions.assertThat(agentAndStatusList).hasSize(1);
        AgentAndStatus agentAndStatus = agentAndStatusList.get(0);
        Assertions.assertThat(agentAndStatus.getAgentInfo()).isEqualTo(testAgentInfo);
    }

    @Test
    public void activeStatusAgentListTest() {
        Range range = Range.between(0, 60_000);
        TimeWindow timeWindow = new TimeWindow(range);
        List<String> agentIds = List.of(testAgentId);
        when(applicationIndexDao.selectAgentIds(testApplicationName)).thenReturn(agentIds);
        when(agentInfoDao.getSimpleAgentInfos(agentIds, range.getTo())).thenReturn(List.of(testAgentInfo));
        when(agentLifeCycleDao.getAgentStatus(ArgumentMatchers.any())).thenReturn(List.of(Optional.of(testAgentStatus)));

        List<AgentAndStatus> agentAndStatusList = applicationAgentListService.activeStatusAgentList(testApplicationName, testApplicationServiceType, timeWindow, AgentInfoFilters.acceptAll());

        Assertions.assertThat(agentAndStatusList).hasSize(1);
        AgentAndStatus agentAndStatus = agentAndStatusList.get(0);
        Assertions.assertThat(agentAndStatus.getAgentInfo()).isEqualTo(testAgentInfo);
        Assertions.assertThat(agentAndStatus.getStatus()).isEqualTo(testAgentStatus);
    }

    @Test
    public void activeStatusAgentListTest2() {
        Range range = Range.between(0, 60_000);
        TimeWindow timeWindow = new TimeWindow(range);

        List<String> agentIds = List.of(testAgentId);
        when(applicationIndexDao.selectAgentIds(testApplicationName)).thenReturn(agentIds);
        when(agentInfoDao.getSimpleAgentInfos(agentIds, range.getTo())).thenReturn(List.of(testAgentInfo));
        when(agentLifeCycleDao.getAgentStatus(ArgumentMatchers.any())).thenReturn(List.of(Optional.of(testAgentStatus)));

        List<AgentAndStatus> agentAndStatusList = applicationAgentListService.activeStatusAgentList(testApplicationName, null, timeWindow, AgentInfoFilters.acceptAll());

        Assertions.assertThat(agentAndStatusList).hasSize(1);
        AgentAndStatus agentAndStatus = agentAndStatusList.get(0);
        Assertions.assertThat(agentAndStatus.getAgentInfo()).isEqualTo(testAgentInfo);
        Assertions.assertThat(agentAndStatus.getStatus()).isEqualTo(testAgentStatus);
    }

    @Test
    public void activeResponseAgentListTest() {
        Range range = Range.between(0, 60_000);
        TimeWindow timeWindow = new TimeWindow(range);

        List<String> agentIds = List.of(testAgentId);
        when(mapResponseDao.selectApplicationResponse(any(), any())).thenReturn(applicationResponse);
        when(agentInfoDao.getSimpleAgentInfos(agentIds, range.getTo())).thenReturn(List.of(testAgentInfo));

        List<AgentAndStatus> agentAndStatusList = applicationAgentListService.activeStatisticsAgentList(testApplicationName, testApplicationServiceType, timeWindow, AgentInfoFilters.acceptAll());

        Assertions.assertThat(agentAndStatusList).hasSize(1);
        AgentAndStatus agentAndStatus = agentAndStatusList.get(0);
        Assertions.assertThat(agentAndStatus.getStatus().getState()).isEqualTo(AgentLifeCycleState.RUNNING);
    }

    @Test
    public void activeResponseAgentListTest2() {
        Range range = Range.between(0, 60_000);
        TimeWindow timeWindow = new TimeWindow(range);

        List<String> agentIds = List.of(testAgentId);
        when(applicationIndexDao.selectApplicationName(testApplicationName)).thenReturn(List.of(testApplication));
        when(mapResponseDao.selectApplicationResponse(any(), any())).thenReturn(applicationResponse);
        when(agentInfoDao.getSimpleAgentInfos(agentIds, range.getTo())).thenReturn(List.of(testAgentInfo));

        List<AgentAndStatus> agentAndStatusList = applicationAgentListService.activeStatisticsAgentList(testApplicationName, null, timeWindow, AgentInfoFilters.acceptAll());

        Assertions.assertThat(agentAndStatusList).hasSize(1);
        AgentAndStatus agentAndStatus = agentAndStatusList.get(0);
        Assertions.assertThat(agentAndStatus.getStatus().getState()).isEqualTo(AgentLifeCycleState.RUNNING);
    }


    @Test
    public void nullAgentInfoHandleTest1() {
        Range range = Range.between(0, 60_000);
        List<String> agentIds = List.of(testAgentId);
        when(applicationIndexDao.selectAgentIds(testApplicationName)).thenReturn(agentIds);
        List<AgentInfo> nullAgentInfoList = new ArrayList<>();
        nullAgentInfoList.add(null);
        when(agentInfoDao.getSimpleAgentInfos(agentIds, range.getTo())).thenReturn(nullAgentInfoList);

        List<AgentAndStatus> agentAndStatusList = applicationAgentListService.allAgentList(testApplicationName, testApplicationServiceType, range, AgentInfoFilters.acceptAll());

        Assertions.assertThat(agentAndStatusList).hasSize(1);
        AgentAndStatus agentAndStatus = agentAndStatusList.get(0);
        Assertions.assertThat(agentAndStatus.getAgentInfo()).isNotNull();
        Assertions.assertThat(agentAndStatus.getAgentInfo().getAgentId()).isEqualTo(testAgentId);
        Assertions.assertThat(agentAndStatus.getAgentInfo().getServiceType()).isNotNull();
        Assertions.assertThat(agentAndStatus.getAgentInfo().getHostName()).isNotBlank();
    }

    @Test
    public void nullAgentInfoHandleTest2() {
        Range range = Range.between(0, 60_000);
        TimeWindow timeWindow = new TimeWindow(range);

        List<String> agentIds = List.of(testAgentId);
        when(mapResponseDao.selectApplicationResponse(any(), any())).thenReturn(applicationResponse);
        List<AgentInfo> nullAgentInfoList = new ArrayList<>();
        nullAgentInfoList.add(null);
        when(agentInfoDao.getSimpleAgentInfos(agentIds, range.getTo())).thenReturn(nullAgentInfoList);

        List<AgentAndStatus> agentAndStatusList = applicationAgentListService.activeStatisticsAgentList(testApplicationName, testApplicationServiceType, timeWindow, AgentInfoFilters.acceptAll());

        Assertions.assertThat(agentAndStatusList).hasSize(1);
        AgentAndStatus agentAndStatus = agentAndStatusList.get(0);
        Assertions.assertThat(agentAndStatus.getAgentInfo()).isNotNull();
        Assertions.assertThat(agentAndStatus.getAgentInfo().getAgentId()).isEqualTo(testAgentId);
        Assertions.assertThat(agentAndStatus.getAgentInfo().getServiceType()).isNotNull();
        Assertions.assertThat(agentAndStatus.getAgentInfo().getHostName()).isNotBlank();

        Assertions.assertThat(agentAndStatus.getStatus().getState()).isEqualTo(AgentLifeCycleState.RUNNING);
    }
}
