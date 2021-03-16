package com.navercorp.pinpoint.grpc.server.flowcontrol;

public class RejectedExecutionListenerFactory {
    private static final int REQUEST_IMMEDIATELY = -1;

    private final long recoveryMessagesCount;
    private final long idleTimeout;

    public RejectedExecutionListenerFactory(long recoveryMessagesCount, long idleTimeout) {
        this.recoveryMessagesCount = recoveryMessagesCount;
        this.idleTimeout = idleTimeout;
    }

    public RejectedExecutionListener newListener(ServerCallWrapper serverCall) {
        IdleTimeout idleTimeout = newIdleTimeout();

        if (recoveryMessagesCount == REQUEST_IMMEDIATELY) {
            return new SimpleRejectedExecutionListener(serverCall, idleTimeout);
        } else {
            return new FlowControlRejectExecutionListener(serverCall, recoveryMessagesCount, idleTimeout);
        }
    }

    private IdleTimeout newIdleTimeout() {
        if (this.idleTimeout == DisableIdleTimeout.DISABLE_TIME) {
            return new DisableIdleTimeout();
        } else {
            return new DefaultIdleTimeout(this.idleTimeout);
        }
    }
}
