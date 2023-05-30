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

import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.realtime.collector.service.AgentCommandService;
import com.navercorp.pinpoint.realtime.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class ActiveThreadCountServiceTest {

    private static final String APPLICATION_NAME = "test-application";
    private static final String AGENT_NAME = "test-agent";
    private static final int START_TIMESTAMP = 1234;

    @Mock
    private AgentCommandService agentCommandService;


    @BeforeEach
    public void beforeEach() {
        final PCmdActiveThreadCountRes res = PCmdActiveThreadCountRes.newBuilder()
                .setHistogramSchemaType(0)
                .addAllActiveThreadCount(List.of(0, 0, 0, 1))
                .setTimeStamp(4567)
                .build();
        when(agentCommandService.requestStream(any(), any(), anyLong())).thenReturn(Flux.just(res, res, res, res));
    }

    @Test
    public void shouldDeserializeSuccessfully() {
        final ActiveThreadCountServiceImpl service = new ActiveThreadCountServiceImpl(agentCommandService, 10000, 100);

        final ATCDemand demand = new ATCDemand();
        demand.setApplicationName(APPLICATION_NAME);
        demand.setAgentId(AGENT_NAME);
        demand.setStartTimestamp(START_TIMESTAMP);

        final List<ATCSupply> supplies = service.requestAsync(demand).collectList().block();

        assertThat(supplies).isNotNull().hasSizeGreaterThanOrEqualTo(2);
        for (final ATCSupply supply: supplies) {
            assertThat(supply.getApplicationName()).isEqualTo(APPLICATION_NAME);
            assertThat(supply.getAgentId()).isEqualTo(AGENT_NAME);
            assertThat(supply.getStartTimestamp()).isEqualTo(START_TIMESTAMP);
            if (supply.getMessage() == ATCSupply.Message.OK) {
                assertThat(supply.getValues()).hasSameElementsAs(List.of(0, 0, 0, 1));
            }
        }
    }

}
