/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.SocketIdClientInterceptor;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.receiver.grpc.CommandServiceStubFactory;
import com.navercorp.pinpoint.profiler.receiver.grpc.GrpcCommandService;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.rpc.DefaultFuture;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;

import org.jboss.netty.buffer.ChannelBuffers;

import java.util.concurrent.ScheduledExecutorService;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.stub.StreamObserver;

/**
 * @author jaehong.kim
 */
public class AgentGrpcDataSender extends GrpcDataSender implements EnhancedDataSender<Object> {

    static {
        // preClassLoad
        ChannelBuffers.buffer(2);
    }

    private final AgentGrpc.AgentStub agentInfoStub;
    private final AgentGrpc.AgentStub agentPingStub;
    private GrpcCommandService grpcCommandService;

    private final ReconnectExecutor reconnectExecutor;

    private volatile PingStreamContext pingStreamContext;
    private final Reconnector reconnector;

    public AgentGrpcDataSender(String host, int port, int executorQueueSize,
                               MessageConverter<GeneratedMessageV3> messageConverter,
                               ReconnectExecutor reconnectExecutor,
                               final ScheduledExecutorService retransmissionExecutor,
                               ChannelFactory channelFactory,
                               ActiveTraceRepository activeTraceRepository) {
        super(host, port, executorQueueSize, messageConverter, channelFactory);

        this.agentInfoStub = AgentGrpc.newStub(managedChannel);
        this.agentPingStub = newAgentPingStub();

        this.reconnectExecutor = reconnectExecutor;
        CommandServiceStubFactory commandServiceStubFactory = new CommandServiceStubFactory(managedChannel);
        this.grpcCommandService = new GrpcCommandService(commandServiceStubFactory, reconnectExecutor, activeTraceRepository);
        {
            final Runnable reconnectJob = new Runnable() {
                @Override
                public void run() {
                    pingStreamContext = newPingStream(agentPingStub, retransmissionExecutor);
                }
            };
            this.reconnector = reconnectExecutor.newReconnector(reconnectJob);
            reconnectJob.run();
        }
    }

    private AgentGrpc.AgentStub newAgentPingStub() {
        AgentGrpc.AgentStub agentStub = AgentGrpc.newStub(managedChannel);
        return agentStub.withInterceptors(new SocketIdClientInterceptor());
    }


    private PingStreamContext newPingStream(AgentGrpc.AgentStub agentStub, ScheduledExecutorService reconnectScheduler) {
        final PingStreamContext pingStreamContext = new PingStreamContext(agentStub, reconnector, reconnectScheduler);
        logger.info("newPingStream:{}", pingStreamContext);
        return pingStreamContext;
    }

    @Override
    public boolean request(Object data) {
        throw new UnsupportedOperationException("unsupported operation request(data)");
    }

    @Override
    public boolean request(Object data, int retryCount) {
        throw new UnsupportedOperationException("unsupported operation request(data, retryCount)");
    }

    @Override
    public boolean request(Object data, final FutureListener listener) {
        final GeneratedMessageV3 message = this.messageConverter.toMessage(data);
        if (!(message instanceof PAgentInfo)) {
            throw new IllegalArgumentException("unsupported message " + data);
        }

        final PAgentInfo pAgentInfo = (PAgentInfo) message;
        this.agentInfoStub.requestAgentInfo(pAgentInfo, new FutureListenerStreamObserver(listener));

        return true;
    }

    @Override
    public boolean send(Object data) {
        throw new UnsupportedOperationException("unsupported operation send(data)");
    }

    @Override
    public void stop() {
        if (shutdown) {
            return;
        }
        this.shutdown = true;

        logger.info("Stop {}, channel={}", name, managedChannel);
        final ReconnectExecutor reconnectExecutor = this.reconnectExecutor;
        if (reconnectExecutor != null) {
            reconnectExecutor.close();
        }
        final PingStreamContext pingStreamContext = this.pingStreamContext;
        if (pingStreamContext != null) {
            pingStreamContext.close();
        }

        final GrpcCommandService grpcCommandService = this.grpcCommandService;
        if (grpcCommandService != null) {
            grpcCommandService.stop();
        }
        this.release();
    }

    @Override
    public boolean addReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        throw new UnsupportedOperationException("unsupported operation addReconnectEventListener(eventListener)");
    }

    @Override
    public boolean removeReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        throw new UnsupportedOperationException("unsupported operation removeReconnectEventListener(eventListener)");
    }

    private static class FutureListenerStreamObserver implements StreamObserver<PResult> {
        private final FutureListener listener;

        private FutureListenerStreamObserver(FutureListener listener) {
            this.listener = listener;
        }

        @Override
        public void onNext(PResult result) {
            final DefaultFuture<ResponseMessage> future = new DefaultFuture<ResponseMessage>();
            final ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setMessage(result.toByteArray());
            future.setResult(responseMessage);
            future.setListener(listener);
        }

        @Override
        public void onError(Throwable throwable) {
            final DefaultFuture<ResponseMessage> future = new DefaultFuture<ResponseMessage>();
            future.setFailure(throwable);
            future.setListener(listener);
        }

        @Override
        public void onCompleted() {
        }
    }
}