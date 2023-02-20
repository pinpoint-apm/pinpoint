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
package com.navercorp.pinpoint.realtime.collector.atc.service.redis;

import com.navercorp.pinpoint.collector.realtime.atc.dao.CountingMetricDao;
import com.navercorp.pinpoint.collector.realtime.atc.service.redis.RedisSupplyPublishService;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.pubsub.PubChannel;
import com.navercorp.pinpoint.realtime.atc.dto.ATCSupply;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class RedisSupplyPublishServiceTest {

    @Mock PubChannel<ATCSupply> supplyChannel;
    @Mock CountingMetricDao countingMetricDao;

    @Captor ArgumentCaptor<ATCSupply> supplyCaptor;

    @Test
    public void testPublish() {
        final RedisSupplyPublishService service = new RedisSupplyPublishService(supplyChannel, countingMetricDao, 0);

        final String applicationName = "test-application";
        final String agentId = "test-agent";
        final long startTimestamp = 12345;
        final ClusterKey clusterKey = new ClusterKey(applicationName, agentId, startTimestamp);

        final List<Integer> values = List.of(3, 6, 9);

        service.publish(clusterKey, values);

        verify(supplyChannel, times(1)).publish(supplyCaptor.capture(), any());
        verify(countingMetricDao, times(1)).incrementCountATCSupply();

        final ATCSupply supply = supplyCaptor.getValue();
        assertThat(supply.getAgentId()).isEqualTo(agentId);
        assertThat(supply.getStartTimestamp()).isEqualTo(startTimestamp);
        assertThat(supply.getValues()).isEqualTo(values);
    }

}
