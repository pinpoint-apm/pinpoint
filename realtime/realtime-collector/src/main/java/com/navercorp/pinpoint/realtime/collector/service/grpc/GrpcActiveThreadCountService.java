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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.navercorp.pinpoint.channel.service.server.IgnoreDemandException;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCount;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcAgentConnection;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcAgentConnectionRepository;
import com.navercorp.pinpoint.realtime.collector.service.ActiveThreadCountService;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import com.navercorp.pinpoint.realtime.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.navercorp.pinpoint.realtime.collector.RealtimeUtils.COLLECTOR_ID;

/**
 * @author youngjin.kim2
 */
class GrpcActiveThreadCountService implements ActiveThreadCountService {

    private final Logger logger = LogManager.getLogger(GrpcActiveThreadCountService.class);

    private final GrpcAgentConnectionRepository connectionRepository;
    private final SinkRepository<FluxSink<PCmdActiveThreadCountRes>> sinkRepository;
    private final Duration demandDuration;

    private final Cache<ClusterKey, Flux<ATCSupply>> fluxCache = CacheBuilder.newBuilder()
            .initialCapacity(32)
            .maximumSize(1024)
            .expireAfterAccess(Duration.ofMinutes(5))
            .build();

    GrpcActiveThreadCountService(
            GrpcAgentConnectionRepository connectionRepository,
            SinkRepository<FluxSink<PCmdActiveThreadCountRes>> sinkRepository,
            Duration demandDuration
    ) {
        this.connectionRepository = Objects.requireNonNull(connectionRepository, "connectionRepository");
        this.sinkRepository = Objects.requireNonNull(sinkRepository, "sinkRepository");
        this.demandDuration = Objects.requireNonNullElse(demandDuration, Duration.ofSeconds(15));
    }

    @Override
    public Flux<ATCSupply> requestAsync(ATCDemand demand) {
        try {
            ClusterKey clusterKey = new ClusterKey(
                    demand.getApplicationName(),
                    demand.getAgentId(),
                    demand.getStartTimestamp());

            return this.fluxCache.get(clusterKey, () -> this.buildFlux(clusterKey)).take(this.demandDuration);
        } catch (ExecutionException e) {
            logger.error("Failed to request atc", e);
            throw new RuntimeException(e);
        }
    }

    private Flux<ATCSupply> buildFlux(ClusterKey clusterKey) {
        return Flux.<PCmdActiveThreadCountRes>create(sink -> {
                    GrpcAgentConnection conn = this.connectionRepository.getConnection(clusterKey);
                    if (conn == null) {
                        sink.error(new IgnoreDemandException("Connection not found"));
                        return;
                    }
                    if (!conn.getSupportCommandList().contains(TCommandType.ACTIVE_THREAD_COUNT)) {
                        sink.error(new RuntimeException("Command not supported"));
                        return;
                    }

                    long sinkId = this.sinkRepository.put(sink);
                    sink.onDispose(() -> {
                        this.sinkRepository.invalidate(sinkId);
                    });
                    conn.request(PCmdRequest.newBuilder()
                            .setRequestId((int) sinkId)
                            .setCommandActiveThreadCount(PCmdActiveThreadCount.newBuilder())
                            .build());
                })
                .map(res -> intoSupply(clusterKey, res))
                .onBackpressureDrop()
                .publish(1)
                .refCount(1, Duration.ofSeconds(5));
    }

    private static ATCSupply intoSupply(ClusterKey clusterKey, PCmdActiveThreadCountRes res) {
        ATCSupply supply = new ATCSupply();
        supply.setMessage(ATCSupply.Message.OK);
        supply.setValues(res.getActiveThreadCountList());
        supply.setCollectorId(COLLECTOR_ID);
        supply.setApplicationName(clusterKey.getApplicationName());
        supply.setAgentId(clusterKey.getAgentId());
        supply.setStartTimestamp(clusterKey.getStartTimestamp());
        return supply;
    }

}
