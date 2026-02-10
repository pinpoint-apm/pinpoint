/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
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
import java.util.Set;

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

    @Mock
    AgentInfoDao agentInfoDao;

    @Mock
    AgentLifeCycleDao agentLifeCycleDao;

    @Mock
    MapAgentResponseDao mapAgentResponseDao;

    @Mock
    ApplicationIndexService applicationIndexService;

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

        applicationAgentListService = new ApplicationAgentListServiceImpl(agentInfoDao, agentLifeCycleDao, mapAgentResponseDao, applicationIndexService);
    }

    @Test
    public void allAgentListTest() {
        Range range = Range.between(0, 60_000);
        List<String> agentIds = List.of(testAgentId);
        when(applicationIndexService.selectAgentIds(testApplicationName)).thenReturn(agentIds);
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
        when(applicationIndexService.selectAgentIds(testApplicationName)).thenReturn(agentIds);
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
        when(applicationIndexService.selectAgentIds(testApplicationName)).thenReturn(agentIds);
        when(agentInfoDao.getSimpleAgentInfos(agentIds, range.getTo())).thenReturn(List.of(testAgentInfo));
        when(agentLifeCycleDao.getAgentStatus((AgentStatusQuery) any())).thenReturn(List.of(Optional.of(testAgentStatus)));

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
        when(applicationIndexService.selectAgentIds(testApplicationName)).thenReturn(agentIds);
        when(agentInfoDao.getSimpleAgentInfos(agentIds, range.getTo())).thenReturn(List.of(testAgentInfo));
        when(agentLifeCycleDao.getAgentStatus((AgentStatusQuery) any())).thenReturn(List.of(Optional.of(testAgentStatus)));

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
        when(mapAgentResponseDao.selectAgentIds(any(), any())).thenReturn(Set.of(testAgentId));
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
        when(applicationIndexService.selectApplication(testApplicationName)).thenReturn(List.of(testApplication));
        when(mapAgentResponseDao.selectAgentIds(any(), any())).thenReturn(Set.of(testAgentId));
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
        when(applicationIndexService.selectAgentIds(testApplicationName)).thenReturn(agentIds);

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
        when(mapAgentResponseDao.selectAgentIds(any(), any())).thenReturn(Set.of(testAgentId));

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
