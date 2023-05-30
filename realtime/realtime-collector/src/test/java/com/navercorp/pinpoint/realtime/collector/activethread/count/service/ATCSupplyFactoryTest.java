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
package com.navercorp.pinpoint.realtime.collector.activethread.count.service;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class ATCSupplyFactoryTest {

    @Test
    public void shouldBuildSupply() {
        final ATCSupplyFactory factory = new ATCSupplyFactory(0);

        final String applicationName = "test-application";
        final String agentId = "test-agent";
        final long startTimestamp = 12345;
        final ClusterKey clusterKey = new ClusterKey(applicationName, agentId, startTimestamp);

        final List<Integer> values = List.of(3, 6, 9);

        final ATCSupply supply = factory.build(clusterKey, values);

        assertThat(supply.getAgentId()).isEqualTo(agentId);
        assertThat(supply.getStartTimestamp()).isEqualTo(startTimestamp);
        assertThat(supply.getValues()).isEqualTo(values);
    }

}
