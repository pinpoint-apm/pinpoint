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
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.grpc.trace.PCustomMetricMessage;
import com.navercorp.pinpoint.grpc.trace.PStatMessage;
import com.navercorp.pinpoint.grpc.trace.StatGrpc;
import com.navercorp.pinpoint.profiler.context.grpc.config.GrpcSenderCompression;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.ClientStreamingProvider;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.DefaultStreamTask;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.StreamExecutorFactory;
import com.navercorp.pinpoint.profiler.util.NamedRunnable;
import io.grpc.Status;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * @author jaehong.kim
 */
public class StatGrpcDataSender extends GrpcDataSender<MetricType> {
    private static final String ID = "StatStream";
    /**
     * With {@link GrpcSenderCompression#GZIP}, a message is compressed only from this serialized size on:
     * a single PAgentStat is a few hundred bytes and gzip would make it larger, while a PAgentStatBatch
     * or a PAgentUriStat with many URIs shrinks to a third or a half.
     */
    static final int COMPRESSION_MIN_BYTES = 512;

    private final StatGrpc.StatStub uncompressedStub;
    // the compressed stub until a collector rejects the compression, then uncompressedStub
    private volatile StatGrpc.StatStub statStub;
    private final GrpcSenderCompression compression;
    private final AtomicBoolean compressionFallback = new AtomicBoolean(false);
    private final ReconnectExecutor reconnectExecutor;

    private final Reconnector reconnector;
    private final StreamState failState;
    private final StreamExecutorFactory<PStatMessage> streamExecutorFactory;


    private volatile StreamTask<MetricType, PStatMessage> currentStreamTask;

    private final ClientStreamingService<PStatMessage, Empty> clientStreamService;

    public MessageDispatcher<MetricType, PStatMessage> dispatcher = new MessageDispatcher<MetricType, PStatMessage>() {
        @Override
        public void onDispatch(ClientCallStreamObserver<PStatMessage> stream, MetricType data) {
            final GeneratedMessageV3 message = messageConverter.toMessage(data);
            if (isDebug) {
                logger.debug("Send message={}", debugLog(message));
            }

            if (message instanceof PAgentStatBatch) {
                final PAgentStatBatch agentStatBatch = (PAgentStatBatch) message;
                final PStatMessage statMessage = PStatMessage.newBuilder().setAgentStatBatch(agentStatBatch).build();
                send(stream, statMessage);
                return;
            }

            if (message instanceof PAgentStat) {
                final PAgentStat agentStat = (PAgentStat) message;
                final PStatMessage statMessage = PStatMessage.newBuilder().setAgentStat(agentStat).build();
                send(stream, statMessage);
                return;
            }
            if (message instanceof PCustomMetricMessage) {
                final PCustomMetricMessage customMetricMessage = (PCustomMetricMessage) message;
                logger.info("Message will not delivered. message:{}", message);

                return;
            }
            if (message instanceof PAgentUriStat) {
                final PAgentUriStat agentUriStat = (PAgentUriStat) message;
                final PStatMessage statMessage = PStatMessage.newBuilder().setAgentUriStat(agentUriStat).build();

                // TODO remove comment
                send(stream, statMessage);
                return;
            }
            throw new IllegalStateException("unsupported message " + message);
        }
    };

    public StatGrpcDataSender(String host, int port,
                              int executorQueueSize,
                              MessageConverter<MetricType, GeneratedMessageV3> messageConverter,
                              ReconnectExecutor reconnectExecutor,
                              ChannelFactory channelFactory) {
        this(host, port, executorQueueSize, messageConverter, reconnectExecutor, channelFactory, GrpcSenderCompression.defaultCompression());
    }

    /**
     * @param compression with {@link GrpcSenderCompression#GZIP} the stat stream is opened with gzip and messages of
     *                    {@link #COMPRESSION_MIN_BYTES} or more are compressed. A collector that cannot decompress
     *                    gzip fails the stream with UNIMPLEMENTED "Can't find decompressor"; the next stream is then
     *                    opened uncompressed for the rest of this sender's life, logged once.
     */
    public StatGrpcDataSender(String host, int port,
                              int executorQueueSize,
                              MessageConverter<MetricType, GeneratedMessageV3> messageConverter,
                              ReconnectExecutor reconnectExecutor,
                              ChannelFactory channelFactory,
                              GrpcSenderCompression compression) {
        super(host, port, executorQueueSize, messageConverter, channelFactory);
        this.compression = compression == null ? GrpcSenderCompression.defaultCompression() : compression;
        this.uncompressedStub = StatGrpc.newStub(managedChannel);
        final String grpcName = this.compression.getGrpcName();
        this.statStub = grpcName == null ? this.uncompressedStub : this.uncompressedStub.withCompression(grpcName);

        this.reconnectExecutor = Objects.requireNonNull(reconnectExecutor, "reconnectExecutor");
        final Runnable reconnectJob = new NamedRunnable(ID) {
            @Override
            public void run() {
                startStream();
            }
        };
        this.reconnector = reconnectExecutor.newReconnector(reconnectJob);
        this.failState = new SimpleStreamState(100, 5000);
        this.streamExecutorFactory = new StreamExecutorFactory<>(executor);

        ClientStreamingProvider<PStatMessage, Empty> clientStreamProvider = new ClientStreamingProvider<PStatMessage, Empty>() {
            @Override
            public ClientCallStateStreamObserver<PStatMessage> newStream(ResponseStreamObserver<PStatMessage, Empty> response) {
                logger.info("newStream {} compression={}{}", ID, compression, compressionFallback.get() ? " (fallback: uncompressed)" : "");
                statStub.sendAgentStat(watchCompression(response));
                return response.getRequestStream();
            }
        };
        this.clientStreamService = new ClientStreamingService<>(clientStreamProvider, reconnector);

        reconnectJob.run();

    }

    private void startStream() {
//        streamTaskManager.closeAllStream();
        try {
            StreamTask<MetricType, PStatMessage> streamTask = new DefaultStreamTask<>(ID, clientStreamService,
                    this.streamExecutorFactory, this.queue, this.dispatcher, failState);
            streamTask.start();
            currentStreamTask = streamTask;
        } catch (Throwable th) {
            logger.error("Unexpected error", th);
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

        final StreamTask<MetricType, PStatMessage> currentStreamTask = this.currentStreamTask;
        if (currentStreamTask != null) {
            currentStreamTask.stop();
        }
        logger.info("{} close()", ID);
        release();
    }

    /**
     * Sends one message, compressed only when the stream is compressed and the message is large enough
     * ({@link #COMPRESSION_MIN_BYTES}): per message, gzip of a few hundred bytes grows rather than shrinks.
     */
    private void send(ClientCallStreamObserver<PStatMessage> stream, PStatMessage statMessage) {
        if (compression != GrpcSenderCompression.NONE && !compressionFallback.get()) {
            stream.setMessageCompression(shouldCompress(statMessage));
        }
        stream.onNext(statMessage);
    }

    static boolean shouldCompress(PStatMessage statMessage) {
        return statMessage.getSerializedSize() >= COMPRESSION_MIN_BYTES;
    }

    /**
     * Wraps the response observer to notice a collector that cannot read the compression, so that the
     * reconnect opens the next stream uncompressed. Everything else is delegated untouched.
     */
    private ClientResponseObserver<PStatMessage, Empty> watchCompression(final ResponseStreamObserver<PStatMessage, Empty> response) {
        if (compression == GrpcSenderCompression.NONE) {
            return response;
        }
        return new ClientResponseObserver<PStatMessage, Empty>() {
            @Override
            public void beforeStart(ClientCallStreamObserver<PStatMessage> requestStream) {
                response.beforeStart(requestStream);
            }

            @Override
            public void onNext(Empty value) {
                response.onNext(value);
            }

            @Override
            public void onError(Throwable t) {
                if (isDecompressorUnavailable(t)) {
                    fallbackToUncompressed(t);
                }
                response.onError(t);
            }

            @Override
            public void onCompleted() {
                response.onCompleted();
            }
        };
    }

    /**
     * The collector answered UNIMPLEMENTED "Can't find decompressor for gzip": it cannot read this
     * compression, so every further compressed stream would fail too.
     */
    boolean isDecompressorUnavailable(Throwable t) {
        if (compression == GrpcSenderCompression.NONE) {
            return false;
        }
        final Status status = Status.fromThrowable(t);
        if (status.getCode() != Status.Code.UNIMPLEMENTED) {
            return false;
        }
        final String description = status.getDescription();
        return description != null && description.contains("decompressor");
    }

    private void fallbackToUncompressed(Throwable t) {
        this.statStub = this.uncompressedStub;
        if (compressionFallback.compareAndSet(false, true)) {
            logger.warn("Collector {}:{} does not accept {} stat streams ({}); sending them uncompressed from now on",
                    host, port, compression, Status.fromThrowable(t).getDescription());
        }
    }

    boolean isCompressionFallback() {
        return compressionFallback.get();
    }

    @Override
    public String toString() {
        return "StatGrpcDataSender{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", compression=" + compression +
                (compressionFallback.get() ? " (fallback: uncompressed)" : "") +
                "} " + super.toString();
    }

}