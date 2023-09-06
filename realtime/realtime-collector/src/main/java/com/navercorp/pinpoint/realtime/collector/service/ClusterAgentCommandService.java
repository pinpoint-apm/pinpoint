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
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class ClusterAgentCommandService implements AgentCommandService {

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
    public Flux<GeneratedMessageV3> requestStream(ClusterKey clusterKey, GeneratedMessageV3 command) {
        try {
            return requestStream0(clusterKey, command);
        } catch (Exception e) {
            throw new RuntimeException("Failed to requestStream", e);
        }
    }

    public Flux<GeneratedMessageV3> requestStream0(ClusterKey clusterKey, GeneratedMessageV3 command) {
        AgentConnection conn = this.agentConnectionRepository.getConnection(clusterKey);
        if (conn == null) {
            return null;
        }

        return Flux.create(sink -> {
            final ClientStreamChannelEventHandler handler = new ClientStreamCallbackAdapter(sink);
            final ClientStreamChannel channel = conn.requestStream(handler, command);
            sink.onDispose(() -> {
                channel.close();
                handler.handleStreamClosePacket(channel, null);
            });
        });
    }

    @Override
    public Mono<GeneratedMessageV3> request(ClusterKey clusterKey, GeneratedMessageV3 command) {
        try {
            return request0(clusterKey, command);
        } catch (Exception e) {
            throw new RuntimeException("Failed to request", e);
        }
    }

    private Mono<GeneratedMessageV3> request0(ClusterKey clusterKey, GeneratedMessageV3 command) {
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

        private final FluxSink<GeneratedMessageV3> sink;

        public ClientStreamCallbackAdapter(FluxSink<GeneratedMessageV3> sink) {
            this.sink = sink;
        }

        @Override
        public void handleStreamResponsePacket(ClientStreamChannel streamChannel, StreamResponsePacket packet) {
            final GeneratedMessageV3 item = deserialize(packet.getPayload());
            if (item != null) {
                sink.next(item);
            }
        }

        @Override
        public void handleStreamClosePacket(ClientStreamChannel streamChannel, StreamClosePacket packet) {
            sink.complete();
        }

        @Override
        public void stateUpdated(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode) {
        }

    }

}
