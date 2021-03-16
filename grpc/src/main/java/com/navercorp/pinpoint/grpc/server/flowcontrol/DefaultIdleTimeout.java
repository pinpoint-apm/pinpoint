package com.navercorp.pinpoint.grpc.server.flowcontrol;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.Clock;
import com.navercorp.pinpoint.common.util.SystemClock;

import java.util.Objects;

public class DefaultIdleTimeout implements IdleTimeout {

    private final long idleTimeout;

    private volatile long lastExecutionTime = Long.MAX_VALUE;
    private volatile boolean expired = false;

    private final Clock clock;

    public DefaultIdleTimeout(long idleTimeout) {
        this(idleTimeout, SystemClock.INSTANCE);
    }

    public DefaultIdleTimeout(long idleTimeout, Clock clock) {
        Assert.isTrue(idleTimeout >= 0, "negative idleTimeout");
        this.idleTimeout = idleTimeout;

        this.clock = Objects.requireNonNull(clock, "clock");
        update();
    }

    @Override
    public void update() {
        this.lastExecutionTime = clock.getTime();
    }

    @Override
    public boolean isExpired() {
        if (this.expired) {
            return true;
        }

        final long elapsedTime = this.clock.getTime() - lastExecutionTime;
        final boolean result = elapsedTime >= idleTimeout;
        if (result) {
            this.expired = true;
        }
        return result;
    }

}
