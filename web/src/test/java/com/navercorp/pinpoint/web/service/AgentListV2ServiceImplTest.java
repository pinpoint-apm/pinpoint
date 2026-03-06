/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.config.AgentListProperties;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static com.navercorp.pinpoint.common.server.util.AgentLifeCycleState.RUNNING;
import static com.navercorp.pinpoint.common.server.util.AgentLifeCycleState.UNKNOWN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AgentListV2ServiceImplTest {

    @Mock
    AgentListProperties agentListProperties;

    @Mock
    private AgentIdDao agentIdDao;

    @Mock
    private MapAgentResponseDao mapAgentResponseDao;

    AgentListV2ServiceImpl agentListV2Service;

    @BeforeEach
    public void setup() {
        when(agentListProperties.getFilterStatisticsExistenceServiceTypeCodes()).thenReturn(Set.of());
        when(agentListProperties.getFilterLastStatusExcludeServiceTypeCodes()).thenReturn(Set.of());
        agentListV2Service = new AgentListV2ServiceImpl(agentListProperties, agentIdDao, mapAgentResponseDao);
    }

    @Test
    public void filterStatusExcludeTest() {
        long currentTime = System.currentTimeMillis();
        long from = currentTime - Duration.ofMinutes(25).toMillis(); // 25 minutes ago
        long to = currentTime - Duration.ofMinutes(5).toMillis(); // 5 minutes ago
        Range range = Range.between(from, to);
        Application testApplication = new Application("testApp", ServiceType.JAVA);

        String agentId = "testAgent";
        String agentId2 = "otherAgent";
        long agentStartTime = currentTime - Duration.ofHours(1).toMillis();
        when(agentListProperties.getFilterLastStatusExcludeServiceTypeCodes()).thenReturn(Set.of((int) ServiceType.JAVA.getCode()));
        when(agentIdDao.getAgentIdEntry(anyInt(), any(), anyInt())).thenReturn(List.of(
                new AgentIdEntry(testApplication, agentId, agentStartTime, null, RUNNING, currentTime),
                new AgentIdEntry(testApplication, agentId2, agentStartTime, null, RUNNING, agentStartTime)
        ));

        List<AgentIdEntry> agentList = agentListV2Service.getAgentList(ServiceUid.DEFAULT, testApplication.getApplicationName(), testApplication.getServiceType(), range);

        verify(agentIdDao, times(1)).getAgentIdEntry(anyInt(), any(), anyInt());
        Assertions.assertThat(agentList).hasSize(2);
    }

    @Test
    public void filterStartTimeTest() {
        long currentTime = System.currentTimeMillis();
        long from = currentTime - Duration.ofMinutes(25).toMillis(); // 25 minutes ago
        long to = currentTime - Duration.ofMinutes(5).toMillis(); // 5 minutes ago
        Range range = Range.between(from, to);
        Application testApplication = new Application("testApp", ServiceType.JAVA);

        String agentId = "testAgent";
        String agentId2 = "toBeFiltered";
        long agentStartTime = currentTime - Duration.ofHours(1).toMillis();
        long newAgentStartTime = to + Duration.ofMinutes(5).toMillis(); // starts after the 'to' time
        when(agentIdDao.getAgentIdEntryByMinStatusTimestamp(anyInt(), any(), anyInt(), anyLong())).thenReturn(List.of(
                new AgentIdEntry(testApplication, agentId, agentStartTime, null, RUNNING, currentTime),
                new AgentIdEntry(testApplication, agentId2, newAgentStartTime, null, RUNNING, currentTime)
        ));

        List<AgentIdEntry> agentList = agentListV2Service.getAgentList(ServiceUid.DEFAULT, testApplication.getApplicationName(), testApplication.getServiceType(), range);

        Assertions.assertThat(agentList).hasSize(1);
        Assertions.assertThat(agentList.get(0).getAgentId()).isEqualTo(agentId);
    }

    @Test
    public void dedupeAgentIdTest() {
        long currentTime = System.currentTimeMillis();
        long from = currentTime - Duration.ofMinutes(25).toMillis(); // 25 minutes ago
        long to = currentTime - Duration.ofMinutes(5).toMillis(); // 5 minutes ago
        Range range = Range.between(from, to);
        Application testApplication = new Application("testApp", ServiceType.JAVA);

        String agentId = "testAgent";
        long agentStartTime = currentTime - Duration.ofHours(1).toMillis();
        long previousAgentStartTime = currentTime - Duration.ofHours(4).toMillis(); // 4 hours ago
        when(agentIdDao.getAgentIdEntryByMinStatusTimestamp(anyInt(), any(), anyInt(), anyLong())).thenReturn(List.of(
                new AgentIdEntry(testApplication, agentId, agentStartTime, null, RUNNING, currentTime),
                new AgentIdEntry(testApplication, agentId, previousAgentStartTime, null, RUNNING, currentTime)
        ));

        List<AgentIdEntry> agentList = agentListV2Service.getAgentList(ServiceUid.DEFAULT, testApplication.getApplicationName(), testApplication.getServiceType(), range);

        Assertions.assertThat(agentList).hasSize(1);
        Assertions.assertThat(agentList.get(0).getAgentId()).isEqualTo(agentId);
    }

    @Test
    public void filterStatisticExistenceTest() {
        long currentTime = System.currentTimeMillis();
        long from = currentTime - Duration.ofMinutes(25).toMillis(); // 25 minutes ago
        long to = currentTime - Duration.ofMinutes(5).toMillis(); // 5 minutes ago
        Range range = Range.between(from, to);
        Application testApplication = new Application("testApp", ServiceType.JAVA);

        String agentId = "testAgent";
        String agentId2 = "toBeFiltered";
        long agentStartTime = currentTime - Duration.ofHours(1).toMillis();
        when(mapAgentResponseDao.selectAgentIds(any(), any())).thenReturn(Set.of(agentId)); // only agentId has statistics
        when(agentListProperties.getFilterStatisticsExistenceServiceTypeCodes()).thenReturn(Set.of(testApplication.getServiceTypeCode())); // enable statistics existence filtering for the service type
        when(agentIdDao.getAgentIdEntryByMinStatusTimestamp(anyInt(), any(), anyInt(), anyLong())).thenReturn(List.of(
                new AgentIdEntry(testApplication, agentId, agentStartTime, null, RUNNING, agentStartTime),
                new AgentIdEntry(testApplication, agentId2, agentStartTime, null, UNKNOWN, 0)
        ));

        List<AgentIdEntry> agentList = agentListV2Service.getAgentList(ServiceUid.DEFAULT, testApplication.getApplicationName(), testApplication.getServiceType(), range);

        Assertions.assertThat(agentList).hasSize(1);
        Assertions.assertThat(agentList.get(0).getAgentId()).isEqualTo(agentId);
    }
}
