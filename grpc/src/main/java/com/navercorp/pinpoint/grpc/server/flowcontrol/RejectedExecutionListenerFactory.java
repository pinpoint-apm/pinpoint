package com.navercorp.pinpoint.grpc.server.flowcontrol;

import java.util.Objects;

public class RejectedExecutionListenerFactory {
    private static final int REQUEST_IMMEDIATELY = -1;

    private final String name;
    private final long recoveryMessagesCount;
    private final IdleTimeoutFactory idleTimeoutFactory;

    public RejectedExecutionListenerFactory(String name, long recoveryMessagesCount, IdleTimeoutFactory idleTimeoutFactory) {
        this.name = Objects.requireNonNull(name, "name");
        this.recoveryMessagesCount = recoveryMessagesCount;
        this.idleTimeoutFactory = Objects.requireNonNull(idleTimeoutFactory, "idleTimeoutFactory");
    }

    public RejectedExecutionListener newListener(ServerCallWrapper serverCall) {
        IdleTimeout idleTimeout = idleTimeoutFactory.newIdleTimeout();

        if (recoveryMessagesCount == REQUEST_IMMEDIATELY) {
            return new SimpleRejectedExecutionListener(this.name, serverCall, idleTimeout);
        } else {
            return new FlowControlRejectExecutionListener(this.name, serverCall, recoveryMessagesCount, idleTimeout);
        }
    }

}
