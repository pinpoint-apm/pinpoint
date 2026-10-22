/*
 * Copyright 2026 NAVER Corp.
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
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.grpc.trace.PEachUriStat;
import com.navercorp.pinpoint.grpc.trace.PStatMessage;
import com.navercorp.pinpoint.grpc.trace.StatGrpc;
import com.navercorp.pinpoint.profiler.context.grpc.config.GrpcSenderCompression;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import io.grpc.DecompressorRegistry;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * gzip stat stream over a real (Netty, loopback) transport: the grpc-encoding header and the collector's
 * rejection of an unknown encoding are only observable here.
 */
class StatGrpcDataSenderCompressionTest {
    private static final Metadata.Key<String> GRPC_ENCODING = Metadata.Key.of("grpc-encoding", Metadata.ASCII_STRING_MARSHALLER);

    private Server server;
    private StatGrpcDataSender sender;
    private final ScheduledExecutorService reconnectScheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<PStatMessage> received = new CopyOnWriteArrayList<>();
    private final List<String> encodings = new CopyOnWriteArrayList<>();

    @AfterEach
    void tearDown() {
        if (sender != null) {
            sender.close();
        }
        reconnectScheduler.shutdownNow();
        if (server != null) {
            server.shutdownNow();
        }
    }

    private int startServer(DecompressorRegistry decompressors) throws IOException {
        final StatGrpc.StatImplBase service = new StatGrpc.StatImplBase() {
            @Override
            public StreamObserver<PStatMessage> sendAgentStat(StreamObserver<Empty> responseObserver) {
                return new StreamObserver<PStatMessage>() {
                    @Override
                    public void onNext(PStatMessage value) {
                        received.add(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onCompleted() {
                        responseObserver.onNext(Empty.getDefaultInstance());
                        responseObserver.onCompleted();
                    }
                };
            }
        };
        final ServerInterceptor recordEncoding = new ServerInterceptor() {
            @Override
            public <Q, R> ServerCall.Listener<Q> interceptCall(ServerCall<Q, R> call, Metadata headers, ServerCallHandler<Q, R> next) {
                final String encoding = headers.get(GRPC_ENCODING);
                encodings.add(encoding == null ? "identity" : encoding);
                return next.startCall(call, headers);
            }
        };
        final NettyServerBuilder builder = NettyServerBuilder.forAddress(new InetSocketAddress("127.0.0.1", 0))
                .addService(ServerInterceptors.intercept(service, recordEncoding));
        if (decompressors != null) {
            builder.decompressorRegistry(decompressors);
        }
        server = builder.build().start();
        return server.getPort();
    }

    @Test
    void gzip_streamIsCompressed_smallAndLargeMessagesReceivedIntact() throws IOException {
        final int port = startServer(null);
        sender = createSender(port, GrpcSenderCompression.GZIP);

        sender.send(new TestMetric(agentStat(1)));          // below the threshold: sent uncompressed on the gzip stream
        sender.send(new TestMetric(uriStat(50)));           // above: compressed

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertThat(received).hasSize(2));
        assertThat(timestamps()).containsExactly(1L);
        assertThat(received.stream().filter(PStatMessage::hasAgentUriStat).findFirst().orElseThrow(AssertionError::new)
                .getAgentUriStat().getEachUriStatCount()).isEqualTo(50);
        assertThat(encodings).isNotEmpty().allMatch("gzip"::equals);
        assertThat(sender.isCompressionFallback()).isFalse();
        assertThat(sender.toString()).contains("compression=GZIP");
    }

    @Test
    void none_streamIsUncompressed() throws IOException {
        final int port = startServer(null);
        sender = createSender(port, GrpcSenderCompression.NONE);

        sender.send(new TestMetric(agentStat(7)));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> assertThat(timestamps()).containsExactly(7L));
        assertThat(encodings).isNotEmpty().allMatch("identity"::equals);
        assertThat(sender.toString()).contains("compression=NONE");
    }

    @Test
    void collectorWithoutGzip_reopensTheStreamUncompressed() throws IOException {
        // a collector that cannot decompress gzip closes the stream with UNIMPLEMENTED "Can't find decompressor for gzip"
        final int port = startServer(DecompressorRegistry.emptyInstance());
        sender = createSender(port, GrpcSenderCompression.GZIP);

        await().atMost(15, TimeUnit.SECONDS).until(sender::isCompressionFallback);

        sender.send(new TestMetric(agentStat(2)));
        sender.send(new TestMetric(uriStat(50)));
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> assertThat(received).hasSize(2));
        assertThat(timestamps()).containsExactly(2L);
        // the server rejects a gzip stream before any interceptor runs, so only the accepted ones are recorded
        assertThat(encodings).isNotEmpty().allMatch("identity"::equals);
        assertThat(sender.toString()).contains("fallback: uncompressed");
    }

    @Test
    void shouldCompress_bySerializedSize() {
        assertThat(StatGrpcDataSender.shouldCompress(PStatMessage.newBuilder().setAgentStat(agentStat(1)).build())).isFalse();
        assertThat(StatGrpcDataSender.shouldCompress(PStatMessage.newBuilder().setAgentUriStat(uriStat(50)).build())).isTrue();
    }

    // --- infrastructure ---

    private static PAgentStat agentStat(long timestamp) {
        return PAgentStat.newBuilder().setTimestamp(timestamp).setCollectInterval(5000).build();
    }

    private static PAgentUriStat uriStat(int uris) {
        final PAgentUriStat.Builder builder = PAgentUriStat.newBuilder().setBucketVersion(1);
        for (int i = 0; i < uris; i++) {
            builder.addEachUriStat(PEachUriStat.newBuilder()
                    .setUri("/api/v2/orders/" + i + "/items/details/summary")
                    .setTimestamp(1_000L + i));
        }
        return builder.build();
    }

    private List<Long> timestamps() {
        return received.stream()
                .filter(PStatMessage::hasAgentStat)
                .map(message -> message.getAgentStat().getTimestamp())
                .collect(Collectors.toList());
    }

    private static class TestMetric implements MetricType {
        private final GeneratedMessageV3 message;

        private TestMetric(GeneratedMessageV3 message) {
            this.message = message;
        }
    }

    private StatGrpcDataSender createSender(int port, GrpcSenderCompression compression) {
        final MessageConverter<MetricType, GeneratedMessageV3> converter = metric -> ((TestMetric) metric).message;
        final ChannelFactory channelFactory = new ChannelFactory() {
            @Override
            public String getFactoryName() {
                return "test-stat-compression";
            }

            @Override
            public ManagedChannel build(String channelName, String host, int port) {
                return NettyChannelBuilder.forAddress(host, port).usePlaintext().build();
            }

            @Override
            public ManagedChannel build(String host, int port) {
                return build("default", host, port);
            }

            @Override
            public void close() {
            }
        };
        return new StatGrpcDataSender("127.0.0.1", port, 100, converter, new ReconnectExecutor(reconnectScheduler),
                channelFactory, compression);
    }
}
