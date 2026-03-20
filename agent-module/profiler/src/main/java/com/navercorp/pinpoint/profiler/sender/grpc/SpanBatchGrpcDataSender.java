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
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.ErrorId;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.trace.PPartialSuccess;
import com.navercorp.pinpoint.grpc.trace.PSpanMessageBatch;
import com.navercorp.pinpoint.grpc.trace.PSpanResultBatch;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.profiler.context.SpanType;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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

    private final SpanGrpc.SpanStub spanStub;
    private final BlockingQueue<SpanType> queue;
    private final Thread sendThread;
    private final int batchSize;
    private final long flushTimeoutMillis;
    private final long batchCollectDeadLineTimeMillis;

    // Reusable — safe because sendLoop runs on a single thread
    private final List<SpanType> batchBuffer;
    private final SpanMessageBatchBuilder batchMessageBuilder;

    public SpanBatchGrpcDataSender(String host, int port,
                                  int executorQueueSize,
                                  MessageConverter<SpanType, GeneratedMessageV3> messageConverter,
                                  ChannelFactory channelFactory,
                                  int batchSize,
                                  long flushTimeoutMillis,
                                  long batchCollectDeadLineTimeMillis) {
        super(host, port, messageConverter, channelFactory);
        this.spanStub = SpanGrpc.newStub(managedChannel);
        this.queue = new LinkedBlockingQueue<>(executorQueueSize);
        this.batchSize = batchSize;
        this.flushTimeoutMillis = flushTimeoutMillis;
        this.batchCollectDeadLineTimeMillis = batchCollectDeadLineTimeMillis;
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
        final boolean added = queue.offer(data);
        if (!added) {
            if (isDebug) {
                logger.debug("reject message queue size:{}", queue.size());
            } else {
                tLogger.info("reject message queue size : {}", queue.size());
            }
        }
        return added;
    }

    private void sendLoop() {
        while (!shutdown) {
            final List<SpanType> buffer = this.batchBuffer;
            try {
                collectBatch(buffer);
                if (CollectionUtils.hasLength(buffer)) {
                    sendBatch(buffer);
                    buffer.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable e) {
                logger.warn("Error in SpanList send loop", e);
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
            sendBatch(remaining);
        }
    }

    private void sendBatch(List<SpanType> batch) {
        final PSpanMessageBatch spanMessageList = batchMessageBuilder.buildBatch(batch);
        if (spanMessageList.getSpanCount() == 0) {
            return;
        }

        if (isDebug) {
            logger.debug("sendSpanList size={}", spanMessageList.getSpanCount());
        }

        spanStub.sendSpanBatch(spanMessageList, new StreamObserver<PSpanResultBatch>() {
            @Override
            public void onNext(PSpanResultBatch response) {
                if (response.hasPartialSuccess()) {
                    final PPartialSuccess partialSuccess = response.getPartialSuccess();
                    final long rejectedSpans = partialSuccess.getRejectedSpans();
                    final ErrorId errorId = ErrorId.of(partialSuccess.getErrorId());
                    if (rejectedSpans > 0) {
                        logger.warn("sendSpanList partial success: rejectedSpans={}, errorId={}, errorMessage={}",
                                rejectedSpans, errorId, partialSuccess.getErrorMessage());
                    } else if (!partialSuccess.getErrorMessage().isEmpty()) {
                        logger.info("sendSpanList warning: errorId={}, {}", errorId, partialSuccess.getErrorMessage());
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("sendSpanList error", t);
            }

            @Override
            public void onCompleted() {
                if (isDebug) {
                    logger.debug("sendSpanList completed");
                }
            }
        });
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
                '}';
    }
}