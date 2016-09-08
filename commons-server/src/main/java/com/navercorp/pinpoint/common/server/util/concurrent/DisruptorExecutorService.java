/*
 * Copyright 2016 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.util.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkerPool;
import com.lmax.disruptor.dsl.BasicExecutor;

/**
 * ExecutorService made of disruptor.
 * <p>
 * related Shutdown methods occasionally not working,
 * So recommend to use when has lifecycle the same as application lifecycle.
 *
 * @author Taejin Koo
 */
public class DisruptorExecutorService extends AbstractExecutorService {

    private final RingBuffer<RunnableEvent> ringBuffer;
    private final WorkerPool<RunnableEvent> workerPool;

    private final AtomicBoolean isShutDowned = new AtomicBoolean(false);

    public DisruptorExecutorService(int poolSize, int queueSize, ThreadFactory threadFactory) {
        this(poolSize, queueSize, threadFactory, new LiteBlockingWaitStrategy(), true);
    }

    public DisruptorExecutorService(int poolSize, int queueSize, ThreadFactory threadFactory, WaitStrategy waitStrategy) {
        this(poolSize, queueSize, threadFactory, waitStrategy, true);
    }

    public DisruptorExecutorService(int poolSize, int queueSize, ThreadFactory threadFactory, boolean supportMultiProducer) {
        this(poolSize, queueSize, threadFactory, new LiteBlockingWaitStrategy(), supportMultiProducer);
    }

    public DisruptorExecutorService(int poolSize, int queueSize, ThreadFactory threadFactory, WaitStrategy waitStrategy, boolean supportMultiProducer) {
        if (queueSize < 1) {
            throw new IllegalArgumentException("bufferSize must not be less than 1");
        }

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
        WorkerPool<RunnableEvent> workerPool = new WorkerPool(ringBuffer, ringBuffer.newBarrier(), new IgnoreExceptionHandler(), handlers);
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
        if (command == null) {
            throw new NullPointerException();
        }
        if (isShutDowned.get()) {
            throw new RejectedExecutionException("Task " + command.toString() + " rejected. Caused:already shutDowned.");
        }

        boolean success = ringBuffer.tryPublishEvent(new EventTranslator<RunnableEvent>() {
            @Override
            public void translateTo(RunnableEvent event, long sequence) {
                event.set(command);
            }

        });

        if (!success) {
            throw new RejectedExecutionException("Task " + command.toString() + " rejected. Caused: queue is overflow.");
        }
    }

    @Override
    public void shutdown() {
        if (isShutDowned.compareAndSet(false, true)) {
            workerPool.drainAndHalt();
        }
    }

    @Override
    public boolean isShutdown() {
        return isShutDowned.get();
    }

    @Override
    public List<Runnable> shutdownNow() {
        if (isShutDowned.compareAndSet(false, true)) {
            workerPool.halt();

            Sequence[] sequence = workerPool.getWorkerSequences();
            if (sequence == null || sequence.length == 0) {
                return Collections.emptyList();
            }

            long startPosition = sequence[sequence.length - 1].get();
            long endPosition = ringBuffer.getCursor();

            if (startPosition >= endPosition) {
                return Collections.emptyList();
            }

            try {
                List<Runnable> notExecutedRunnableList = new ArrayList((int) (endPosition - startPosition));
                for (long i = startPosition; i < endPosition; i++) {
                    notExecutedRunnableList.add(ringBuffer.get(i).getValue());
                }

                return notExecutedRunnableList;
            } catch (Exception e) {
                // do nothing
            }

            return Collections.emptyList();
        }

        return Collections.emptyList();
    }

    @Override
    public boolean isTerminated() {
        if (!isShutDowned.get()) {
            return false;
        }

        return !hasNotExecutedRunnable();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        final long timeOutAt = System.currentTimeMillis() + unit.toMillis(timeout);
        // Busy spin
        while (hasNotExecutedRunnable()) {
            if (timeout >= 0 && System.currentTimeMillis() > timeOutAt) {
                return false;
            }
        }

        return true;
    }

    private boolean hasNotExecutedRunnable() {
        final long cursor = ringBuffer.getCursor();
        if (cursor > ringBuffer.getMinimumGatingSequence()) {
            return true;
        }
        return false;
    }

    private static class RunnableExecuteHandler implements WorkHandler<RunnableEvent> {

        @Override
        public void onEvent(RunnableEvent runnableEvent) throws Exception {
            runnableEvent.getValue().run();
        }

    }

}
