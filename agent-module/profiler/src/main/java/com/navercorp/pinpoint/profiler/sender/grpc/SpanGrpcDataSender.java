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
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.stream.ClientCallStateStreamObserver;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.ClientStreamingProvider;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.ShortLivedStreamTask;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.StreamExecutorFactory;
import com.navercorp.pinpoint.profiler.util.NamedRunnable;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCallStreamObserver;

import java.util.Objects;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * Sends spans to the collector using short-lived gRPC streams.
 *
 * <p>Instead of maintaining a single long-lived stream, this sender creates a new
 * stream for each batch of spans (up to {@code spanBatchSize}). After the batch is sent,
 * the stream is closed and a new one is started immediately. This approach avoids
 * issues with long-lived streams such as sequence overflow and complex state management.
 *
 * @author jaehong.kim
 */
public class SpanGrpcDataSender extends GrpcDataSender<SpanType> {

    private final SpanGrpc.SpanStub spanStub;
    private final ReconnectExecutor reconnectExecutor;

    private final Reconnector reconnector;
    private final StreamExecutorFactory<PSpanMessage> streamExecutorFactory;
    private final String id = "SpanStream";
    private final int spanBatchSize;

    private volatile StreamTask<SpanType, PSpanMessage> currentStreamTask;

    private final ClientStreamingService<PSpanMessage, Empty> clientStreamService;

    public final MessageDispatcher<SpanType, PSpanMessage> dispatcher = new MessageDispatcher<SpanType, PSpanMessage>() {
        @Override
        public void onDispatch(ClientCallStreamObserver<PSpanMessage> stream, SpanType data) {
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
                              MessageConverter<SpanType, GeneratedMessageV3> messageConverter,
                              ReconnectExecutor reconnectExecutor,
                              ChannelFactory channelFactory,
                              int spanBatchSize) {
        super(host, port, executorQueueSize, messageConverter, channelFactory);
        this.spanStub = SpanGrpc.newStub(managedChannel);
        this.spanBatchSize = spanBatchSize;

        this.reconnectExecutor = Objects.requireNonNull(reconnectExecutor, "reconnectExecutor");
        final Runnable reconnectJob = new NamedRunnable(this.id) {
            @Override
            public void run() {
                startStream();
            }
        };
        this.reconnector = reconnectExecutor.newReconnector(reconnectJob);
        this.streamExecutorFactory = new StreamExecutorFactory<>(executor);

        ClientStreamingProvider<PSpanMessage, Empty> clientStreamProvider = new ClientStreamingProvider<PSpanMessage, Empty>() {
            @Override
            public ClientCallStateStreamObserver<PSpanMessage> newStream(ResponseStreamObserver<PSpanMessage, Empty> response) {
                final ManagedChannel managedChannel = SpanGrpcDataSender.this.managedChannel;
                String authority = managedChannel.authority();
                final ConnectivityState state = managedChannel.getState(false);
                SpanGrpcDataSender.this.logger.info("newStream {}/{} state:{} isShutdown:{} isTerminated:{}", id, authority, state, managedChannel.isShutdown(), managedChannel.isTerminated());

                spanStub.sendSpan(response);

                return response.getRequestStream();
            }

        };
        this.clientStreamService = new ClientStreamingService<>(clientStreamProvider, reconnector);
        reconnectJob.run();
    }

    private void startStream() {
        if (shutdown) {
            return;
        }
        try {
            final Runnable onBatchComplete = () -> reconnectExecutor.scheduleNow(SpanGrpcDataSender.this::startStream);
            StreamTask<SpanType, PSpanMessage> streamTask = new ShortLivedStreamTask<>(id, clientStreamService,
                    reconnector, this.streamExecutorFactory, this.queue, this.dispatcher, spanBatchSize, onBatchComplete);
            streamTask.start();
            this.currentStreamTask = streamTask;
        } catch (Throwable th) {
            logger.error("startStream error", th);
        }
    }

    @Override
    public void close() {
        if (shutdown) {
            return;
        }
        this.shutdown = true;

        logger.info("Stop {}, channel={}", name, managedChannel);
        final ReconnectExecutor reconnectExecutor = this.reconnectExecutor;
        if (reconnectExecutor != null) {
            reconnectExecutor.close();
        }

        final StreamTask<SpanType, PSpanMessage> currentStreamTask = this.currentStreamTask;
        if (currentStreamTask != null) {
            currentStreamTask.stop();
        }
        logger.info("{} close()", id);
        release();
    }

    @Override
    public String toString() {
        return "SpanGrpcDataSender{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", spanBatchSize=" + spanBatchSize +
                "} " + super.toString();
    }

}
