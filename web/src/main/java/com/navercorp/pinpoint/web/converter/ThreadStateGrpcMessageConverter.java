package com.navercorp.pinpoint.web.converter;

import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.trace.PThreadState;

public class ThreadStateGrpcMessageConverter implements MessageConverter<Object, PThreadState> {

    @Override
    public PThreadState toMessage(Object message) {
        if (message instanceof Thread.State) {
            return toMessage((Thread.State) message);
        } else {
            throw new IllegalArgumentException("invalid message type. message=" + message);
        }
    }

    private PThreadState toMessage(Thread.State threadState) {
        switch (threadState) {
            case NEW:
                return PThreadState.THREAD_STATE_NEW;
            case RUNNABLE:
                return PThreadState.THREAD_STATE_RUNNABLE;
            case BLOCKED:
                return PThreadState.THREAD_STATE_BLOCKED;
            case WAITING:
                return PThreadState.THREAD_STATE_WAITING;
            case TIMED_WAITING:
                return PThreadState.THREAD_STATE_TIMED_WAITING;
            case TERMINATED:
                return PThreadState.THREAD_STATE_TERMINATED;
        }
        return PThreadState.THREAD_STATE_UNKNOWN;
    }

}
