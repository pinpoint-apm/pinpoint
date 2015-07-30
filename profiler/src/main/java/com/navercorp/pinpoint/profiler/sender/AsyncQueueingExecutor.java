/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.util.PinpointThreadFactory;

/**
 * @author emeroad
 */
public class AsyncQueueingExecutor<T> implements Runnable {

    private static final AsyncQueueingExecutorListener EMPTY_LISTENER = new EmptyAsyncQueueingExecutorListener();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isWarn = logger.isWarnEnabled();

    private final LinkedBlockingQueue<T> queue;
    private final AtomicBoolean isRun = new AtomicBoolean(true);
    private final Thread executeThread;
    private final String executorName;

    private final int maxDrainSize;
    // Caution. single thread only. this Collection is simpler than ArrayList.
    private final Collection<T> drain;

    private AsyncQueueingExecutorListener<T> listener = EMPTY_LISTENER;


    public AsyncQueueingExecutor() {
        this(1024 * 5, "Pinpoint-AsyncQueueingExecutor");
    }

    public AsyncQueueingExecutor(int queueSize, String executorName) {
        if (executorName == null) {
            throw new NullPointerException("executorName must not be null");
        }
        // BEFORE executeThread start
        this.maxDrainSize = 10;
        this.drain = new UnsafeArrayCollection<T>(maxDrainSize);
        this.queue = new LinkedBlockingQueue<T>(queueSize);

        this.executeThread = this.createExecuteThread(executorName);
        this.executorName = executeThread.getName();
    }

    private Thread createExecuteThread(String executorName) {
        final ThreadFactory threadFactory = new PinpointThreadFactory(executorName, true);
        Thread thread = threadFactory.newThread(this);
        thread.start();
        return thread;
    }

    @Override
    public void run() {
        logger.info("{} started.", executorName);
        doExecute();
    }

    private void doExecute() {
        drainStartEntry:
        while (isRun()) {
            try {
                Collection<T> dtoList = getDrainQueue();
                int drainSize = takeN(dtoList, this.maxDrainSize);
                if (drainSize > 0) {
                    doExecute(dtoList);
                    continue;
                }

                while (isRun()) {
                    T dto = takeOne();
                    if (dto != null) {
                        doExecute(dto);
                        continue drainStartEntry;
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
        while(true) {
            Collection<T> dtoList = getDrainQueue();
           int drainSize = takeN(dtoList, this.maxDrainSize);
            if (drainSize == 0) {
                break;
            }
            if (debugEnabled) {
                logger.debug("flushData size {}", drainSize);
            }
            doExecute(dtoList);
        }
    }

    protected T takeOne() {
        try {
            return queue.poll(1000 * 2, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    protected int takeN(Collection<T> drain, int maxDrainSize) {
        return queue.drainTo(drain, maxDrainSize);
    }

    public boolean execute(T data) {
        if (data == null) {
            if (isWarn) {
                logger.warn("execute(). data is null");
            }
            return false;
        }
        if (!isRun.get()) {
            if (isWarn) {
                logger.warn("{} is shutdown. discard data:{}", executorName, data);
            }
            return false;
        }
        boolean offer = queue.offer(data);
        if (!offer) {
            if (isWarn) {
                logger.warn("{} Drop data. queue is full. size:{}", executorName, queue.size());
            }
        }
        return offer;
    }

    public void setListener(AsyncQueueingExecutorListener<T> listener) {
        if (listener == null) {
            throw new NullPointerException("listener must not be null");
        }
        this.listener = listener;
    }

    private void doExecute(Collection<T> dtoList) {
        this.listener.execute(dtoList);
    }

    private void doExecute(T dto) {
        this.listener.execute(dto);
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
        this.drain.clear();
        return drain;
    }
}
