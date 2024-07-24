package com.navercorp.pinpoint.collector.receiver.grpc.monitor;

public class EmptyMonitor implements Monitor {

    public EmptyMonitor() {
    }

    public void register(Runnable job) {
    }

    @Override
    public void close() {
    }
}