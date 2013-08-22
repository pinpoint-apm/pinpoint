package com.nhn.pinpoint.profiler.sender;

import java.util.Collection;

/**
 *
 */
public interface AsyncQueueingExecutorListener {

    void execute(Collection<Object> dtoList);

    void execute(Object dto);
}
