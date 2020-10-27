package com.navercorp.pinpoint.profiler.sender.grpc.stream;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.sender.grpc.MessageDispatcher;
import com.navercorp.pinpoint.profiler.sender.grpc.StreamTask;
import com.navercorp.pinpoint.profiler.sender.grpc.StreamState;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class StreamExecutorFactory<ReqT> {

    private final ExecutorService executor;

    public StreamExecutorFactory(ExecutorService executor) {
        this.executor = Assert.requireNonNull(executor, "executor");
    }

    public StreamExecutor<ReqT> newStreamExecutor() {
        return new StreamExecutor<ReqT>(executor);
    }

}
