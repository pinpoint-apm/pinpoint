/*
 * Copyright 2025 NAVER Corp.
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
import com.navercorp.pinpoint.grpc.trace.PPartialSuccess;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.PSpanMessageBatch;
import com.navercorp.pinpoint.grpc.trace.PSpanResultBatch;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.profiler.context.SpanType;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class SpanBatchGrpcDataSenderTest {

    private Server server;
    private String serverName;
    private TestSpanBatchService service;
    private SpanBatchGrpcDataSender sender;

    private void setUpServer(TestSpanBatchService service) throws IOException {
        serverName = InProcessServerBuilder.generateName();
        this.service = service;
        server = InProcessServerBuilder
                .forName(serverName)
                .addService(service)
                .build()
                .start();
    }

    @AfterEach
    void tearDown() {
        if (sender != null) {
            sender.close();
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    void sendSingleSpan_receivedByServer() throws IOException {
        setUpServer(new TestSpanBatchService());
        sender = createSender(100, 10, 200, 100, 5);

        boolean result = sender.send(new TestSpan(1));

        assertThat(result).isTrue();
        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(totalSpanCount()).isEqualTo(1));

        List<Integer> apiIds = collectAllApiIds();
        assertThat(apiIds).containsExactly(1);
    }

    @Test
    void sendMultipleSpans_batchedTogether() throws IOException {
        setUpServer(new TestSpanBatchService());
        sender = createSender(100, 10, 500, 300, 5);

        for (int i = 1; i <= 5; i++) {
            sender.send(new TestSpan(i));
        }

        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(totalSpanCount()).isEqualTo(5));

        assertThat(service.receivedBatches.size()).isLessThanOrEqualTo(2);

        List<Integer> apiIds = collectAllApiIds();
        assertThat(apiIds).containsExactlyInAnyOrder(1, 2, 3, 4, 5);
    }

    @Test
    void batchSizeLimit_doesNotExceedConfiguredSize() throws IOException {
        setUpServer(new TestSpanBatchService());
        sender = createSender(100, 3, 200, 100, 5);

        for (int i = 1; i <= 7; i++) {
            sender.send(new TestSpan(i));
        }

        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(totalSpanCount()).isEqualTo(7));

        for (PSpanMessageBatch batch : service.receivedBatches) {
            assertThat(batch.getSpanCount()).isLessThanOrEqualTo(3);
        }
        assertThat(service.receivedBatches.size()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void queueOverflow_discardsOldestSpan() throws Exception {
        setUpServer(TestSpanBatchService.blocking());

        sender = createSender(1, 1, 5000, 50, 1);

        sender.send(new TestSpan(1));
        assertThat(service.getRequestArrivedLatch().await(3, TimeUnit.SECONDS)).isTrue();

        sender.send(new TestSpan(2));
        Thread.sleep(300);

        sender.send(new TestSpan(3));
        sender.send(new TestSpan(4));
        sender.send(new TestSpan(5));

        service.unblock();

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(totalSpanCount()).isGreaterThanOrEqualTo(2));

        List<Integer> apiIds = collectAllApiIds();
        assertThat(apiIds).contains(1);
        assertThat(apiIds).doesNotContain(3, 4);
    }

    @Test
    void concurrentRequestLimiting() throws Exception {
        setUpServer(new TestSpanBatchService(false, null, () -> false, 500));

        sender = createSender(100, 1, 200, 50, 2);

        for (int i = 1; i <= 5; i++) {
            sender.send(new TestSpan(i));
        }

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(service.totalRequestCount.get()).isGreaterThanOrEqualTo(3));

        assertThat(service.maxConcurrent.get()).isLessThanOrEqualTo(2);
    }

    @Test
    void serverError_handledGracefully() throws IOException {
        AtomicBoolean fail = new AtomicBoolean(true);
        setUpServer(new TestSpanBatchService(false, null, fail::get, 0));

        sender = createSender(100, 1, 200, 50, 5);

        for (int i = 1; i <= 3; i++) {
            sender.send(new TestSpan(i));
        }

        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(service.totalRequestCount.get()).isGreaterThanOrEqualTo(3));

        fail.set(false);

        sender.send(new TestSpan(100));

        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<Integer> apiIds = collectAllApiIds();
                    assertThat(apiIds).contains(100);
                });
    }

    @Test
    void partialSuccess_handledProperly() throws IOException {
        PSpanResultBatch partialResponse = PSpanResultBatch.newBuilder()
                .setPartialSuccess(PPartialSuccess.newBuilder()
                        .setRejectedSpans(2)
                        .setErrorId(42)
                        .setErrorMessage("test partial rejection")
                        .build())
                .build();
        AtomicBoolean usePartialResponse = new AtomicBoolean(true);
        setUpServer(new TestSpanBatchService(false,
                () -> usePartialResponse.get() ? partialResponse : null,
                () -> false, 0));

        sender = createSender(100, 5, 200, 100, 5);

        for (int i = 1; i <= 5; i++) {
            sender.send(new TestSpan(i));
        }

        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(totalSpanCount()).isEqualTo(5));

        usePartialResponse.set(false);
        sender.send(new TestSpan(99));

        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<Integer> apiIds = collectAllApiIds();
                    assertThat(apiIds).contains(99);
                });
    }

    @Test
    void close_allSentSpansReceived() throws IOException {
        setUpServer(new TestSpanBatchService());
        sender = createSender(100, 5, 200, 100, 5);

        for (int i = 1; i <= 10; i++) {
            sender.send(new TestSpan(i));
        }

        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(totalSpanCount()).isEqualTo(10));

        sender.close();
        sender = null;

        List<Integer> apiIds = collectAllApiIds();
        assertThat(apiIds).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    void sendAfterShutdown_returnsFalse() throws IOException {
        setUpServer(new TestSpanBatchService());
        sender = createSender(100, 10, 200, 100, 5);
        sender.close();

        boolean result = sender.send(new TestSpan(1));

        assertThat(result).isFalse();
        sender = null;
    }

    // --- Test infrastructure ---

    private SpanBatchGrpcDataSender createSender(int executorQueueSize, int batchSize,
                                                  long flushTimeoutMillis, long batchCollectDeadLineTimeMillis,
                                                  int maxConcurrentRequests) {
        MessageConverter<SpanType, GeneratedMessageV3> converter = message -> {
            TestSpan span = (TestSpan) message;
            return PSpan.newBuilder().setApiId(span.id).build();
        };

        ChannelFactory channelFactory = new ChannelFactory() {
            @Override
            public String getFactoryName() {
                return "test-span-batch";
            }

            @Override
            public ManagedChannel build(String channelName, String host, int port) {
                return InProcessChannelBuilder.forName(serverName).build();
            }

            @Override
            public ManagedChannel build(String host, int port) {
                return build("default", host, port);
            }

            @Override
            public void close() {
            }
        };

        return new SpanBatchGrpcDataSender("localhost", 0, executorQueueSize,
                converter, channelFactory, batchSize, flushTimeoutMillis,
                batchCollectDeadLineTimeMillis, maxConcurrentRequests);
    }

    private int totalSpanCount() {
        return service.receivedBatches.stream()
                .mapToInt(PSpanMessageBatch::getSpanCount)
                .sum();
    }

    private List<Integer> collectAllApiIds() {
        return service.receivedBatches.stream()
                .flatMap(batch -> batch.getSpanList().stream())
                .filter(PSpanMessage::hasSpan)
                .map(msg -> msg.getSpan().getApiId())
                .collect(Collectors.toList());
    }

    // --- Test doubles ---

    static class TestSpan implements SpanType {
        final int id;

        TestSpan(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "TestSpan{id=" + id + '}';
        }
    }

    static class TestSpanBatchService extends SpanGrpc.SpanImplBase {
        final List<PSpanMessageBatch> receivedBatches = new CopyOnWriteArrayList<>();
        final AtomicInteger totalRequestCount = new AtomicInteger();
        final AtomicInteger maxConcurrent = new AtomicInteger();
        final AtomicInteger currentConcurrent = new AtomicInteger();

        final CountDownLatch requestArrivedLatch = new CountDownLatch(1);
        final CountDownLatch serverBlockLatch;
        final Supplier<PSpanResultBatch> responseSupplier;
        final BooleanSupplier failMode;
        final long responseDelayMillis;

        TestSpanBatchService() {
            this(false, null, () -> false, 0);
        }

        TestSpanBatchService(boolean blockServer,
                             Supplier<PSpanResultBatch> responseSupplier,
                             BooleanSupplier failMode,
                             long responseDelayMillis) {
            this.serverBlockLatch = new CountDownLatch(blockServer ? 1 : 0);
            this.responseSupplier = responseSupplier;
            this.failMode = failMode;
            this.responseDelayMillis = responseDelayMillis;
        }

        static TestSpanBatchService blocking() {
            return new TestSpanBatchService(true, null, () -> false, 0);
        }

        CountDownLatch getRequestArrivedLatch() {
            return requestArrivedLatch;
        }

        void unblock() {
            serverBlockLatch.countDown();
        }

        @Override
        public void sendSpanBatch(PSpanMessageBatch request,
                                  StreamObserver<PSpanResultBatch> responseObserver) {
            int concurrent = currentConcurrent.incrementAndGet();
            maxConcurrent.updateAndGet(prev -> Math.max(prev, concurrent));
            totalRequestCount.incrementAndGet();
            receivedBatches.add(request);

            requestArrivedLatch.countDown();

            try {
                serverBlockLatch.await();

                if (responseDelayMillis > 0) {
                    Thread.sleep(responseDelayMillis);
                }

                if (failMode.getAsBoolean()) {
                    responseObserver.onError(
                            Status.INTERNAL.withDescription("test error").asException());
                    return;
                }

                PSpanResultBatch response = (responseSupplier != null) ? responseSupplier.get() : null;
                if (response == null) {
                    response = PSpanResultBatch.getDefaultInstance();
                }
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                responseObserver.onError(Status.CANCELLED.asException());
            } finally {
                currentConcurrent.decrementAndGet();
            }
        }
    }
}
