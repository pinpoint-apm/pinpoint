package com.nhn.pinpoint.profiler.context;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class ActiveThreadCounter {
    private AtomicInteger counter = new AtomicInteger(0);

    public void start() {
        counter.incrementAndGet();
    }

    public void end() {
        counter.decrementAndGet();
    }

    public int getActiveThread() {
        return counter.get();
    }

    public void reset() {
        counter.set(0);
    }
}
