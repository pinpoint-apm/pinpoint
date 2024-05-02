package com.navercorp.pinpoint.common.profiler.logging;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

public class LogSampler {
    private final long ratio;
    private final AtomicLong counter = new AtomicLong();

    public LogSampler(long ratio) {
        this.ratio = ratio;
    }

    private boolean isSampled(long counter) {
        if (counter % ratio == 0) {
            return true;
        }
        return false;
    }

    public void log(LongConsumer log) {
        final long counter = this.counter.getAndIncrement();
        if (isSampled(counter)) {
            log.accept(counter);
        }
    }

    public void log(Runnable log) {
        final long counter = this.counter.getAndIncrement();
        if (isSampled(counter)) {
            log.run();
        }
    }
}
