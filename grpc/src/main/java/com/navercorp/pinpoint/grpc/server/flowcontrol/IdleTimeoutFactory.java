package com.navercorp.pinpoint.grpc.server.flowcontrol;

public class IdleTimeoutFactory {
    private final long idleTimeout;

    public IdleTimeoutFactory(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public IdleTimeout newIdleTimeout() {
        if (this.idleTimeout == DisableIdleTimeout.DISABLE_TIME) {
            return new DisableIdleTimeout();
        } else {
            return new DefaultIdleTimeout(this.idleTimeout);
        }
    }
}
