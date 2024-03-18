package com.navercorp.pinpoint.profiler.sender.grpc.metric;

public class EmptyChannelzScheduledReporter implements ChannelzScheduledReporter{
    @Override
    public void registerRootChannel(long id, ChannelzReporter reporter) {
        
    }

    @Override
    public void stop() {

    }
}
