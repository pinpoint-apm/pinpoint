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

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.grpc.trace.PCmdEcho;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.realtime.collector.service.AgentCommandService;
import com.navercorp.pinpoint.realtime.dto.Echo;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class EchoServiceImpl implements EchoService {

    private final AgentCommandService commandService;

    EchoServiceImpl(AgentCommandService commandService) {
        this.commandService = Objects.requireNonNull(commandService, "commandService");
    }

    @Override
    public Mono<Echo> echo(Echo echo) {
        final PCmdEcho command = PCmdEcho.newBuilder().setMessage(echo.getMessage()).build();
        final Mono<GeneratedMessageV3> response = this.commandService.request(echo.getAgentKey(), command);
        if (response == null) {
            return null;
        }
        return response.map(res -> compose(res, echo.getAgentKey()));
    }

    private static Echo compose(GeneratedMessageV3 res, ClusterKey clusterKey) {
        if (res instanceof PCmdEchoResponse) {
            final PCmdEchoResponse echo = (PCmdEchoResponse) res;
            return new Echo(clusterKey, echo.getMessage());
        }
        throw new RuntimeException("Failed to parse echo");
    }

}
