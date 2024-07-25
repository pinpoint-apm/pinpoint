package com.navercorp.pinpoint.collector.receiver.grpc.monitor;

import java.io.Closeable;

public interface Monitor extends Closeable {
    Monitor NONE = new EmptyMonitor();

    void register(Runnable job);

    void close();
}
