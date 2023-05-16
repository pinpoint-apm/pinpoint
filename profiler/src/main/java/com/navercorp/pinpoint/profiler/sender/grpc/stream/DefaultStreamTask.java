package com.navercorp.pinpoint.profiler.sender.grpc.stream;

import java.util.Objects;
import com.navercorp.pinpoint.profiler.sender.grpc.ClientStreamingService;
import com.navercorp.pinpoint.profiler.sender.grpc.MessageDispatcher;
import com.navercorp.pinpoint.profiler.sender.grpc.StreamId;
import com.navercorp.pinpoint.profiler.sender.grpc.StreamState;
import com.navercorp.pinpoint.profiler.sender.grpc.StreamTask;
import com.navercorp.pinpoint.profiler.util.NamedRunnable;
import io.grpc.stub.ClientCallStreamObserver;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DefaultStreamTask<M, ReqT, ResT> implements StreamTask<M, ReqT> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final StreamId streamId;

    private final ClientStreamingService<ReqT, ResT> clientStreamingService;
    private final StreamExecutorFactory<ReqT> streamExecutorFactory;
    private final BlockingQueue<M> queue;
    private final MessageDispatcher<M, ReqT> dispatcher;
    private final StreamState failState;

    private volatile ClientCallStreamObserver<ReqT> stream;
    private volatile CountDownLatch latch;
    private volatile boolean stop = false;


    public DefaultStreamTask(String id, ClientStreamingService<ReqT, ResT> clientStreamingService,
                             StreamExecutorFactory<ReqT> streamExecutorFactory,
                             BlockingQueue<M> queue, MessageDispatcher<M, ReqT> dispatcher, StreamState failState) {
        this.streamId = StreamId.newStreamId(id);
        this.clientStreamingService = Objects.requireNonNull(clientStreamingService, "clientStreamingService");
        this.streamExecutorFactory = Objects.requireNonNull(streamExecutorFactory, "streamExecutorFactory");
        this.queue = Objects.requireNonNull(queue, "queue");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        this.failState = Objects.requireNonNull(failState, "failState");
    }


    @Override
    public void start() {
        this.latch = new CountDownLatch(1);
        StreamJob<ReqT> job = new StreamJob<ReqT>() {
            @Override
            public Future<?> start(final ClientCallStreamObserver<ReqT> requestStream) {
                Runnable runnable = DefaultStreamTask.this.newRunnable(requestStream, latch);
                StreamExecutor<ReqT> streamExecutor = streamExecutorFactory.newStreamExecutor();
                return streamExecutor.execute(runnable);
            }

            @Override
            public String toString() {
                return streamId.toString();
            }
        };

        this.stream = clientStreamingService.newStream(job);
    }

    enum FinishStatus {
        UNKNOWN,
        INTERRUPTED,
        ISREADY_ERROR
    }

    public Runnable newRunnable(final ClientCallStreamObserver<ReqT> requestStream, final CountDownLatch latch) {
        return new NamedRunnable(streamId.toString()) {
            @Override
            public void run() {
                dispatch(requestStream);
            }

            private void dispatch(ClientCallStreamObserver<ReqT> stream) {
                logger.info("dispatch start {}", this);
                FinishStatus status = FinishStatus.UNKNOWN;

                try {
//            while (true) {
                    final Thread thread = Thread.currentThread();
                    while (!thread.isInterrupted()) {
                        final M message = queue.take();
                        if (stream.isReady()) {
                            try {
                                dispatcher.onDispatch(stream, message);
                            } catch (Exception e) {
                                logger.warn("dispatch failed", e);
                            }
                            failState.success();
                        } else {
                            failState.fail();

                            if (failState.isFailure()) {
                                logger.info("isReadyState error, Trigger stream.cancel {}", this);
                                stream.cancel("isReadyState error", new Exception("isReadyState error"));
                                status = FinishStatus.ISREADY_ERROR;
                                break;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.debug("dispatch thread interrupted {}/{}", Thread.currentThread().getName(), this);
                    status = FinishStatus.INTERRUPTED;
                } catch (Throwable th) {
                    logger.error("Unexpected DispatchThread error {}/{}", Thread.currentThread().getName(), this, th);
                }

                logger.info("dispatch thread end status:{} {}", status, this);
                latch.countDown();
            }

        };
    }


    @Override
    public void stop() {
        logger.info("stop start {}", this.streamId);

        this.stop = true;

        final ClientCallStreamObserver<ReqT> copy = this.stream;
        if (copy != null) {
//            copy.cancel("stream stop", new Exception("stream stop"));
            copy.onCompleted();
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

    public boolean isStop() {
        return stop;
    }

    @Override
    public String toString() {
        return "DefaultStreamTask{" +
                streamId +
                '}';
    }
}
