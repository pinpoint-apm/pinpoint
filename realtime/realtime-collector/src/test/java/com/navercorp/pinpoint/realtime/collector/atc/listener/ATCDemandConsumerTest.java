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
package com.navercorp.pinpoint.realtime.collector.atc.listener;

import com.navercorp.pinpoint.collector.realtime.atc.listener.ActiveThreadCountDemandConsumer;
import com.navercorp.pinpoint.collector.realtime.atc.service.ActiveThreadCountService;
import com.navercorp.pinpoint.collector.realtime.atc.service.SupplyPublishService;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.atc.dto.ATCDemand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class ATCDemandConsumerTest {

    @Mock ActiveThreadCountService activeThreadCountService;
    @Mock SupplyPublishService supplyPublishService;
    @Captor ArgumentCaptor<Consumer<List<Integer>>> consumerCaptor;

    @Test
    public void testAccept() throws Exception {
        final ActiveThreadCountDemandConsumer consumer =
                new ActiveThreadCountDemandConsumer(activeThreadCountService, supplyPublishService);

        final String applicationName = "test-application";
        final String agentId = "test-agent";
        final long startTimestamp = 12345;

        final ATCDemand demand = new ATCDemand();
        demand.setApplicationName(applicationName);
        demand.setAgentId(agentId);
        demand.setStartTimestamp(startTimestamp);

        consumer.consume(demand, null);

        final ClusterKey clusterKey = new ClusterKey(applicationName, agentId, startTimestamp);

        verify(activeThreadCountService).requestAsync(eq(clusterKey), consumerCaptor.capture());
        final Consumer<List<Integer>> capturedConsumer = consumerCaptor.getValue();

        final Random random = new Random();
        for (int i = 0; i < 10; i++) {
            final List<Integer> testValueList = List.of(random.nextInt(), random.nextInt(), random.nextInt());
            capturedConsumer.accept(testValueList);
        }

        verify(supplyPublishService, times(10)).publish(eq(clusterKey), any());
    }

}
