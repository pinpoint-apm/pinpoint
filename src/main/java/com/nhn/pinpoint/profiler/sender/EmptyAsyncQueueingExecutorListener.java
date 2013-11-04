package com.nhn.pinpoint.profiler.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author emeroad
 */
public class EmptyAsyncQueueingExecutorListener implements AsyncQueueingExecutorListener<Object> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(Collection<Object> dtoList) {
        logger.info("execute()");

    }

    @Override
    public void execute(Object dto) {
        logger.info("execute()");
    }
}
