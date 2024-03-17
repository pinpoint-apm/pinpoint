package com.navercorp.pinpoint.profiler.context.grpc.mapper;


import com.navercorp.pinpoint.grpc.trace.PThreadState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author intr3p1d
 */
class ThreadDumpMapperTest {

    ThreadDumpMapper mapper = new ThreadDumpMapperImpl();

    @Test
    void testEnumToEnum() {
        for (Thread.State state : Thread.State.values()) {
            PThreadState pThreadState = mapper.map(state);
            assertEquals("THREAD_STATE_" + state.name(), pThreadState.name());
        }
    }

}