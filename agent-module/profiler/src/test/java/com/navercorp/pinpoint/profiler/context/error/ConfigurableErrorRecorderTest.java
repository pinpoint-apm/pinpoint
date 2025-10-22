package com.navercorp.pinpoint.profiler.context.error;

import com.navercorp.pinpoint.common.trace.ErrorCategory;
import com.navercorp.pinpoint.profiler.context.id.DefaultShared;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ConfigurableErrorRecorderTest {
    private ConfigurableErrorRecorder sut;

    Shared shared;
    LocalTraceRoot localTraceRoot;

    @BeforeEach
    public void setUp() {
        shared = new DefaultShared();

        localTraceRoot = Mockito.mock(LocalTraceRoot.class);
        when(localTraceRoot.getShared()).thenReturn(shared);
    }

    @Test
    void none() {
        sut = new ConfigurableErrorRecorder(localTraceRoot, EnumSet.noneOf(ErrorCategory.class));

        for (ErrorCategory cat : ErrorCategory.values()) {
            sut.recordError(cat);
        }

        assertEquals(0, shared.getErrorCode());
    }

    @Test
    void maskErrorCode() {
        sut = new ConfigurableErrorRecorder(localTraceRoot, EnumSet.allOf(ErrorCategory.class));

        sut.recordError(ErrorCategory.UNKNOWN);

        assertNotEquals(0, shared.getErrorCode());
    }
}