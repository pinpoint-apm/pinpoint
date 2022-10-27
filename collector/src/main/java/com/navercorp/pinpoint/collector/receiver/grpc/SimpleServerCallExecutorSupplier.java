package com.navercorp.pinpoint.collector.receiver.grpc;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallExecutorSupplier;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author smilu97
 */
public class SimpleServerCallExecutorSupplier implements ServerCallExecutorSupplier  {
    private final Executor executor;

    public SimpleServerCallExecutorSupplier(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Nullable
    @Override
    public <ReqT, RespT> Executor getExecutor(ServerCall<ReqT, RespT> call, Metadata metadata) {
        return executor;
    }
}
