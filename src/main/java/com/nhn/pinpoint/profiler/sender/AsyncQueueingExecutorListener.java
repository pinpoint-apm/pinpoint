package com.nhn.pinpoint.profiler.sender;

import java.util.Collection;

/**
 *
 */
public interface AsyncQueueingExecutorListener<T> {

    void execute(Collection<T> dtoList);

    void execute(T dto);
}
