package com.navercorp.pinpoint.profiler.sender.grpc.stream;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class StreamExecutorFactory<ReqT> {

    private final ExecutorService executor;

    public StreamExecutorFactory(ExecutorService executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    public StreamExecutor<ReqT> newStreamExecutor() {
        return new StreamExecutor<ReqT>(executor);
    }

}
