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
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.context.grpc.config.GrpcTransportConfig;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.ClientStreamingProvider;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.DefaultStreamTask;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.StreamExecutorFactory;
import com.navercorp.pinpoint.profiler.util.NamedRunnable;
import io.github.resilience4j.core.IntervalFunction;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCallStreamObserver;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * @author jaehong.kim
 */
public class SpanGrpcDataSender extends GrpcDataSender<SpanType> {

    private final ReconnectExecutor reconnectExecutor;

    private final Reconnector reconnector;
    private final StreamState failState;
    private final StreamExecutorFactory<PSpanMessage> streamExecutorFactory;
    private final String id = "SpanStream";

    private volatile StreamTask<SpanType, PSpanMessage> currentStreamTask;

    private final ClientStreamingService<PSpanMessage, Empty> clientStreamService;

    private final IntervalFunction interval;
    private final AtomicLong rpcExpiredAt;

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
                attemptRenew();
                return;
            }
            if (message instanceof PSpan) {
                final PSpan pSpan = (PSpan) message;
                final PSpanMessage spanMessage = PSpanMessage.newBuilder().setSpan(pSpan).build();
                stream.onNext(spanMessage);
                attemptRenew();
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
                              StreamState failState,
                              long maxRpcAgeMillis) {
        super(host, port, executorQueueSize, messageConverter, channelFactory);

        this.interval = newIntervalFunction(maxRpcAgeMillis);
        this.rpcExpiredAt = new AtomicLong(System.currentTimeMillis());

        this.reconnectExecutor = Objects.requireNonNull(reconnectExecutor, "reconnectExecutor");
        final Runnable reconnectJob = new NamedRunnable(this.id) {
            @Override
            public void run() {
                startStream();
            }
        };
        this.reconnector = reconnectExecutor.newReconnector(reconnectJob);
        this.failState = Objects.requireNonNull(failState, "failState");
        this.streamExecutorFactory = new StreamExecutorFactory<>(executor);

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
        this.clientStreamService = new ClientStreamingService<>(clientStreamProvider, reconnector);
        reconnectJob.run();
    }

    private IntervalFunction newIntervalFunction(long maxRpcAgeMillis) {
        if (maxRpcAgeMillis >= GrpcTransportConfig.DEFAULT_RENEW_TRANSPORT_PERIOD_MILLIS_DISABLE) {
            return null;
        }
        return IntervalFunction.ofRandomized(maxRpcAgeMillis, 0.1);
    }

    private void attemptRenew() {
        if (interval == null) {
            return;
        }

        final long rpcExpiredAtValue = rpcExpiredAt.get();
        final long now = System.currentTimeMillis();
        if (now > rpcExpiredAtValue) {
            final long nextRpcExpiredAt = now + interval.apply(1);
            if (rpcExpiredAt.compareAndSet(rpcExpiredAtValue, nextRpcExpiredAt)) {
                if (isDebug) {
                    logger.debug("renewStream nextRpcExpiredAt:{}", new Date(nextRpcExpiredAt));
                }
                renewStream();
            }
        }
    }

    private void renewStream() {
        logger.debug("renewStream {}", name);
        if (this.currentStreamTask != null) {
            logger.info("Aborting Span RPC to renew");
            this.currentStreamTask.stop();
        }
    }

    private void startStream() {
        try {
            StreamTask<SpanType, PSpanMessage> streamTask = new DefaultStreamTask<>(id, clientStreamService,
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

        final StreamTask<SpanType, PSpanMessage> currentStreamTask = this.currentStreamTask;
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