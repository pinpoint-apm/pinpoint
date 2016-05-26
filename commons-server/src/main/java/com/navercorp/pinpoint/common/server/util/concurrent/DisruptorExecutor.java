package com.navercorp.pinpoint.common.server.util.concurrent;

import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.FatalExceptionHandler;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkerPool;
import com.lmax.disruptor.dsl.BasicExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;

/**
 * @author Taejin Koo
 */
public class DisruptorExecutor implements Executor {

    private final int poolSize;
    private final RingBuffer<RunnableEvent> ringBuffer;
    private final WorkerPool<RunnableEvent> workerPool;

    public DisruptorExecutor(int poolSize, int queueSize, ThreadFactory threadFactory) {
        this(poolSize, queueSize, threadFactory, new LiteBlockingWaitStrategy(), true);
    }

    public DisruptorExecutor(int poolSize, int queueSize, ThreadFactory threadFactory, WaitStrategy waitStrategy) {
        this(poolSize, queueSize, threadFactory, waitStrategy, true);
    }

    public DisruptorExecutor(int poolSize, int queueSize, ThreadFactory threadFactory, boolean supportMultiProducer) {
        this(poolSize, queueSize, threadFactory, new LiteBlockingWaitStrategy(), supportMultiProducer);
    }

    public DisruptorExecutor(int poolSize, int queueSize, ThreadFactory threadFactory, WaitStrategy waitStrategy, boolean supportMultiProducer) {
        if (queueSize < 1) {
            throw new IllegalArgumentException("bufferSize must not be less than 1");
        }

        this.poolSize = poolSize;

        RingBuffer<RunnableEvent> ringBuffer = createRingBuffer(supportMultiProducer, queueSize, waitStrategy);
        this.ringBuffer = ringBuffer;

        WorkerPool<RunnableEvent> workerPool = createWorkerPool(poolSize, ringBuffer);
        this.workerPool = workerPool;

        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        workerPool.start(new BasicExecutor(threadFactory));
    }

    private RingBuffer<RunnableEvent> createRingBuffer(boolean supportMultiProducer, int queueSize, WaitStrategy waitStrategy) {
        if (supportMultiProducer) {
            return RingBuffer.createMultiProducer(RunnableEvent.EVENT_FACTORY, queueSize, waitStrategy);
        } else {
            return RingBuffer.createSingleProducer(RunnableEvent.EVENT_FACTORY, queueSize, waitStrategy);
        }
    }

    private WorkerPool<RunnableEvent> createWorkerPool(int poolSize, RingBuffer ringBuffer) {
        RunnableExecuteHandler[] handlers = prepareRunnableExecuteHandler(poolSize);
        WorkerPool<RunnableEvent> workerPool = new WorkerPool(ringBuffer, ringBuffer.newBarrier(), new FatalExceptionHandler(), handlers);

        return workerPool;
    }

    private RunnableExecuteHandler[] prepareRunnableExecuteHandler(int poolSize) {
        RunnableExecuteHandler[] handlers = new RunnableExecuteHandler[poolSize];
        for (int i = 0; i < poolSize; i++) {
            handlers[i] = new RunnableExecuteHandler();
        }

        return handlers;
    }

    @Override
    public void execute(final Runnable command) {
        boolean success = ringBuffer.tryPublishEvent(new EventTranslator<RunnableEvent>() {
            @Override
            public void translateTo(RunnableEvent event, long sequence) {
                event.set(command);
            }

        });

        if (!success) {
            throw new RejectedExecutionException("Task " + command.toString() + " rejected");
        }
    }

    public void shutdown() {
        // FIXME : caution thread not cleared.
        // The threads do not receive a termination message if there is a live.
        if (workerPool != null) {
            workerPool.drainAndHalt();
        }
    }

    private class RunnableExecuteHandler implements WorkHandler<RunnableEvent> {

        @Override
        public void onEvent(RunnableEvent runnableEvent) throws Exception {
            runnableEvent.getValue().run();
        }

    }

}
