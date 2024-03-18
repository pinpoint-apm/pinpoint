package com.navercorp.pinpoint.profiler.sender.grpc.metric;

public interface ChannelzScheduledReporter {
    void registerRootChannel(long id, ChannelzReporter reporter);

    void stop();
}
