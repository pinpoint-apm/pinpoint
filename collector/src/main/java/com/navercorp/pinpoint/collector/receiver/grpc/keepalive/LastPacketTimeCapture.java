package com.navercorp.pinpoint.collector.receiver.grpc.keepalive;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class LastPacketTimeCapture {
    private static AtomicLongFieldUpdater<LastPacketTimeCapture> UPDATER = AtomicLongFieldUpdater.newUpdater(LastPacketTimeCapture.class, "lastTime");

    private volatile long lastTime = currentTimeMillis();

    public long last() {
        return UPDATER.get(this);
    }

    public void update() {
        UPDATER.set(this, currentTimeMillis());
    }

    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
