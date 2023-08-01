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
package com.navercorp.pinpoint.realtime.collector.service;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.realtime.dto.mapper.ThriftToGrpcConverter;
import com.navercorp.pinpoint.realtime.util.ScheduleUtil;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
class ClusterAgentCommandService implements AgentCommandService {

    private static final Logger logger = LogManager.getLogger(ClusterAgentCommandService.class);

    private final ScheduledExecutorService closer = ScheduleUtil.makeScheduledExecutorService("closer");

    private final AgentConnectionRepository agentConnectionRepository;
    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

    public ClusterAgentCommandService(
            AgentConnectionRepository agentConnectionRepository,
            DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory
    ) {
        this.agentConnectionRepository = Objects.requireNonNull(agentConnectionRepository, "agentConnectionRepository");
        this.deserializerFactory = Objects.requireNonNull(deserializerFactory, "deserializerFactory");
    }

    @Override
    public Flux<GeneratedMessageV3> requestStream(
            ClusterKey clusterKey,
            GeneratedMessageV3 command,
            long durationMillis
    ) {
        try {
            return requestStream0(clusterKey, command, durationMillis);
        } catch (Exception e) {
            throw new RuntimeException("Failed to requestStream", e);
        }
    }

    public Flux<GeneratedMessageV3> requestStream0(
            ClusterKey clusterKey,
            GeneratedMessageV3 command,
            long durationMillis
    ) {
        AgentConnection conn = this.agentConnectionRepository.getConnection(clusterKey);
        if (conn == null) {
            return null;
        }

        final Sinks.Many<GeneratedMessageV3> sink = Sinks.many().multicast().onBackpressureBuffer(4);
        final ClientStreamChannelEventHandler handler = new ClientStreamCallbackAdapter(sink);
        final ClientStreamChannel channel = conn.requestStream(handler, command);

        if (durationMillis > 0 && durationMillis < Long.MAX_VALUE) {
            final Closer closer = new Closer(handler, channel);
            this.closer.schedule(closer, durationMillis, TimeUnit.MILLISECONDS);
        }

        return sink.asFlux();
    }

    @Override
    public Mono<GeneratedMessageV3> request(ClusterKey clusterKey, GeneratedMessageV3 command) {
        try {
            return request0(clusterKey, command);
        } catch (Exception e) {
            throw new RuntimeException("Failed to request", e);
        }
    }

    private Mono<GeneratedMessageV3> request0(ClusterKey clusterKey, GeneratedMessageV3 command) throws TException {
        AgentConnection conn = this.agentConnectionRepository.getConnection(clusterKey);
        if (conn == null) {
            return null;
        }

        return Mono.fromFuture(conn.request(command))
                .map(ResponseMessage::getMessage)
                .mapNotNull(this::deserialize);
    }

    private GeneratedMessageV3 deserialize(byte[] bytes) {
        final Message<TBase<?, ?>> des = SerializationUtils.deserialize(bytes, deserializerFactory, null);
        if (des == null) {
            return null;
        }
        return ThriftToGrpcConverter.convert(des.getData());
    }

    private class ClientStreamCallbackAdapter extends ClientStreamChannelEventHandler {

        private final Sinks.Many<GeneratedMessageV3> sink;

        public ClientStreamCallbackAdapter(Sinks.Many<GeneratedMessageV3> sink) {
            this.sink = sink;
        }

        @Override
        public void handleStreamResponsePacket(ClientStreamChannel streamChannel, StreamResponsePacket packet) {
            final GeneratedMessageV3 item = deserialize(packet.getPayload());
            if (item != null) {
                sink.emitNext(item, Sinks.EmitFailureHandler.FAIL_FAST);
            }
        }

        @Override
        public void handleStreamClosePacket(ClientStreamChannel streamChannel, StreamClosePacket packet) {
            logger.info("Emit Complete");
            sink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        }

        @Override
        public void stateUpdated(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode) {
        }

    }

    private static class Closer implements Runnable {
        private final ClientStreamChannelEventHandler handler;
        private final ClientStreamChannel channel;

        Closer(
                ClientStreamChannelEventHandler handler,
                ClientStreamChannel channel
        ) {
            this.handler = handler;
            this.channel = channel;
        }

        @Override
        public void run() {
            this.channel.close();
            this.handler.handleStreamClosePacket(this.channel, null);
        }

    }

}
