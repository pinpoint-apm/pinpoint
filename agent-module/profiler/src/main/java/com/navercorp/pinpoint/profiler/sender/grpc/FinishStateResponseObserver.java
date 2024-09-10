package com.navercorp.pinpoint.profiler.sender.grpc;

import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class FinishStateResponseObserver<V> implements StreamObserver<V> {
    private final Logger logger;

    private final FinishState state = new FinishState();

    public FinishStateResponseObserver(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void onNext(V value) {
        logger.debug("onNext {}", value);
    }

    @Override
    public void onError(Throwable t) {
        logger.info("onError", t);
        this.state.error();
    }

    @Override
    public void onCompleted() {
        logger.debug("onCompleted");
        this.state.completed();
    }

    public FinishState getState() {
        return state;
    }
};
