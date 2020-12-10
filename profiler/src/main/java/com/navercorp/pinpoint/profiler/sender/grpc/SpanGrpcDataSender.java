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


import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.ClientStreamingProvider;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.DefaultStreamTask;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.StreamExecutorFactory;
import com.navercorp.pinpoint.profiler.util.NamedRunnable;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCallStreamObserver;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * @author jaehong.kim
 */
public class SpanGrpcDataSender extends GrpcDataSender {

    private final ReconnectExecutor reconnectExecutor;

    private final Reconnector reconnector;
    private final StreamState failState;
    private final StreamExecutorFactory<PSpanMessage> streamExecutorFactory;
    private final String id = "SpanStream";

    private volatile StreamTask<PSpanMessage> currentStreamTask;

    private final ClientStreamingService<PSpanMessage, Empty> clientStreamService;

    public final MessageDispatcher<PSpanMessage> dispatcher = new MessageDispatcher<PSpanMessage>() {
        @Override
        public void onDispatch(ClientCallStreamObserver<PSpanMessage> stream, Object data) {
            final GeneratedMessageV3 message = messageConverter.toMessage(data);
            if (isDebug) {
                logger.debug("Send message={}", debugLog(message));
            }
            if (message instanceof PSpanChunk) {
                final PSpanChunk spanChunk = (PSpanChunk) message;
                final PSpanMessage spanMessage = PSpanMessage.newBuilder().setSpanChunk(spanChunk).build();
                stream.onNext(spanMessage);
                return;
            }
            if (message instanceof PSpan) {
                final PSpan pSpan = (PSpan) message;
                final PSpanMessage spanMessage = PSpanMessage.newBuilder().setSpan(pSpan).build();
                stream.onNext(spanMessage);
                return;
            }
            throw new IllegalStateException("unsupported message " + data);
        }
    };


    public SpanGrpcDataSender(String host, int port,
                              int executorQueueSize,
                              MessageConverter<GeneratedMessageV3> messageConverter,
                              ReconnectExecutor reconnectExecutor,
                              ChannelFactory channelFactory,
                              StreamState failState) {
        super(host, port, executorQueueSize, messageConverter, channelFactory);

        this.reconnectExecutor = Assert.requireNonNull(reconnectExecutor, "reconnectExecutor");
        final Runnable reconnectJob = new NamedRunnable(this.id) {
            @Override
            public void run() {
                startStream();
            }
        };
        this.reconnector = reconnectExecutor.newReconnector(reconnectJob);
        this.failState = Assert.requireNonNull(failState, "failState");
        this.streamExecutorFactory = new StreamExecutorFactory<PSpanMessage>(executor);

        ClientStreamingProvider<PSpanMessage, Empty> clientStreamProvider = new ClientStreamingProvider<PSpanMessage, Empty>() {
            @Override
            public ClientCallStreamObserver<PSpanMessage> newStream(ResponseStreamObserver<PSpanMessage, Empty> response) {
                final ManagedChannel managedChannel = SpanGrpcDataSender.this.managedChannel;
                String authority = managedChannel.authority();
                final ConnectivityState state = managedChannel.getState(false);
                SpanGrpcDataSender.this.logger.info("newStream {}/{} state:{} isShutdown:{} isTerminated:{}", id, authority, state, managedChannel.isShutdown(), managedChannel.isTerminated());

                SpanGrpc.SpanStub spanStub = SpanGrpc.newStub(managedChannel);
                return (ClientCallStreamObserver<PSpanMessage>) spanStub.sendSpan(response);
            }

        };
        this.clientStreamService = new ClientStreamingService<PSpanMessage, Empty>(clientStreamProvider, reconnector);
        reconnectJob.run();
    }

    private void startStream() {
        try {
            StreamTask<PSpanMessage> streamTask = new DefaultStreamTask<PSpanMessage, Empty>(id, clientStreamService,
                    this.streamExecutorFactory, this.queue, this.dispatcher, failState);
            streamTask.start();
            this.currentStreamTask = streamTask;
        } catch (Throwable th) {
            logger.error("startStream error", th);
        }
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

        final StreamTask<PSpanMessage> currentStreamTask = this.currentStreamTask;
        if (currentStreamTask != null) {
            currentStreamTask.stop();
        }
        logger.info("{} close()", id);
//        StreamUtils.close(this.stream);
        release();
    }

    @Override
    public String toString() {
        return "SpanGrpcDataSender{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                "} " + super.toString();
    }

}