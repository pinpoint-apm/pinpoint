package com.nhn.pinpoint.profiler.sender;

import java.util.Collection;

import com.nhn.pinpoint.profiler.sender.message.PinpointMessage;

/**
 * @author emeroad
 */
public interface AsyncQueueingExecutorListener<T extends PinpointMessage> {

    void execute(Collection<T> messageList);

    void execute(T message);
    
}
