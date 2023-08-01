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
package com.navercorp.pinpoint.collector.cluster;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.realtime.collector.service.AgentConnection;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamException;
import com.navercorp.pinpoint.thrift.sender.message.CommandGrpcToThriftMessageConverter;
import org.apache.thrift.TBase;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author youngjin.kim2
 */
public class AgentConnectionImpl implements AgentConnection {

    private final ClusterPoint<?> clusterPoint;
    private final CommandGrpcToThriftMessageConverter messageConverter;

    public AgentConnectionImpl(ClusterPoint<?> clusterPoint, CommandGrpcToThriftMessageConverter messageConverter) {
        this.clusterPoint = Objects.requireNonNull(clusterPoint, "clusterPoint");
        this.messageConverter = Objects.requireNonNull(messageConverter, "messageConverter");
    }

    @Override
    public ClientStreamChannel requestStream(ClientStreamChannelEventHandler handler, GeneratedMessageV3 command) {
        TBase<?, ?> tCommand = this.messageConverter.toMessage(command);
        if (!this.clusterPoint.isSupportCommand(tCommand)) {
            throw new RuntimeException("Unsupported command: " + command);
        }
        if (clusterPoint instanceof GrpcAgentConnection) {
            return openStream(handler, tCommand);
        }
        throw new RuntimeException("Invalid clusterPoint: " + clusterPoint);
    }

    private ClientStreamChannel openStream(ClientStreamChannelEventHandler handler, TBase<?, ?> tCommand) {
        try {
            return ((GrpcAgentConnection) clusterPoint).openStream(tCommand, handler);
        } catch (StreamException e) {
            throw new RuntimeException("Failed to openStream " + tCommand);
        }
    }

    @Override
    public CompletableFuture<ResponseMessage> request(GeneratedMessageV3 command) {
        TBase<?, ?> tCommand = this.messageConverter.toMessage(command);
        if (!this.clusterPoint.isSupportCommand(tCommand)) {
            throw new RuntimeException("Unsupported command: " + command);
        }
        if (clusterPoint instanceof GrpcAgentConnection) {
            return ((GrpcAgentConnection) this.clusterPoint).request(tCommand);
        }
        throw new RuntimeException("Invalid clusterPoint: " + clusterPoint);
    }

}
