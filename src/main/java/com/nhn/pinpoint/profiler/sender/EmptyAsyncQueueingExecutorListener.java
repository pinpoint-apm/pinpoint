package com.nhn.pinpoint.profiler.sender;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class EmptyAsyncQueueingExecutorListener<T> implements AsyncQueueingExecutorListener<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void execute(Collection<T> dtoList) {
        if (isDebug) {
            logger.debug("execute()");
        }
    }

    @Override
    public void execute(T dto) {
        if (isDebug) {
            logger.debug("execute()");
        }
    }
}
