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
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCount;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.PCmdEcho;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.profiler.context.thrift.CommandGrpcToThriftMessageConverter;
import com.navercorp.pinpoint.rpc.DefaultFuture;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.RequestManager;
import com.navercorp.pinpoint.rpc.common.SocketState;
import com.navercorp.pinpoint.rpc.common.SocketStateChangeResult;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannel;
import com.navercorp.pinpoint.rpc.stream.StreamChannelRepository;
import com.navercorp.pinpoint.rpc.stream.StreamException;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.CommandHeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import io.grpc.stub.StreamObserver;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Taejin Koo
 */
public class PinpointGrpcServer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isInfo = logger.isInfoEnabled();

    private final SocketState state = new SocketState();
    private final AtomicReference<List<Integer>> supportCommandServiceList = new AtomicReference<>();

    private final CommandGrpcToThriftMessageConverter messageConverter = new CommandGrpcToThriftMessageConverter();
    private final CommandHeaderTBaseSerializerFactory commandHeaderTBaseSerializerFactory = CommandHeaderTBaseSerializerFactory.getDefaultInstance();

    private final StreamChannelRepository streamChannelRepository = new StreamChannelRepository();

    private final InetSocketAddress remoteAddress;
    private final AgentInfo agentInfo;
    private final RequestManager requestManager;
    private final StreamObserver<PCmdRequest> requestObserver;

    public PinpointGrpcServer(InetSocketAddress remoteAddress, AgentInfo agentInfo, RequestManager requestManager, StreamObserver<PCmdRequest> requestObserver) {
        this.remoteAddress = Objects.requireNonNull(remoteAddress, "remoteAddress");
        this.agentInfo = Objects.requireNonNull(agentInfo, "agentInfo");
        this.requestManager = Objects.requireNonNull(requestManager, "requestManager");
        this.requestObserver = Objects.requireNonNull(requestObserver, "requestObserver");
    }

    public void connected() {
        toState(SocketStateCode.CONNECTED);
    }

    public boolean handleHandshake(List<Integer> supportCommandServiceList) {
        if (isInfo) {
            logger.info("{} handleHandshake() started. data:{}", agentInfo, supportCommandServiceList);
        }

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
        if (logger.isDebugEnabled()) {
            logger.debug(result.toString());
        }
        return result;
    }

    public Future<ResponseMessage> request(GeneratedMessageV3 message) {
        if (!state.checkState(SocketStateCode.RUN_DUPLEX)) {
            return createFailedFuture(new IllegalStateException("failed to request message. caused:illegal State"));
        }

        PCmdRequest request = createRequest(message);
        if (request == null) {
            return createFailedFuture(new PinpointSocketException(TRouteResult.NOT_SUPPORTED_REQUEST.name()));
        }

        DefaultFuture<ResponseMessage> future = requestManager.register(request.getRequestId());
        requestObserver.onNext(request);
        return future;
    }

    // 1st message : client(collector) -> server(agent)
    public ClientStreamChannel openStream(GeneratedMessageV3 message, ClientStreamChannelEventHandler streamChannelEventHandler) throws StreamException {
        if (!state.checkState(SocketStateCode.RUN_DUPLEX)) {
            throw new StreamException(StreamCode.STATE_NOT_CONNECTED);
        }

        PCmdRequest request = createRequest(message);
        if (request == null) {
            throw new StreamException(StreamCode.TYPE_UNSUPPORT);
        }

        GrpcClientStreamChannel grpcClientStreamChannel = new GrpcClientStreamChannel(remoteAddress, request.getRequestId(), streamChannelRepository, streamChannelEventHandler);
        try {
            grpcClientStreamChannel.init();
            grpcClientStreamChannel.connect(new Runnable() {
                @Override
                public void run() {
                    requestObserver.onNext(request);
                }
            }, 3000);
        } catch (StreamException e) {
            grpcClientStreamChannel.close(e.getStreamCode());
            throw e;
        }
        return grpcClientStreamChannel;
    }

    private PCmdRequest createRequest(GeneratedMessageV3 message) {
        PCmdRequest.Builder requestBuilder = PCmdRequest.newBuilder();

        final int requestId = requestManager.nextRequestId();
        requestBuilder.setRequestId(requestId);

        if (message instanceof PCmdEcho) {
            requestBuilder.setCommandEcho((PCmdEcho) message);
            return requestBuilder.build();
        } else if (message instanceof PCmdActiveThreadCount) {
            requestBuilder.setCommandActiveThreadCount((PCmdActiveThreadCount) message);
            return requestBuilder.build();
        } else if (message instanceof PCmdActiveThreadDump) {
            requestBuilder.setCommandActiveThreadDump((PCmdActiveThreadDump) message);
            return requestBuilder.build();
        } else if (message instanceof PCmdActiveThreadLightDump) {
            requestBuilder.setCommandActiveThreadLightDump((PCmdActiveThreadLightDump) message);
            return requestBuilder.build();
        } else {
            return null;
        }
    }

    public void handleMessage(int responseId, GeneratedMessageV3 message) {
        if (!state.checkState(SocketStateCode.RUN_DUPLEX)) {
            logger.warn("failed to handle message. caused:illegal State");
            return;
        }

        if (isInfo) {
            logger.info("{} handleMessage:{}", agentInfo, MessageFormatUtils.debugLog(message));
        }
        TBase tMessage = messageConverter.toMessage(message);

        try {
            byte[] serialize = SerializationUtils.serialize(tMessage, commandHeaderTBaseSerializerFactory);
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

    // 2nd message : server(agent) -> client(collector)
    public boolean handleStreamCreateMessage(int streamId, StreamObserver<Empty> connectionObserver) {
        if (isInfo) {
            logger.info("handleStreamCreateMessage. streamId:{}", streamId);
        }

        StreamChannel streamChannel = streamChannelRepository.getStreamChannel(streamId);
        if (streamChannel == null) {
            logger.warn("Can't find suitable streamChannel.(streamId:{})", streamId);
            return false;
        }

        GrpcClientStreamChannel grpcClientStreamChannel = (GrpcClientStreamChannel) streamChannel;
        grpcClientStreamChannel.setConnectionObserver(connectionObserver);
        return grpcClientStreamChannel.changeStateConnected();
    }

    public void handleStreamMessage(int streamId, GeneratedMessageV3 message) throws StreamException {
        if (logger.isDebugEnabled()) {
            logger.debug("handleStreamMessage() streamId:{}, message:{}", streamId, MessageFormatUtils.debugLog(message));
        }

        GrpcClientStreamChannel streamChannel = (GrpcClientStreamChannel) streamChannelRepository.getStreamChannel(streamId);
        if (streamChannel == null) {
            throw new StreamException(StreamCode.ID_NOT_FOUND, "Can't find suitable streamChannel.(streamId:" + streamId + ")");
        }

        TBase tBase = messageConverter.toMessage(message);
        if (tBase == null) {
            throw new StreamException(StreamCode.TYPE_ERROR, "Failed to convert message.(message:" + MessageFormatUtils.debugLog(message).toString() + ")");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("handleStreamMessage() streamId:{}, message:{}", streamId, tBase);
        }
        try {
            byte[] serialize = SerializationUtils.serialize(tBase, commandHeaderTBaseSerializerFactory);
            streamChannel.handleStreamResponsePacket(new StreamResponsePacket(streamId, serialize));
        } catch (TException t) {
            throw new StreamException(StreamCode.TYPE_UNKNOWN, "Failed to serialize message.(tBase:" + tBase + ")");
        } catch (StreamException e) {
            throw e;
        }
    }

    public void handleStreamDisconnected(int streamId) {
        handleStreamDisconnected(streamId, null);
    }

    public void handleStreamDisconnected(int streamId, Throwable t) {
        GrpcClientStreamChannel streamChannel = (GrpcClientStreamChannel) streamChannelRepository.getStreamChannel(streamId);
        if (streamChannel == null) {
            logger.warn("Can't find suitable streamChannel.(streamId:{})", streamId);
            return;
        }

        if (t == null) {
            streamChannel.disconnect(StreamCode.STATE_CLOSED);
        } else {
            streamChannel.disconnect(StreamCode.CONNECTION_ERRROR);
        }
    }

    public void disconnected() {
        close(false);
    }

    public void close() {
        close(true);
    }

    public void close(boolean serverStop) {
        synchronized (this) {
            try {
                if (SocketStateCode.isRun(getState())) {
                    if (serverStop) {
                        toState(SocketStateCode.BEING_CLOSE_BY_SERVER);
                        requestObserver.onCompleted();
                    } else {
                        toState(SocketStateCode.BEING_CLOSE_BY_CLIENT);
                        requestObserver.onCompleted();
                    }
                }

                SocketStateCode currentStateCode = getState();
                if (SocketStateCode.BEING_CLOSE_BY_SERVER == currentStateCode) {
                    toState(SocketStateCode.CLOSED_BY_SERVER);
                } else if (SocketStateCode.BEING_CLOSE_BY_CLIENT == currentStateCode) {
                    toState(SocketStateCode.CLOSED_BY_CLIENT);
                } else if (SocketStateCode.isClosed(currentStateCode)) {
                    logger.warn("stop(). Socket has closed state({}).", currentStateCode);
                } else {
                    toState(SocketStateCode.ERROR_ILLEGAL_STATE_CHANGE);
                    logger.warn("stop(). Socket has unexpected state({})", currentStateCode);
                }
            } finally {
                logger.info("{} <=> local all streamChannels will be close.", agentInfo.getAgentKey());
                streamChannelRepository.close(StreamCode.STATE_CLOSED);
            }
        }
    }

    private void setFailMessageToFuture(int responseId, String message) {
        DefaultFuture<ResponseMessage> future = requestManager.removeMessageFuture(responseId);
        if (future != null) {
            future.setFailure(new PinpointSocketException(message));
        }
    }

    public SocketStateCode getState() {
        return state.getCurrentState();
    }

    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    public Future<ResponseMessage> createFailedFuture(Exception failException) {
        DefaultFuture<ResponseMessage> failedFuture = new DefaultFuture<>();
        failedFuture.setFailure(failException);
        return failedFuture;
    }
}