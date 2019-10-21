/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.web.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SimpleOrderedThreadPool implements Executor {

    private final int threadCount;
    private final int workerQueueSize;
    private final ThreadFactory threadFactory;

    private final ExecutorService[] childExecutors;

    public SimpleOrderedThreadPool(int threadCount, int workerQueueSize, ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException("threadFactory");
        }
        if (threadCount < 0) {
            throw new IllegalArgumentException("threadCount workerQueueSize");
        }
        if (workerQueueSize < 0) {
            throw new IllegalArgumentException("workerQueueSize workerQueueSize");
        }
        this.threadCount = threadCount;
        this.workerQueueSize = workerQueueSize;
        this.threadFactory = threadFactory;
        this.childExecutors = createChildExecutor(threadCount);
    }

    private ExecutorService[] createChildExecutor(int threadCount) {

        final ExecutorService[] childExecutor = new ExecutorService[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final ExecutorService child = createSingleThreadExecutor(this.workerQueueSize, threadFactory);
            childExecutor[i] = child;
        }
        return childExecutor;
    }

    private ExecutorService createSingleThreadExecutor(int workerQueueSize, ThreadFactory threadFactory) {

        final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(workerQueueSize);

        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, workQueue, threadFactory);
    }


    @Override
    public void execute(Runnable command) {
        final ExecutorService childExecutor = getChildExecutor(command);

        dispatchChild(childExecutor, command);
    }

    private void dispatchChild(ExecutorService childExecutor, Runnable command) {
        childExecutor.execute(command);
    }

    private ExecutorService getChildExecutor(Runnable command) {
        if (!(command instanceof HashSelector)) {
            throw new IllegalArgumentException("invalid HashSelector command");
        }
        final HashSelector selector = (HashSelector) command;
        final int mod = mod(selector);

        return this.childExecutors[mod];
    }

    private int mod(HashSelector selector) {
        final int id = selector.select();
        final int mod = id % threadCount;
        return Math.abs(mod);
    }

    public void shutdown() {
        for (ExecutorService executorService : childExecutors) {
            executorService.shutdown();
        }
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        boolean totalShutdown = true;
        for (ExecutorService executorService : childExecutors) {
            final boolean child = executorService.awaitTermination(timeout, unit);
            if (!child) {
                totalShutdown = false;
            }
            // TODO timeout calculation
            // remainTimeout = remainTimeout - childShutdownTime ~~~~~
        }
        return totalShutdown;
    }

    public interface HashSelector {
        int select();
    }
}
