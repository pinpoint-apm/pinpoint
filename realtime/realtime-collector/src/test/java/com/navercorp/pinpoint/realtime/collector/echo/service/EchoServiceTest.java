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
package com.navercorp.pinpoint.realtime.collector.echo.service;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.realtime.collector.service.AgentCommandService;
import com.navercorp.pinpoint.realtime.dto.Echo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class EchoServiceTest {

    private static final String APPLICATION_NAME = "test-application";
    private static final String AGENT_NAME = "test-agent";
    private static final int START_TIMESTAMP = 1234;
    private static final ClusterKey CLUSTER_KEY = new ClusterKey(APPLICATION_NAME, AGENT_NAME, START_TIMESTAMP);
    private static final String MESSAGE = "hello";

    @Mock
    private AgentCommandService agentCommandService;

    @BeforeEach
    public void beforeEach() {
        when(agentCommandService.request(eq(CLUSTER_KEY), any())).thenReturn(Mono.just(PCmdEchoResponse.newBuilder()
                .setMessage(MESSAGE)
                .build()));
    }

    @Test
    public void shouldDeserializeSuccessfully() {
        final EchoServiceImpl service = new EchoServiceImpl(agentCommandService);
        final Echo actualResponse = service.echo(new Echo(CLUSTER_KEY, MESSAGE)).block();

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getMessage()).isEqualTo(MESSAGE);
        assertThat(actualResponse.getAgentKey()).isEqualTo(CLUSTER_KEY);
    }

}
