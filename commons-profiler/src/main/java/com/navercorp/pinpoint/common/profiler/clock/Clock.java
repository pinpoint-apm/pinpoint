package com.navercorp.pinpoint.common.profiler.clock;

/**
 * millisecond precision clock
 * @author Woonduk Kang(emeroad)
 */
public interface Clock {

    long millis();

    static Clock systemUTC() {
        return SystemClock.UTC;
    }

    static Clock fixed(long timestamp) {
        return new FixedClock(timestamp);
    }

    static Clock tick(long tick) {
        return new TickClock(Clock.systemUTC(), tick);
    }

    static Clock tick(Clock clock, long tick) {
        return new TickClock(clock, tick);
    }

    class SystemClock implements Clock {
        static final SystemClock UTC = new SystemClock();

        @Override
        public long millis() {
            return System.currentTimeMillis();
        }
    }

    class FixedClock implements Clock {
        private final long timestamp;

        public FixedClock(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public long millis() {
            return timestamp;
        }
    }

}
