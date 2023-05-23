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

package com.navercorp.pinpoint.common.profiler.concurrent.executor;

import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author emeroad
 */
public class AsyncQueueingExecutor<T> {

    private final Logger logger;

    private final LinkedBlockingQueue<T> queue;
    private final AtomicBoolean isRun = new AtomicBoolean(true);
    private final Thread executeThread;
    private final String executorName;

    private final int maxDrainSize;
    // Caution. single thread only. this Collection is simpler than ArrayList.
    private final Collection<T> drain;

    private final MultiConsumer<T> consumer;

    public AsyncQueueingExecutor(int queueSize, String executorName, Consumer<T> consumer) {
        this(queueSize, executorName, new SingleConsumer<>(consumer));
    }

    public AsyncQueueingExecutor(int queueSize, String executorName, MultiConsumer<T> consumer) {
        Objects.requireNonNull(executorName, "executorName");

        this.logger = LogManager.getLogger(this.getClass().getName() + "@" + executorName);

        // BEFORE executeThread start
        this.maxDrainSize = 10;
        this.drain = new UnsafeArrayCollection<>(maxDrainSize);
        this.queue = new LinkedBlockingQueue<>(queueSize);

        this.executeThread = this.createExecuteThread(executorName);
        this.executorName = executeThread.getName();

        this.consumer = Objects.requireNonNull(consumer, "consumer");
    }

    private Thread createExecuteThread(String executorName) {
        final ThreadFactory threadFactory = new PinpointThreadFactory(executorName, true);
        Thread thread = threadFactory.newThread(this::doAccept);
        thread.start();
        return thread;
    }

    private void doAccept() {
        long timeout = 2000;
        drainStartEntry:
        while (isRun()) {
            try {
                final Collection<T> dtoList = getDrainQueue();
                final int drainSize = takeN(dtoList, this.maxDrainSize);
                if (drainSize > 0) {
                    doAccept(dtoList);
                    continue;
                }

                while (isRun()) {
                    final T dto = takeOne(timeout);
                    if (dto != null) {
                        doAccept(dto);
                        continue drainStartEntry;
                    } else {
                        pollTimeout(timeout);
                    }
                }
            } catch (Throwable th) {
                logger.warn("{} doExecute(). Unexpected Error. Cause:{}", executorName, th.getMessage(), th);
            }
        }
        flushQueue();
    }

    private void flushQueue() {
        boolean debugEnabled = logger.isDebugEnabled();
        if (debugEnabled) {
            logger.debug("Loop is stop.");
        }
        while (true) {
            final Collection<T> elementList = getDrainQueue();
            int drainSize = takeN(elementList, this.maxDrainSize);
            if (drainSize == 0) {
                break;
            }
            if (debugEnabled) {
                logger.debug("flushData size {}", drainSize);
            }
            doAccept(elementList);
        }
    }

    private T takeOne(long timeout) {
        try {
            return queue.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private int takeN(Collection<T> drain, int maxDrainSize) {
        return queue.drainTo(drain, maxDrainSize);
    }

    protected void pollTimeout(long timeout) {
        // do nothing
    }

    public boolean execute(T data) {
        if (data == null) {
            logger.warn("execute(). data is null");
            return false;
        }
        if (!isRun.get()) {
            logger.warn("{} is shutdown. discard data:{}", executorName, data);
            return false;
        }
        boolean offer = queue.offer(data);
        if (!offer) {
            logger.warn("{} Drop data. queue is full. size:{}", executorName, queue.size());
        }
        return offer;
    }


    private void doAccept(Collection<T> elements) {
        try {
            this.consumer.acceptN(elements);
        } finally {
            elements.clear();
        }
    }

    private void doAccept(T element) {
        this.consumer.accept(element);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean isRun() {
        return isRun.get();
    }

    public void stop() {
        isRun.set(false);

        if (!isEmpty()) {
            logger.info("Wait 5 seconds. Flushing queued data.");
        }
        executeThread.interrupt();
        try {
            executeThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("{} stopped incompletely.", executorName);
        }

        logger.info("{} stopped.", executorName);
    }

    Collection<T> getDrainQueue() {
        return drain;
    }
}
