/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.web.realtime.atc.service;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.pubsub.PubChannel;
import com.navercorp.pinpoint.realtime.atc.dto.ATCDemand;
import com.navercorp.pinpoint.web.realtime.atc.dao.ATCValueDao;
import com.navercorp.pinpoint.web.realtime.atc.dao.CountingMetricDao;
import com.navercorp.pinpoint.web.realtime.atc.dao.memory.ATCSessionRepository;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class DemandPublishServiceImplTest {

    @Mock PubChannel<ATCDemand> pubChannel;
    @Mock AgentLookupService agentLookupService;
    @Mock ATCValueDao valueDao;
    @Mock
    CountingMetricDao countingMetricDao;
    @Mock
    ATCSessionRepository sessionRepository;

    @Captor ArgumentCaptor<ATCDemand> demandCaptor;

    @Test
    public void testDemandSingle() {
        final DemandPublishServiceImpl service = new DemandPublishServiceImpl(
                pubChannel, agentLookupService, valueDao, countingMetricDao, sessionRepository);

        final String applicationName = "test-application";
        final String agentId = "test-agent";
        final long startTimestamp = 12345;
        final ClusterKey clusterKey = new ClusterKey(applicationName, agentId, startTimestamp);

        service.demand(clusterKey);

        verify(countingMetricDao, times(1)).incrementCountATCDemand();
        verify(pubChannel, times(1)).publish(demandCaptor.capture(), isNull());
        final ATCDemand demand = demandCaptor.getValue();

        assertThat(demand.getApplicationName()).isEqualTo(applicationName);
        assertThat(demand.getAgentId()).isEqualTo(agentId);
        assertThat(demand.getStartTimestamp()).isEqualTo(startTimestamp);
    }

}
