package com.nhn.pinpoint.profiler.receiver;

import org.apache.thrift.TBase;

import com.nhn.pinpoint.rpc.stream.ServerStreamChannelContext;

public interface ProfilerStreamCommandService extends ProfilerCommandService {

    short streamCommandService(TBase tBase, ServerStreamChannelContext streamChannelContext);
    
}
