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

package com.navercorp.pinpoint.profiler.sender.grpc.stream;

import com.navercorp.pinpoint.grpc.stream.ClientCallStateStreamObserver;
import com.navercorp.pinpoint.grpc.stream.StreamUtils;
import com.navercorp.pinpoint.profiler.sender.grpc.ClientStreamingService;
import com.navercorp.pinpoint.profiler.sender.grpc.MessageDispatcher;
import com.navercorp.pinpoint.profiler.sender.grpc.Reconnector;
import com.navercorp.pinpoint.profiler.sender.grpc.ResponseStreamObserver;
import com.navercorp.pinpoint.profiler.sender.grpc.ShortLivedStreamEventListener;
import com.navercorp.pinpoint.profiler.sender.grpc.StreamId;
import com.navercorp.pinpoint.profiler.sender.grpc.StreamTask;
import com.navercorp.pinpoint.profiler.util.NamedRunnable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Short-lived stream task that reads up to {@code batchSize} messages from the queue,
 * sends them over a gRPC stream, then closes the stream gracefully.
 *
 * <p>This avoids long-lived stream issues such as sequence overflow and complex
 * state management. Each batch creates a new, independent stream which is discarded
 * after use. On normal batch completion, the stream is closed and a new one is
 * started immediately. On error, exponential backoff reconnect is used.
 *
 * @author jaehong.kim
 */
public class ShortLivedStreamTask<M, ReqT, ResT> implements StreamTask<M, ReqT> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final StreamId streamId;

    private final ClientStreamingService<ReqT, ResT> clientStreamingService;
    private final Reconnector reconnector;
    private final StreamExecutorFactory<ReqT> streamExecutorFactory;
    private final BlockingQueue<M> queue;
    private final MessageDispatcher<M, ReqT> dispatcher;
    /**
     * Maximum number of messages to send per stream before closing.
     * After this count is reached, the stream is closed gracefully and a new one starts immediately.
     * Must be positive.
     */
    private final int batchSize;
    private final Runnable onBatchComplete;

    private volatile ClientCallStateStreamObserver<ReqT> stream;
    private volatile CountDownLatch latch;
    private volatile boolean stop = false;

    public ShortLivedStreamTask(String id,
                                ClientStreamingService<ReqT, ResT> clientStreamingService,
                                Reconnector reconnector,
                                StreamExecutorFactory<ReqT> streamExecutorFactory,
                                BlockingQueue<M> queue,
                                MessageDispatcher<M, ReqT> dispatcher,
                                int batchSize,
                                Runnable onBatchComplete) {
        this.streamId = StreamId.newStreamId(id);
        this.clientStreamingService = Objects.requireNonNull(clientStreamingService, "clientStreamingService");
        this.reconnector = Objects.requireNonNull(reconnector, "reconnector");
        this.streamExecutorFactory = Objects.requireNonNull(streamExecutorFactory, "streamExecutorFactory");
        this.queue = Objects.requireNonNull(queue, "queue");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be > 0, batchSize=" + batchSize);
        }
        this.batchSize = batchSize;
        this.onBatchComplete = Objects.requireNonNull(onBatchComplete, "onBatchComplete");
    }

    @Override
    public void start() {
        this.latch = new CountDownLatch(1);
        StreamJob<ReqT> job = new StreamJob<ReqT>() {
            @Override
            public Future<?> start(final ClientCallStateStreamObserver<ReqT> requestStream) {
                Runnable runnable = ShortLivedStreamTask.this.newRunnable(requestStream, latch);
                StreamExecutor<ReqT> streamExecutor = streamExecutorFactory.newStreamExecutor();
                return streamExecutor.execute(runnable);
            }

            @Override
            public String toString() {
                return streamId.toString();
            }
        };

        ShortLivedStreamEventListener<ReqT> listener = new ShortLivedStreamEventListener<>(reconnector, job, onBatchComplete);
        ResponseStreamObserver<ReqT, ResT> response = clientStreamingService.newResponseStreamObserver(listener);
        this.stream = clientStreamingService.newStream(response);
    }

    public Runnable newRunnable(final ClientCallStateStreamObserver<ReqT> requestStream,
                                final CountDownLatch latch) {
        return new NamedRunnable(streamId.toString()) {
            @Override
            public void run() {
                dispatch(requestStream);
            }

            private void dispatch(ClientCallStateStreamObserver<ReqT> stream) {
                logger.info("dispatch start batchSize:{} {}", batchSize, this);
                int count = 0;
                try {
                    final Thread thread = Thread.currentThread();
                    while (!thread.isInterrupted() && count < batchSize) {
                        final M message = queue.take();
                        if (stream.isReady()) {
                            try {
                                dispatcher.onDispatch(stream, message);
                                count++;
                            } catch (Exception e) {
                                logger.warn("dispatch failed", e);
                            }
                        } else {
                            // Stream not ready: the message is dropped (same behavior as DefaultStreamTask).
                            // The batch loop continues to avoid blocking indefinitely on a slow stream.
                            logger.warn("stream not ready, dropping message count:{}/{}", count, batchSize);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.debug("dispatch thread interrupted {}/{}", Thread.currentThread().getName(), this);
                } catch (Throwable th) {
                    logger.error("Unexpected DispatchThread error {}/{}", Thread.currentThread().getName(), this, th);
                }

                logger.info("dispatch thread end count:{}/{} {}", count, batchSize, this);

                if (stream.isRun()) {
                    StreamUtils.onCompleted(stream, (ex) -> logger.info("stream stop", ex));
                }

                latch.countDown();
            }
        };
    }

    @Override
    public void stop() {
        logger.info("stop start {}", this.streamId);
        if (stop) {
            logger.info("already stop {}", this.streamId);
            return;
        }
        this.stop = true;

        final ClientCallStateStreamObserver<ReqT> copy = this.stream;
        if (copy != null) {
            if (copy.isRun()) {
                StreamUtils.onCompleted(copy, (th) -> logger.info("stream stop", th));
            }
        }
        final CountDownLatch latch = this.latch;
        if (latch != null) {
            try {
                latch.await(3000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("stop end {}", this.streamId);
    }

    @Override
    public String toString() {
        return "ShortLivedStreamTask{" +
                streamId +
                ", batchSize=" + batchSize +
                '}';
    }
}
