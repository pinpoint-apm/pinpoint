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

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.collector.service.AgentCommandService;
import com.navercorp.pinpoint.realtime.dto.ATDDemand;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;
import com.navercorp.pinpoint.realtime.dto.mapper.grpc.GrpcDtoMapper;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class ActiveThreadDumpServiceImpl implements ActiveThreadDumpService {

    private final AgentCommandService commandService;

    ActiveThreadDumpServiceImpl(AgentCommandService commandService) {
        this.commandService = Objects.requireNonNull(commandService, "commandService");
    }

    @Override
    public Mono<ATDSupply> getDump(ATDDemand demand) {
        final ClusterKey clusterKey = demand.getClusterKey();
        final GeneratedMessageV3 command = GrpcDtoMapper.buildGeneratedMessage(demand);
        final Mono<GeneratedMessageV3> responseMono = this.commandService.request(clusterKey, command);
        if (responseMono == null) {
            return null;
        }
        return responseMono.mapNotNull(GrpcDtoMapper::buildATDSupply);
    }

}
