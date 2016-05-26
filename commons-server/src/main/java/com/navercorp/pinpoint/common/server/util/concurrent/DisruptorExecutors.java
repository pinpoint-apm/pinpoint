package com.navercorp.pinpoint.common.server.util.concurrent;

import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;

import java.util.concurrent.ThreadFactory;

/**
 * @author Taejin Koo
 */
public class DisruptorExecutors {

    private DisruptorExecutors() {
    }

    public static int nextPowerOfTwo(int v) {
        return 1 << (32 - Integer.numberOfLeadingZeros(v - 1));
    }

    public static DisruptorExecutor newSingleProducerExecutor(int poolSize, int queueSize, ThreadFactory threadFactory) {
        return newSingleProducerExecutor(poolSize, queueSize, threadFactory, new LiteBlockingWaitStrategy());
    }

    public static DisruptorExecutor newSingleProducerExecutor(int poolSize, int queueSize, ThreadFactory threadFactory, WaitStrategy waitStrategy) {
        return new DisruptorExecutor(poolSize, queueSize, threadFactory, waitStrategy, false);
    }

    public static DisruptorExecutor newMultiProducerExecutor(int poolSize, int queueSize, ThreadFactory threadFactory) {
        return newMultiProducerExecutor(poolSize, queueSize, threadFactory, new LiteBlockingWaitStrategy());
    }

    public static DisruptorExecutor newMultiProducerExecutor(int poolSize, int queueSize, ThreadFactory threadFactory, WaitStrategy waitStrategy) {
        return new DisruptorExecutor(poolSize, queueSize, threadFactory, waitStrategy, true);
    }

}
