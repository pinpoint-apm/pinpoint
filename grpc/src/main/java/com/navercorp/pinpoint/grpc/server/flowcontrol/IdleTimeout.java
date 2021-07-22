package com.navercorp.pinpoint.grpc.server.flowcontrol;

public interface IdleTimeout {
    void update();

    boolean isExpired();
}
