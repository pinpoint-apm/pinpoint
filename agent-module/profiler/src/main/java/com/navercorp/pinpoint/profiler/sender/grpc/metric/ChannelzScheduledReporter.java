package com.navercorp.pinpoint.profiler.sender.grpc.metric;

import java.io.Closeable;

public interface ChannelzScheduledReporter extends Closeable {
    void registerRootChannel(long id, ChannelzReporter reporter);

    @Override
    void close();
}
