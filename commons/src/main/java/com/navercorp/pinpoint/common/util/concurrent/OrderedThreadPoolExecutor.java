/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.common.util.concurrent;

import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * We have referred OrderedThreadPoolExecutor OrderedThreadPoolExecutor of Mina.
 *
 * @Author Taejin Koo
 */
public class OrderedThreadPoolExecutor<K> implements ExecutorService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object EXIT_SIGNAL = new Object();

    private final Class<K> orderedKeyClazzType;

    private final int corePoolSize;
    private final int maximumPoolSize;
    private final long keepAliveTime;
    private final ThreadFactory threadFactory;

    private final BlockingQueue workGroupKeyQueue = new LinkedBlockingQueue();
    private final OrderedThreadPoolTaskHolder<K> taskHolder;
    private final Set<Worker> workers;
    private volatile int largestPoolSize = 0;
    private volatile boolean shutdown;

    private long completedTaskCount;

    public OrderedThreadPoolExecutor(Class<K> orderedKeyClazzType, int corePoolSize, int maximumPoolSize) {
        this(orderedKeyClazzType, corePoolSize, maximumPoolSize, 60, TimeUnit.SECONDS);
    }

    public OrderedThreadPoolExecutor(Class<K> orderedKeyClazzType, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        this(orderedKeyClazzType, corePoolSize, maximumPoolSize, keepAliveTime, unit, PinpointThreadFactory.createThreadFactory(OrderedThreadPoolExecutor.class.getSimpleName()));
    }

    public OrderedThreadPoolExecutor(Class<K> orderedKeyClazzType, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
        if (orderedKeyClazzType == null) {
            throw new IllegalArgumentException("orderedKeyClazzType may not be null.");
        }

        if (corePoolSize < 0) {
            throw new IllegalArgumentException("corePoolSize: " + corePoolSize);
        }

        if (maximumPoolSize == 0 || maximumPoolSize < corePoolSize) {
            throw new IllegalArgumentException("maximumPoolSize: " + maximumPoolSize);
        }

        if (keepAliveTime <= 0) {
            throw new IllegalArgumentException("keepAliveTime: " + keepAliveTime);
        }

        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = unit.toMillis(keepAliveTime);

        this.orderedKeyClazzType = orderedKeyClazzType;
        this.workers = new HashSet<Worker>(corePoolSize);
        this.taskHolder = new OrderedThreadPoolTaskHolder<K>(corePoolSize);
        this.threadFactory = threadFactory;

        initCoreWorkers();
    }

    private void initCoreWorkers() {
        synchronized (workers) {
            while (workers.size() < getCorePoolSize()) {
                addWorker();
            }
        }
    }

    private void addWorkerIfNecessary() {
        synchronized (workers) {
            if (workers.isEmpty() || !hasAvailableWorker()) {
                addWorker();
            }
        }
    }

    private void addWorker() {
        synchronized (workers) {
            if (workers.size() >= getMaximumPoolSize()) {
                return;
            }

            Worker worker = new Worker();
            Thread thread = threadFactory.newThread(worker);
            thread.start();
            workers.add(worker);
        }
    }

    private boolean hasAvailableWorker() {
        synchronized (workers) {
            for (Worker worker : workers) {
                if (worker.isAvailable()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void shutdown() {
        if (shutdown) {
            return;
        }
        shutdown = true;

        synchronized (workers) {
            for (Worker worker : workers) {
                worker.shutdown();
            }

            for (int i = workers.size(); i > 0; i--) {
                workGroupKeyQueue.add(EXIT_SIGNAL);
            }
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return taskHolder.clear();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);

        synchronized (workers) {
            while (!isTerminated()) {
                long waitTime = deadline - System.currentTimeMillis();
                if (waitTime <= 0) {
                    break;
                }

                workers.wait(waitTime);
            }
        }
        return isTerminated();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        if (!shutdown) {
            return false;
        }

        synchronized (workers) {
            return workers.isEmpty();
        }
    }

    public long getCompletedTaskCount() {
        synchronized (workers) {
            long answer = completedTaskCount;
            for (Worker w : workers) {
                answer += w.getCompletedTaskCount();
            }

            return answer;
        }
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    @Override
    public Future<?> submit(Runnable task) {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    @Override
    public void execute(Runnable command) {
        if (shutdown) {
            throw new RejectedExecutionException("Task " + command.toString() + " rejected.");
        }

        assertRunnableType(command);
        K orderKey = ((OrderedThreadPoolRunnable<K>) command).getOrderKey();

        boolean needsPutKey = taskHolder.putTaskAndReturnKeyIsFirst(orderKey, command);
        if (needsPutKey) {
            workGroupKeyQueue.add(orderKey);
            addWorkerIfNecessary();
        }
    }

    private void assertRunnableType(Runnable command) {
        if (!(command instanceof OrderedThreadPoolRunnable)) {
            throw new IllegalArgumentException("command must be an OrderedThreadPoolRunnable or its subclass.");
        }

        Object orderKey = ((OrderedThreadPoolRunnable) command).getOrderKey();
        if (!orderedKeyClazzType.isInstance(orderKey)) {
            throw new IllegalArgumentException("orderedKey must be an " + orderedKeyClazzType + " or its subclass.");
        }
    }

    private class Worker implements Runnable {
        private final AtomicLong completedTaskCount = new AtomicLong();

        private Thread thread;
        private volatile boolean available = false;
        private volatile boolean shutdown = false;

        @Override
        public void run() {
            thread = Thread.currentThread();

            try {
                while (!shutdown) {
                    Object groupKey = getGroupKey();
                    available = false;

                    if (groupKey == EXIT_SIGNAL) {
                        break;
                    }

                    if (groupKey == null || !orderedKeyClazzType.isInstance(groupKey)) {
                        synchronized (workers) {
                            if (workers.size() > getCorePoolSize()) {
                                workers.remove(this);
                                break;
                            }
                        }
                    }

                    runTasks((K) groupKey);
                    available = true;
                }
            } finally {
                synchronized (workers) {
                    available = false;
                    workers.remove(this);
                    OrderedThreadPoolExecutor.this.completedTaskCount += completedTaskCount.get();
                    workers.notifyAll();
                }
            }
        }

        boolean isAvailable() {
            return available;
        }

        void shutdown() {
            this.shutdown = true;
        }

        long getCompletedTaskCount() {
            return completedTaskCount.get();
        }

        private Object getGroupKey() {
            long startTime = System.currentTimeMillis();
            long deadline = startTime + keepAliveTime;
            for (;;) {
                try {
                    long currentTime = System.currentTimeMillis();
                    long waitTime = deadline - currentTime;
                    if (waitTime <= 0) {
                        break;
                    }

                    Object key = workGroupKeyQueue.poll(waitTime, TimeUnit.MILLISECONDS);
                    if (key != null) {
                        return key;
                    }
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
            return null;
        }

        private void runTasks(K groupKey) {
            if (groupKey == null) {
                return;
            }

            try {
                while (!taskHolder.removeIfEmpty(groupKey)) {
                    Runnable task = taskHolder.getTask(groupKey);
                    if (task != null) {
                        runTask(task);
                    }
                }
            } catch (RuntimeException e) {
                boolean removed = taskHolder.removeIfEmpty(groupKey);
                if (!removed) {
                    workGroupKeyQueue.add(groupKey);
                }
            }
        }

        private void runTask(Runnable runnable) {
            runnable.run();
            completedTaskCount.incrementAndGet();
        }
    }

}
