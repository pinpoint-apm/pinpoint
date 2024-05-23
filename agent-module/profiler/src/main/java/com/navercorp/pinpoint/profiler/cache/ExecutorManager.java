package com.navercorp.pinpoint.profiler.cache;

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.profiler.concurrent.ExecutorFactory;
import com.navercorp.pinpoint.common.util.CpuUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorManager {

    private volatile ExecutorService executor;

    @VisibleForTesting
    static ExecutorService cacheExecutor() {
        // TODO Cleanup the executor when the agent is shutdown
        final int nThreads = cpuCount(CpuUtils.cpuCount());
        // Must set the same property as ForkJoinPool.commonPool()
        return ExecutorFactory.newFixedThreadPool(nThreads, Integer.MAX_VALUE, "Caffeine", true);
    }

    @VisibleForTesting
    static int cpuCount(int cpuCount) {
        cpuCount = cpuCount - 1;
        if (cpuCount <= 0) {
            return 1;
        }
        return Math.min(cpuCount, CaffeineBuilder.MAX_CPU);
    }

    @VisibleForTesting
    public Executor executor() {
        if (executor != null) {
            return executor;
        }
        synchronized (this) {
            if (executor != null) {
                return executor;
            }
            executor = cacheExecutor();
            return executor;
        }
    }

    @VisibleForTesting
    void shutdown() {
        if (executor == null) {
            return;
        }
        synchronized (this) {
            if (executor == null) {
                return;
            }
            executor.shutdown();
            try {
                executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignore) {
            }
            executor = null;
        }
    }
}
