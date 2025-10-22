package com.navercorp.pinpoint.profiler.context.error;

import com.navercorp.pinpoint.common.trace.ErrorCategory;
import com.navercorp.pinpoint.profiler.context.id.DefaultShared;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;

class SimpleErrorRecorderTest {
    private SimpleErrorRecorder sut;

    Shared shared;

    @BeforeEach
    void setUp() {
        shared = new DefaultShared();

        LocalTraceRoot localTraceRoot = Mockito.mock(LocalTraceRoot.class);
        when(localTraceRoot.getShared()).thenReturn(shared);

        sut = new SimpleErrorRecorder(localTraceRoot);
    }

    @Test
    void maskErrorCode() {
        assertEquals(0, shared.getErrorCode());
        sut.recordError(ErrorCategory.UNKNOWN);
        assertNotEquals(0, shared.getErrorCode());
    }
}