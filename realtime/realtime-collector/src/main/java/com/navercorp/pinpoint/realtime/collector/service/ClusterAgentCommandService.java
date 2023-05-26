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

import com.navercorp.pinpoint.collector.cluster.ClusterPoint;
import com.navercorp.pinpoint.collector.cluster.GrpcAgentConnection;
import com.navercorp.pinpoint.collector.cluster.route.StreamRouteHandler;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.realtime.util.ScheduleUtil;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;
import com.navercorp.pinpoint.rpc.stream.StreamException;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
class ClusterAgentCommandService implements AgentCommandService {

    private static final Logger logger = LogManager.getLogger(ClusterAgentCommandService.class);

    private final ScheduledExecutorService closer = ScheduleUtil.makeScheduledExecutorService("closer");

    private final StreamRouteHandler streamRouteHandler;
    private final SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;
    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

    public ClusterAgentCommandService(
            StreamRouteHandler streamRouteHandler,
            SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory,
            DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory
    ) {
        this.streamRouteHandler = Objects.requireNonNull(streamRouteHandler, "streamRouteHandler");
        this.commandSerializerFactory = Objects.requireNonNull(commandSerializerFactory, "commandSerializerFactory");
        this.deserializerFactory = Objects.requireNonNull(deserializerFactory, "deserializerFactory");
    }

    @Override
    public Flux<TBase<?, ?>> requestStream(
            ClusterKey clusterKey,
            TBase<?, ?> command,
            long durationMillis
    ) {
        try {
            return requestStream0(clusterKey, command, durationMillis);
        } catch (Exception e) {
            throw new RuntimeException("Failed to requestStream", e);
        }
    }

    public Flux<TBase<?, ?>> requestStream0(
            ClusterKey clusterKey,
            TBase<?, ?> command,
            long durationMillis
    ) throws Exception {
        final ClusterPoint<?> clusterPoint = findClusterPoint(clusterKey);
        if (clusterPoint == null) {
            return null;
        }

        final Sinks.Many<TBase<?, ?>> sink = Sinks.many().multicast().onBackpressureBuffer(4);
        final ClientStreamChannelEventHandler handler = new ClientStreamCallbackAdapter(sink);
        final ClientStreamChannel channel = openClientStreamChannel(clusterPoint, handler, command);

        if (durationMillis > 0 && durationMillis < Long.MAX_VALUE) {
            final Closer closer = new Closer(handler, channel);
            this.closer.schedule(closer, durationMillis, TimeUnit.MILLISECONDS);
        }

        return sink.asFlux();
    }

    @Override
    public Mono<TBase<?, ?>> request(ClusterKey clusterKey, TBase<?, ?> command) {
        try {
            return request0(clusterKey, command);
        } catch (Exception e) {
            throw new RuntimeException("Failed to request", e);
        }
    }

    private Mono<TBase<?, ?>> request0(ClusterKey clusterKey, TBase<?, ?> command) throws Exception {
        final ClusterPoint<?> clusterPoint = findClusterPoint(clusterKey);
        if (clusterPoint == null) {
            return null;
        }

        if (!clusterPoint.isSupportCommand(command)) {
            throw new RuntimeException("Unsupported command: " + command);
        }

        final CompletableFuture<ResponseMessage> future = request(clusterPoint, command);
        return Mono.fromFuture(future)
                .map(ResponseMessage::getMessage)
                .flatMap(this::deserializeMono);
    }

    private CompletableFuture<ResponseMessage> request(ClusterPoint<?> clusterPoint, TBase<?, ?> command) throws TException {
        if (clusterPoint instanceof GrpcAgentConnection) {
            final GrpcAgentConnection grpcPoint = (GrpcAgentConnection) clusterPoint;
            return grpcPoint.request(command);
        }

        throw new RuntimeException("Invalid clusterPoint: " + clusterPoint);
    }

    private byte[] serialize(TBase<?, ?> command) throws TException {
        return commandSerializerFactory.createSerializer().serialize(command);
    }

    private ClusterPoint<?> findClusterPoint(ClusterKey clusterKey) {
        return streamRouteHandler.findClusterPoint(clusterKey);
    }

    private TBase<?, ?> deserialize(byte[] bytes) {
        final Message<TBase<?, ?>> des = SerializationUtils.deserialize(bytes, deserializerFactory, null);
        if (des == null) {
            return null;
        }
        return des.getData();
    }

    private Mono<TBase<?, ?>> deserializeMono(byte[] bytes) {
        final TBase<?, ?> data = deserialize(bytes);
        if (data != null) {
            return Mono.just(data);
        }
        return Mono.empty();
    }

    private ClientStreamChannel openClientStreamChannel(
            ClusterPoint<?> clusterPoint,
            ClientStreamChannelEventHandler handler,
            TBase<?, ?> command
    ) throws StreamException {
        if (!clusterPoint.isSupportCommand(command)) {
            throw new RuntimeException("Unsupported command: " + command);
        }

        if (clusterPoint instanceof GrpcAgentConnection) {
            return ((GrpcAgentConnection) clusterPoint).openStream(command, handler);
        }

        throw new RuntimeException("Invalid clusterPoint: " + clusterPoint);
    }

    private class ClientStreamCallbackAdapter extends ClientStreamChannelEventHandler {

        private final Sinks.Many<TBase<?, ?>> sink;

        public ClientStreamCallbackAdapter(Sinks.Many<TBase<?, ?>> sink) {
            this.sink = sink;
        }

        @Override
        public void handleStreamResponsePacket(ClientStreamChannel streamChannel, StreamResponsePacket packet) {
            final TBase<?, ?> item = deserialize(packet.getPayload());
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
