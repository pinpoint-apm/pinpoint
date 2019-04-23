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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.cluster.AgentInfo;
import com.navercorp.pinpoint.grpc.trace.PCmdEcho;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.profiler.context.thrift.CommandGrpcToThriftMessageConverter;
import com.navercorp.pinpoint.profiler.receiver.CommandSerializer;
import com.navercorp.pinpoint.rpc.DefaultFuture;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.RequestManager;
import com.navercorp.pinpoint.rpc.common.SocketState;
import com.navercorp.pinpoint.rpc.common.SocketStateChangeResult;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.stub.StreamObserver;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Taejin Koo
 */
public class PinpointGrpcServer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SocketState state = new SocketState();
    private final AtomicReference<List<Integer>> supportCommandServiceList = new AtomicReference<>();

    private final CommandGrpcToThriftMessageConverter messageConverter = new CommandGrpcToThriftMessageConverter();

    private final AgentInfo agentInfo;
    private final RequestManager requestManager;
    private final StreamObserver<PCmdRequest> requestObserver;

    public PinpointGrpcServer(AgentInfo agentInfo, RequestManager requestManager, StreamObserver<PCmdRequest> requestObserver) {
        this.agentInfo = Objects.requireNonNull(agentInfo, "agentInfo must not be null");
        this.requestManager = Objects.requireNonNull(requestManager, "requestManager must not be null");
        this.requestObserver = Objects.requireNonNull(requestObserver, "requestObserver must not be null");
    }

    public void connected() {
        toState(SocketStateCode.CONNECTED);
    }

    public boolean handleHandshake(List<Integer> supportCommandServiceList) {
        logger.info("{} handleHandshake() started. data:{}", agentInfo, supportCommandServiceList);

        boolean isFirst = this.supportCommandServiceList.compareAndSet(null, supportCommandServiceList);
        if (isFirst) {
            toState(SocketStateCode.RUN_WITHOUT_HANDSHAKE);
            SocketStateChangeResult socketStateChangeResult = toState(SocketStateCode.RUN_DUPLEX);
            return socketStateChangeResult.isChange();
        }
        return false;
    }

    private SocketStateChangeResult toState(SocketStateCode socketStateCode) {
        SocketStateChangeResult result = state.to(socketStateCode);
        logger.info(result.toString());
        return result;
    }

    public Future<ResponseMessage> request(GeneratedMessageV3 message) {
        if (state.getCurrentState() != SocketStateCode.RUN_DUPLEX) {
            DefaultFuture<ResponseMessage> failedFuture = new DefaultFuture<>();
            failedFuture.setFailure(new IllegalStateException("failed to request message. caused:illegal State"));
            return failedFuture;
        }

        PCmdRequest.Builder requestBuilder = PCmdRequest.newBuilder();

        final int requestId = requestManager.nextRequestId();
        requestBuilder.setRequestId(requestId);

        DefaultFuture<ResponseMessage> future = requestManager.register(requestId);

        if (message instanceof PCmdEcho) {
            requestBuilder.setCommandEcho((PCmdEcho) message);
        } else {
            future.setFailure(new PinpointSocketException(TRouteResult.NOT_SUPPORTED_REQUEST.name()));
            return future;
        }

        requestObserver.onNext(requestBuilder.build());
        return future;
    }

    public void handleMessage(int responseId, GeneratedMessageV3 message) {
        if (state.getCurrentState() != SocketStateCode.RUN_DUPLEX) {
            logger.warn("failed to handle message. caused:illegal State");
            return ;
        }

        logger.info("{} handleMessage:{}", agentInfo, message);
        TBase tMessage = messageConverter.toMessage(message);

        try {
            byte[] serialize = SerializationUtils.serialize(tMessage, CommandSerializer.SERIALIZER_FACTORY);
            ResponsePacket responsePacket = new ResponsePacket(responseId, serialize);
            requestManager.messageReceived(responsePacket, agentInfo.toString());
        } catch (TException e) {
            setFailMessageToFuture(responseId, e.getMessage());
        }
    }

    public void handleFail(PCmdResponse failMessage) {
        String message = failMessage.getMessage().getValue();
        if (message != null) {
            setFailMessageToFuture(failMessage.getResponseId(), message);
        } else {
            setFailMessageToFuture(failMessage.getResponseId(), "failed to route message");
        }
    }

    private void setFailMessageToFuture(int responseId, String message) {
        DefaultFuture<ResponseMessage> future = requestManager.removeMessageFuture(responseId);
        if (future != null) {
            future.setFailure(new PinpointSocketException(message));
        }
    }

    public void disconnected() {
        toState(SocketStateCode.BEING_CLOSE_BY_CLIENT);
        toState(SocketStateCode.CLOSED_BY_CLIENT);
    }

    public SocketStateCode getState() {
        return state.getCurrentState();
    }

    public void close() {
        toState(SocketStateCode.BEING_CLOSE_BY_SERVER);
        toState(SocketStateCode.CLOSED_BY_SERVER);
    }

    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

}
