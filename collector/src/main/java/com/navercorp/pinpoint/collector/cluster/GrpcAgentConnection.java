/*
 * Copyright 2019 NAVER Corp.
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
import com.navercorp.pinpoint.collector.receiver.grpc.PinpointGrpcServer;
import com.navercorp.pinpoint.profiler.context.grpc.CommandThriftToGrpcMessageConverter;
import com.navercorp.pinpoint.rpc.DefaultFuture;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamException;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import org.apache.thrift.TBase;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class GrpcAgentConnection implements ClusterPoint<TBase> {

    private final CommandThriftToGrpcMessageConverter messageConverter = new CommandThriftToGrpcMessageConverter();

    private final PinpointGrpcServer pinpointGrpcServer;

    private final List<TCommandType> supportCommandList;

    public GrpcAgentConnection(PinpointGrpcServer pinpointGrpcServer, List<Integer> supportCommandServiceKeyList) {
        this.pinpointGrpcServer = Objects.requireNonNull(pinpointGrpcServer, "pinpointGrpcServer");

        Objects.requireNonNull(supportCommandServiceKeyList, "supportCommandServiceKeyList");
        this.supportCommandList = SupportedCommandUtils.newSupportCommandList(supportCommandServiceKeyList);
    }

    @Override
    public Future<ResponseMessage> request(TBase request) {
        GeneratedMessageV3 message = messageConverter.toMessage(request);
        if (message == null) {
            DefaultFuture<ResponseMessage> failedFuture = new DefaultFuture<ResponseMessage>();
            failedFuture.setFailure(new PinpointSocketException(TRouteResult.NOT_SUPPORTED_REQUEST.name()));
            return failedFuture;
        }
        return pinpointGrpcServer.request(message);
    }

    public ClientStreamChannel openStream(TBase request, ClientStreamChannelEventHandler streamChannelEventHandler) throws StreamException {
        GeneratedMessageV3 message = messageConverter.toMessage(request);
        if (message == null) {
            throw new StreamException(StreamCode.TYPE_UNSUPPORT);
        }
        return pinpointGrpcServer.openStream(message, streamChannelEventHandler);
    }

    @Override
    public AgentInfo getDestAgentInfo() {
        return pinpointGrpcServer.getAgentInfo();
    }

    @Override
    public boolean isSupportCommand(TBase command) {
        for (TCommandType supportCommand : supportCommandList) {
            if (supportCommand.getClazz() == command.getClass()) {
                return true;
            }
        }
        return false;
    }

    public PinpointGrpcServer getPinpointGrpcServer() {
        return pinpointGrpcServer;
    }

    @Override
    public int hashCode() {
        return pinpointGrpcServer.getAgentInfo().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof GrpcAgentConnection)) {
            return false;
        }

        if (this.pinpointGrpcServer == ((GrpcAgentConnection) obj).pinpointGrpcServer) {
            return true;
        }

        return false;
    }

}
