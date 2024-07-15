package com.navercorp.pinpoint.profiler.sender.grpc.metric;

import com.navercorp.pinpoint.common.profiler.Stoppable;

public interface ChannelzScheduledReporter extends Stoppable {
    void registerRootChannel(long id, ChannelzReporter reporter);

    @Override
    void stop();
}
