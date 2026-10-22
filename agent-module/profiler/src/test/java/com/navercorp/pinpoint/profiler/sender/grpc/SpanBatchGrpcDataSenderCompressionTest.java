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

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.PSpanMessageBatch;
import com.navercorp.pinpoint.grpc.trace.PSpanResultBatch;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.context.grpc.config.GrpcSenderCompression;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * gzip span batches over a real (Netty, loopback) transport: the in-process transport used by
 * {@link SpanBatchGrpcDataSenderTest} never compresses, so the grpc-encoding header is only
 * observable here.
 */
class SpanBatchGrpcDataSenderCompressionTest {

    private static final Metadata.Key<String> GRPC_ENCODING = Metadata.Key.of("grpc-encoding", Metadata.ASCII_STRING_MARSHALLER);

    private Server server;
    private SpanBatchGrpcDataSender sender;
    private final List<PSpanMessageBatch> received = new CopyOnWriteArrayList<>();
    private final List<String> encodings = new CopyOnWriteArrayList<>();
    private final AtomicInteger calls = new AtomicInteger();

    @AfterEach
    void tearDown() {
        if (sender != null) {
            sender.close();
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    private int startServer(DecompressorRegistry decompressors) throws IOException {
        final SpanGrpc.SpanImplBase service = new SpanGrpc.SpanImplBase() {
            @Override
            public void sendSpanBatch(PSpanMessageBatch request, StreamObserver<PSpanResultBatch> responseObserver) {
                received.add(request);
                responseObserver.onNext(PSpanResultBatch.getDefaultInstance());
                responseObserver.onCompleted();
            }
        };
        final ServerInterceptor recordEncoding = new ServerInterceptor() {
            @Override
            public <Q, R> ServerCall.Listener<Q> interceptCall(ServerCall<Q, R> call, Metadata headers, ServerCallHandler<Q, R> next) {
                calls.incrementAndGet();
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
    void gzip_sendsCompressedBatches_receivedIntact() throws IOException {
        final int port = startServer(null);
        sender = createSender(port, GrpcSenderCompression.GZIP);

        for (int i = 1; i <= 5; i++) {
            sender.send(new SpanBatchGrpcDataSenderTest.TestSpan(i));
        }

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(apiIds()).containsExactlyInAnyOrder(1, 2, 3, 4, 5));
        assertThat(encodings).isNotEmpty().allMatch("gzip"::equals);
        assertThat(sender.isCompressionFallback()).isFalse();
        assertThat(sender.toString()).contains("compression=GZIP");
    }

    @Test
    void none_sendsUncompressedBatches() throws IOException {
        final int port = startServer(null);
        sender = createSender(port, GrpcSenderCompression.NONE);

        sender.send(new SpanBatchGrpcDataSenderTest.TestSpan(7));

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(apiIds()).containsExactly(7));
        assertThat(encodings).isNotEmpty().allMatch("identity"::equals);
    }

    @Test
    void collectorWithoutGzip_fallsBackToUncompressed() throws IOException {
        // a collector that cannot decompress gzip answers UNIMPLEMENTED "Can't find decompressor for gzip"
        final int port = startServer(DecompressorRegistry.emptyInstance());
        sender = createSender(port, GrpcSenderCompression.GZIP);

        sender.send(new SpanBatchGrpcDataSenderTest.TestSpan(1));
        await().atMost(5, TimeUnit.SECONDS).until(sender::isCompressionFallback);
        assertThat(received).isEmpty();   // the compressed batch was rejected

        sender.send(new SpanBatchGrpcDataSenderTest.TestSpan(2));
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(apiIds()).containsExactly(2));
        // the server rejects a gzip call before any interceptor runs, so only the accepted ones are recorded
        assertThat(encodings).isNotEmpty().allMatch("identity"::equals);
        assertThat(sender.toString()).contains("fallback: uncompressed");
    }

    // --- infrastructure ---

    private List<Integer> apiIds() {
        return received.stream()
                .flatMap(batch -> batch.getSpanList().stream())
                .filter(PSpanMessage::hasSpan)
                .map(message -> message.getSpan().getApiId())
                .collect(Collectors.toList());
    }

    private SpanBatchGrpcDataSender createSender(int port, GrpcSenderCompression compression) {
        final MessageConverter<SpanType, GeneratedMessageV3> converter = message -> {
            final SpanBatchGrpcDataSenderTest.TestSpan span = (SpanBatchGrpcDataSenderTest.TestSpan) message;
            return PSpan.newBuilder().setApiId(span.id).build();
        };
        final ChannelFactory channelFactory = new ChannelFactory() {
            @Override
            public String getFactoryName() {
                return "test-span-batch-compression";
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
        return new SpanBatchGrpcDataSender("127.0.0.1", port, 100, converter, channelFactory,
                10, 200, 100, 5, compression);
    }
}
