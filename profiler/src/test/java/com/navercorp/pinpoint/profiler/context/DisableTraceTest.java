package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class DisableTraceTest {
    @Test
    public void testGetScope() {
        Trace trace = newTrace();
        Trace childTrace = newChildTrace();
        Assertions.assertNull(trace.addScope("empty"));
        Assertions.assertNull(childTrace.addScope("empty"));
    }

    @Test
    public void testAddScope() {
        Trace trace = newTrace();
        Trace childTrace = newChildTrace();

        trace.addScope("aaa");
        childTrace.addScope("bbb");
        Assertions.assertNotNull(trace.getScope("aaa"));
        Assertions.assertNotNull(childTrace.getScope("bbb"));
    }

    @Test
    public void testSampled() {
        Trace trace = newTrace();
        Trace childTrace = newChildTrace();

        Assertions.assertFalse(trace.canSampled());
        Assertions.assertFalse(childTrace.canSampled());
    }

    @Test
    public void testSpanRecorder() {
        Trace trace = newTrace();
        Trace childTrace = newChildTrace();
        SpanRecorder spanRecorder = trace.getSpanRecorder();
        SpanRecorder childSpanRecorder = childTrace.getSpanRecorder();

        Assertions.assertNull(spanRecorder);
        Assertions.assertNull(childSpanRecorder);
    }

    @Test
    public void testCurrentSpanEventRecorder() {
        Trace trace = newTrace();
        Trace childTrace = newChildTrace();
        SpanEventRecorder spanEventRecorder = trace.currentSpanEventRecorder();
        SpanEventRecorder childSpanEventRecorder = childTrace.currentSpanEventRecorder();

        Assertions.assertNull(spanEventRecorder);
        Assertions.assertNull(childSpanEventRecorder);
    }

    private Trace newTrace() {
        ActiveTraceHandle activeTraceHandle = mock(ActiveTraceHandle.class);
        return new DisableTrace(1, 2, activeTraceHandle);
    }

    private Trace newChildTrace() {
        TraceRoot traceRoot = mock(TraceRoot.class);
        LocalAsyncId localAsyncId = mock(LocalAsyncId.class);

        return new DisableAsyncChildTrace(traceRoot, localAsyncId);
    }
}