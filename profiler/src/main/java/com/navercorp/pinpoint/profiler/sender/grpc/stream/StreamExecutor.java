package com.navercorp.pinpoint.profiler.sender.grpc.stream;

import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public class StreamExecutor<ReqT> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExecutorService executor;

    public StreamExecutor(ExecutorService executor) {
        this.executor = Assert.requireNonNull(executor, "executor");
    }


    public Future<?> execute(final Runnable runnable) {
        logger.info("stream execute {}", runnable);
        try {
            return executor.submit(runnable);
        } catch (RejectedExecutionException reject) {
            logger.error("stream job rejected {}", runnable, reject);
            return null;
        }
    }

    @Override
    public String toString() {
        return "StreamExecutor{}";
    }
}
