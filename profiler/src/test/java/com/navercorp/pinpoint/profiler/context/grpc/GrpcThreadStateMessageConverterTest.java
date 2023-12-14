package com.navercorp.pinpoint.profiler.context.grpc;


import com.navercorp.pinpoint.grpc.trace.PThreadState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author intr3p1d
 */
class GrpcThreadStateMessageConverterTest {

    GrpcThreadStateMessageConverter converter = new GrpcThreadStateMessageConverter();

    @Test
    void testEnumToEnum() {
        for (Thread.State state : Thread.State.values()) {
            PThreadState pThreadState = converter.convertThreadState(state);
            assertEquals("THREAD_STATE_" + state.name(), pThreadState.name());
        }
    }

}