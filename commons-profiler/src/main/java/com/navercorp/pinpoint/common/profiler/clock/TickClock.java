package com.navercorp.pinpoint.common.profiler.clock;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TickClock implements Clock {
    private final Clock baseClock;
    private final long tick;

    public TickClock(Clock baseClock, long tick) {
        this.baseClock = Objects.requireNonNull(baseClock, "baseClock");
        if (tick < 0) {
            throw new IllegalArgumentException("negative tick");
        }
        this.tick = tick;
    }

    public long millis() {
        long millis = baseClock.millis();
        return tick(millis);
    }

    public long tick(long millis) {
        return millis - (millis % tick);
    }

}

