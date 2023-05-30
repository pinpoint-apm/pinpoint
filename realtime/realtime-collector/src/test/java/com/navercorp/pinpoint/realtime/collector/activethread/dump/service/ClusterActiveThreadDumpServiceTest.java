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
package com.navercorp.pinpoint.realtime.collector.activethread.dump.service;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.realtime.collector.service.AgentCommandService;
import com.navercorp.pinpoint.realtime.dto.ATDDemand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class ClusterActiveThreadDumpServiceTest {

    @Mock AgentCommandService agentCommandService;

    @Test
    public void shouldCallAgentCommandService() {
        final ActiveThreadDumpService service = new ActiveThreadDumpServiceImpl(agentCommandService);

        when(agentCommandService.request(any(), any())).thenReturn(Mono.just(PCmdActiveThreadLightDumpRes.newBuilder().build()));

        final ATDDemand demand = new ATDDemand();
        demand.setClusterKey(ClusterKey.parse("test-application:test-agent:12345"));
        demand.setThreadNameList(List.of());
        demand.setLocalTraceIdList(List.of());
        demand.setLight(true);
        demand.setLimit(100);

        service.getDump(demand).subscribe();

        verify(agentCommandService).request(any(), any());
    }

}
