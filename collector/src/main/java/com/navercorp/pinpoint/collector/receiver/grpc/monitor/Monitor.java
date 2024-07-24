package com.navercorp.pinpoint.collector.receiver.grpc.monitor;

import java.io.Closeable;

public interface Monitor extends Closeable {

    void register(Runnable job);

    void close();
}
