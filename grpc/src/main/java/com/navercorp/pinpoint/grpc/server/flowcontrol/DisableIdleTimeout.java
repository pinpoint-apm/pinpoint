package com.navercorp.pinpoint.grpc.server.flowcontrol;

public class DisableIdleTimeout implements IdleTimeout {
    public static final long DISABLE_TIME = -1;

    @Override
    public void update() {

    }

    @Override
    public boolean isExpired() {
        return false;
    }
}
