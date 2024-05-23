package com.navercorp.pinpoint.profiler.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadPoolExecutor;

class ExecutorManagerTest {
    @Test
    void cpuCount() {
        Assertions.assertEquals(1, ExecutorManager.cpuCount(0));

        Assertions.assertEquals(1, ExecutorManager.cpuCount(1));
        Assertions.assertEquals(1, ExecutorManager.cpuCount(2));
        Assertions.assertEquals(2, ExecutorManager.cpuCount(3));
        Assertions.assertEquals(3, ExecutorManager.cpuCount(4));

        Assertions.assertEquals(4, ExecutorManager.cpuCount(5));
        Assertions.assertEquals(4, ExecutorManager.cpuCount(16));
    }


    @Test
    void cacheExecutor() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) ExecutorManager.cacheExecutor();
        executor.prestartAllCoreThreads();

        executor.shutdown();
    }

}