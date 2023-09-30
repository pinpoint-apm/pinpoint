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
package com.navercorp.pinpoint.realtime.collector.service.grpc;

import com.navercorp.pinpoint.channel.service.server.IgnoreDemandException;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.realtime.collector.mapper.ATDDemandMapper;
import com.navercorp.pinpoint.realtime.collector.mapper.ATDSupplyMapper;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcAgentConnection;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcAgentConnectionRepository;
import com.navercorp.pinpoint.realtime.collector.service.ActiveThreadDumpService;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import com.navercorp.pinpoint.realtime.dto.ATDDemand;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class GrpcActiveThreadDumpService implements ActiveThreadDumpService {

    private final GrpcAgentConnectionRepository connectionRepository;
    private final SinkRepository<MonoSink<PCmdActiveThreadDumpRes>> sinkRepository;
    private final SinkRepository<MonoSink<PCmdActiveThreadLightDumpRes>> lightSinkRepository;

    GrpcActiveThreadDumpService(
            GrpcAgentConnectionRepository connectionRepository,
            SinkRepository<MonoSink<PCmdActiveThreadDumpRes>> sinkRepository,
            SinkRepository<MonoSink<PCmdActiveThreadLightDumpRes>> lightSinkRepository
    ) {
        this.connectionRepository = Objects.requireNonNull(connectionRepository, "connectionRepository");
        this.sinkRepository = Objects.requireNonNull(sinkRepository, "sinkRepository");
        this.lightSinkRepository = Objects.requireNonNull(lightSinkRepository, "lightSinkRepository");
    }

    @Override
    public Mono<ATDSupply> getDump(ATDDemand demand) {
        if (demand.isLight()) {
            return getLightDump(demand);
        } else {
            return getDetailedDump(demand);
        }
    }

    private Mono<ATDSupply> getDetailedDump(ATDDemand demand) {
        assert !demand.isLight();

        return Mono.<PCmdActiveThreadDumpRes>create(sink -> {
            GrpcAgentConnection conn = this.connectionRepository.getConnection(demand.getClusterKey());
            if (conn == null) {
                sink.error(new IgnoreDemandException("Connection not found"));
                return;
            }
            if (!conn.getSupportCommandList().contains(TCommandType.ACTIVE_THREAD_DUMP)) {
                sink.error(new RuntimeException("Command not supported"));
                return;
            }

            long sinkId = this.sinkRepository.put(sink);
            conn.request(PCmdRequest.newBuilder()
                    .setRequestId(Long.valueOf(sinkId).intValue())
                    .setCommandActiveThreadDump(ATDDemandMapper.intoDetailed(demand))
                    .build());
        }).map(ATDSupplyMapper::from);
    }

    private Mono<ATDSupply> getLightDump(ATDDemand demand) {
        assert demand.isLight();

        return Mono.<PCmdActiveThreadLightDumpRes>create(sink -> {
            GrpcAgentConnection conn = this.connectionRepository.getConnection(demand.getClusterKey());
            if (conn == null) {
                sink.error(new IgnoreDemandException("Connection not found"));
                return;
            }
            if (!conn.getSupportCommandList().contains(TCommandType.ACTIVE_THREAD_LIGHT_DUMP)) {
                sink.error(new RuntimeException("Command not supported"));
                return;
            }

            long sinkId = this.lightSinkRepository.put(sink);
            conn.request(PCmdRequest.newBuilder()
                    .setRequestId(Long.valueOf(sinkId).intValue())
                    .setCommandActiveThreadLightDump(ATDDemandMapper.intoLight(demand))
                    .build());
        }).map(ATDSupplyMapper::from);
    }

}
