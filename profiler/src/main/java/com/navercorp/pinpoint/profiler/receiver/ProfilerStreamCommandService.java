package com.navercorp.pinpoint.profiler.receiver;

import org.apache.thrift.TBase;

import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelContext;

public interface ProfilerStreamCommandService extends ProfilerCommandService {

    short streamCommandService(TBase tBase, ServerStreamChannelContext streamChannelContext);
    
}
