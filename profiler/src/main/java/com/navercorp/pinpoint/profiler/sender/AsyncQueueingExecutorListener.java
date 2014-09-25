package com.nhn.pinpoint.profiler.sender;

import java.util.Collection;

/**
 * @author emeroad
 */
public interface AsyncQueueingExecutorListener<T> {

    void execute(Collection<T> messageList);

    void execute(T message);
    
}
