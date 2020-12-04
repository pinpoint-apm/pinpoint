package com.navercorp.pinpoint.profiler.sender.grpc.metric;

public interface ChannelzReporter {
    void reportRootChannel(long id);
}
