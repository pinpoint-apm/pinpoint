package com.navercorp.pinpoint.profiler.context.id;

import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class LoggingAsyncState implements AsyncState {
    private final Logger logger;

    private final AsyncState delegate;

    public LoggingAsyncState(AsyncState delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.logger = LogManager.getLogger(delegate.getClass());
    }


    @Override
    public void setup() {
        logger.debug("setup()");
        delegate.setup();
    }

    @Override
    public boolean await() {
        logger.debug("await()");
        return delegate.await();
    }

    @Override
    public void finish() {
        logger.debug("finish()");
        delegate.finish();
    }

    @Override
    public String toString() {
        return "LoggingAsyncState{" +
                "delegate=" + delegate +
                '}';
    }
}
