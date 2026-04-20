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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.ErrorId;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.trace.PPartialSuccess;
import com.navercorp.pinpoint.grpc.trace.PSpanMessageBatch;
import com.navercorp.pinpoint.grpc.trace.PSpanResultBatch;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.profiler.context.SpanType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Sends spans in batches via a unary RPC (SendSpanList), replacing the long-lived streaming approach.
 * <p>
 * Benefits over streaming:
 * <ul>
 *   <li>No complex stream state management or re-initialization for load balancing</li>
 *   <li>A single invalid span does not terminate the entire connection</li>
 *   <li>No sequence overflow concerns; each batch is an independent request</li>
 * </ul>
 *
 * @author emeroad
 */
public class SpanBatchGrpcDataSender extends AbstractGrpcDataSender<SpanType> {

    private final SpanGrpc.SpanFutureStub spanFutureStub;
    private final BlockingQueue<SpanType> queue;
    private final Thread sendThread;
    private final int batchSize;
    private final long flushTimeoutMillis;
    private final long batchCollectDeadLineTimeMillis;
    private final int maxConcurrentRequests;
    private final Semaphore concurrentRequestPermit;

    private final FutureCallback<PSpanResultBatch> sendCallback;

    // Reusable — safe because sendLoop runs on a single thread
    private final List<SpanType> batchBuffer;
    private final SpanMessageBatchBuilder batchMessageBuilder;

    public SpanBatchGrpcDataSender(String host, int port,
                                  int executorQueueSize,
                                  MessageConverter<SpanType, GeneratedMessageV3> messageConverter,
                                  ChannelFactory channelFactory,
                                  int batchSize,
                                  long flushTimeoutMillis,
                                  long batchCollectDeadLineTimeMillis,
                                  int maxConcurrentRequests) {
        super(host, port, messageConverter, channelFactory);
        this.spanFutureStub = SpanGrpc.newFutureStub(managedChannel);
        this.queue = new LinkedBlockingQueue<>(executorQueueSize);
        this.batchSize = batchSize;
        this.flushTimeoutMillis = flushTimeoutMillis;
        this.batchCollectDeadLineTimeMillis = batchCollectDeadLineTimeMillis;
        this.maxConcurrentRequests = maxConcurrentRequests;
        this.concurrentRequestPermit = new Semaphore(maxConcurrentRequests);
        this.sendCallback = newSendCallback(this.concurrentRequestPermit);
        this.batchBuffer = new ArrayList<>(batchSize);
        this.batchMessageBuilder = new SpanMessageBatchBuilder(messageConverter);
        this.sendThread = new Thread(this::sendLoop, "Pinpoint-SpanBatch-Sender");
        this.sendThread.setDaemon(true);
        this.sendThread.start();
    }

    @Override
    public boolean send(SpanType data) {
        if (shutdown) {
            return false;
        }
        if (queue.offer(data)) {
            return true;
        }
        final SpanType discarded = queue.poll();
        if (discarded != null) {
            if (isDebug) {
                logger.debug("discard oldest message queue size:{}", queue.size());
            } else {
                tLogger.info("discard oldest message queue size:{}", queue.size());
            }
        }
        return queue.offer(data);
    }

    private void sendLoop() {
        while (!shutdown) {
            final List<SpanType> buffer = this.batchBuffer;
            try {
                collectBatch(buffer);
                if (CollectionUtils.hasLength(buffer)) {
                    sendBatchAsync(buffer);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable e) {
                logger.warn("Error in SpanList send loop", e);
            } finally {
                buffer.clear();
            }
        }
        flushRemaining();
    }

    private void collectBatch(List<SpanType> buffer) throws InterruptedException {
        // Block until the first item arrives
        final SpanType first = queue.poll(flushTimeoutMillis, TimeUnit.MILLISECONDS);
        if (first == null) {
            return;
        }
        buffer.add(first);

        // Gather more items within the collection time window
        final long deadline = System.currentTimeMillis() + batchCollectDeadLineTimeMillis;
        while (buffer.size() < batchSize) {
            final long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                break;
            }
            final SpanType item = queue.poll(remaining, TimeUnit.MILLISECONDS);
            if (item == null) {
                break;
            }
            buffer.add(item);
        }
    }

    private void flushRemaining() {
        final List<SpanType> remaining = new ArrayList<>();
        queue.drainTo(remaining);
        if (!remaining.isEmpty()) {
            logger.info("Flushing {} remaining spans on shutdown", remaining.size());
            sendBatchAsync(remaining);
        }
        awaitInFlightRequests();
    }

    private void awaitInFlightRequests() {
        try {
            if (!concurrentRequestPermit.tryAcquire(maxConcurrentRequests, 3, TimeUnit.SECONDS)) {
                logger.warn("Timed out waiting for in-flight span requests to complete");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendBatchAsync(List<SpanType> batch) {
        final PSpanMessageBatch spanMessageBatch = batchMessageBuilder.buildBatch(batch);
        if (spanMessageBatch.getSpanCount() == 0) {
            return;
        }

        if (isDebug) {
            logger.debug("sendSpanList size={}", spanMessageBatch.getSpanCount());
        }

        try {
            if (!concurrentRequestPermit.tryAcquire(flushTimeoutMillis, TimeUnit.MILLISECONDS)) {
                tLogger.warn("sendSpanList skipped: no available permits within {}ms", flushTimeoutMillis);
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        final ListenableFuture<PSpanResultBatch> future = spanFutureStub.sendSpanBatch(spanMessageBatch);
        Futures.addCallback(future, sendCallback, MoreExecutors.directExecutor());
    }

    private FutureCallback<PSpanResultBatch> newSendCallback(final Semaphore concurrentRequestPermit) {
        return new FutureCallback<PSpanResultBatch>() {
            @Override
            public void onSuccess(PSpanResultBatch response) {
                concurrentRequestPermit.release();
                handleResponse(response);
            }

            @Override
            public void onFailure(Throwable t) {
                concurrentRequestPermit.release();
                tLogger.warn("sendSpanList failed", t);
            }
        };
    }

    private void handleResponse(PSpanResultBatch response) {
        if (response.hasPartialSuccess()) {
            final PPartialSuccess partialSuccess = response.getPartialSuccess();
            final long rejectedSpans = partialSuccess.getRejectedSpans();
            final ErrorId errorId = ErrorId.of(partialSuccess.getErrorId());
            if (rejectedSpans > 0) {
                tLogger.warn("sendSpanList partial success: rejectedSpans={}, errorId={}, errorMessage={}",
                        rejectedSpans, errorId, partialSuccess.getErrorMessage());
            } else if (!partialSuccess.getErrorMessage().isEmpty()) {
                tLogger.info("sendSpanList warning: errorId={}, {}", errorId, partialSuccess.getErrorMessage());
            }
        }
    }

    @Override
    public void close() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        logger.info("Close SpanBatchGrpcDataSender host={} port={}", host, port);
        sendThread.interrupt();
        try {
            sendThread.join(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        releaseChannel();
    }

    @Override
    public String toString() {
        return "SpanBatchGrpcDataSender{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", batchSize=" + batchSize +
                ", flushTimeoutMillis=" + flushTimeoutMillis +
                ", batchCollectDeadLineTimeMillis=" + batchCollectDeadLineTimeMillis +
                ", maxConcurrentRequests=" + maxConcurrentRequests +
                '}';
    }
}