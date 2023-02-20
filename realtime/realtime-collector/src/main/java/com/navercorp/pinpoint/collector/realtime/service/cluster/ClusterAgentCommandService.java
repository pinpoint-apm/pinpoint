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
package com.navercorp.pinpoint.collector.realtime.service.cluster;

import com.navercorp.pinpoint.collector.cluster.ClusterPoint;
import com.navercorp.pinpoint.collector.cluster.GrpcAgentConnection;
import com.navercorp.pinpoint.collector.cluster.ThriftAgentConnection;
import com.navercorp.pinpoint.collector.cluster.route.StreamRouteHandler;
import com.navercorp.pinpoint.collector.realtime.service.AgentCommandService;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;
import com.navercorp.pinpoint.rpc.stream.StreamException;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author youngjin.kim2
 */
public class ClusterAgentCommandService implements AgentCommandService {

    private final StreamRouteHandler streamRouteHandler;
    private final SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    public ClusterAgentCommandService(
            StreamRouteHandler streamRouteHandler,
            SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory
    ) {
        this.streamRouteHandler = Objects.requireNonNull(streamRouteHandler, "streamRouteHandler");
        this.commandSerializerFactory = Objects.requireNonNull(commandSerializerFactory, "commandSerializerFactory");
    }

    @Override
    public ClusterPoint<?> findClusterPoint(ClusterKey clusterKey) {
        return streamRouteHandler.findClusterPoint(clusterKey);
    }

    @Override
    public ClientStreamChannel request(
            ClusterPoint<?> clusterPoint,
            TBase<?, ?> command,
            Consumer<StreamResponsePacket> callback
    ) throws Exception {
        final ClientStreamChannelEventHandler handler = new ClientStreamCallbackAdapter(callback);
        return openClientStreamChannel(clusterPoint, handler, command);
    }

    private ClientStreamChannel openClientStreamChannel(
            ClusterPoint<?> clusterPoint,
            ClientStreamChannelEventHandler handler,
            TBase<?, ?> command
    ) throws TException, StreamException {
        if (!clusterPoint.isSupportCommand(command)) {
            throw new RuntimeException("Unsupported command: " + command);
        }

        if (clusterPoint instanceof ThriftAgentConnection) {
            final byte[] payload = commandSerializerFactory.createSerializer().serialize(command);
            final long timeoutMillis = 3000;
            return ((ThriftAgentConnection) clusterPoint)
                    .getPinpointServer()
                    .openStreamAndAwait(payload, handler, timeoutMillis);
        }

        if (clusterPoint instanceof GrpcAgentConnection) {
            return ((GrpcAgentConnection) clusterPoint).openStream(command, handler);
        }

        throw new RuntimeException("Invalid clusterPoint: " + clusterPoint);
    }

    private static class ClientStreamCallbackAdapter extends ClientStreamChannelEventHandler {

        private final Consumer<StreamResponsePacket> callback;

        public ClientStreamCallbackAdapter(Consumer<StreamResponsePacket> callback) {
            this.callback = callback;
        }

        @Override
        public void handleStreamResponsePacket(ClientStreamChannel streamChannel, StreamResponsePacket packet) {
            callback.accept(packet);
        }

        @Override
        public void handleStreamClosePacket(ClientStreamChannel streamChannel, StreamClosePacket packet) {

        }

        @Override
        public void stateUpdated(ClientStreamChannel streamChannel, StreamChannelStateCode updatedStateCode) {

        }

    }

}
