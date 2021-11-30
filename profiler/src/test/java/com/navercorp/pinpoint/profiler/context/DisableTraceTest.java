package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class DisableTraceTest {
    @Test
    public void testGetScope() {
        Trace trace = newTrace();
        Assert.assertNull(trace.addScope("empty"));
    }

    @Test
    public void testAddScope() {
        Trace trace = newTrace();

        trace.addScope("aaa");
        Assert.assertNotNull(trace.getScope("aaa"));
    }

    private Trace newTrace() {
        ActiveTraceHandle activeTraceHandle = mock(ActiveTraceHandle.class);
        return new DisableTrace(1, 2, activeTraceHandle);
    }
}