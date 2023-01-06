package com.navercorp.pinpoint.profiler.context.id;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.profiler.context.DisableAsyncChildTrace;
import com.navercorp.pinpoint.profiler.context.DisableTrace;
import com.navercorp.pinpoint.profiler.context.LocalAsyncId;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class LocalTraceTest {
    @Test
    public void testGetScope() {
        LocalTraceRoot traceRoot = TraceRoot.local("testAgent", 2, 1);
        Trace trace = newTrace(traceRoot);
        Trace childTrace = newChildTrace(traceRoot);
        Assertions.assertNull(trace.addScope("empty"));
        Assertions.assertNull(childTrace.addScope("empty"));
    }

    @Test
    public void testAddScope() {
        LocalTraceRoot traceRoot = TraceRoot.local("testAgent", 2, 1);
        Trace trace = newTrace(traceRoot);
        Trace childTrace = newChildTrace(traceRoot);

        trace.addScope("aaa");
        childTrace.addScope("bbb");
        Assertions.assertNotNull(trace.getScope("aaa"));
        Assertions.assertNotNull(childTrace.getScope("bbb"));
    }

    @Test
    public void testSampled() {
        LocalTraceRoot traceRoot = TraceRoot.local("testAgent", 2, 1);
        Trace trace = newTrace(traceRoot);
        Trace childTrace = newChildTrace(traceRoot);

        Assertions.assertFalse(trace.canSampled());
        Assertions.assertFalse(childTrace.canSampled());
    }

    @Test
    public void testSpanRecorder() {
        LocalTraceRoot traceRoot = TraceRoot.local("testAgent", 2, 1);
        Trace trace = newTrace(traceRoot);
        Trace childTrace = newChildTrace(traceRoot);
        SpanRecorder spanRecorder = trace.getSpanRecorder();
        SpanRecorder childSpanRecorder = childTrace.getSpanRecorder();

        Assertions.assertNotNull(spanRecorder);
        Assertions.assertNull(childSpanRecorder);
    }

    @Test
    public void testCurrentSpanEventRecorder() {
        LocalTraceRoot traceRoot = TraceRoot.local("testAgent", 2, 1);
        Trace trace = newTrace(traceRoot);
        Trace childTrace = newChildTrace(traceRoot);
        SpanEventRecorder spanEventRecorder = trace.currentSpanEventRecorder();
        SpanEventRecorder childSpanEventRecorder = childTrace.currentSpanEventRecorder();

        Assertions.assertNull(spanEventRecorder);
        Assertions.assertNull(childSpanEventRecorder);
    }

    private Trace newTrace(LocalTraceRoot traceRoot) {
        ActiveTraceHandle activeTraceHandle = mock(ActiveTraceHandle.class);
        UriStatStorage uriStatStorage = mock(UriStatStorage.class);
        SpanRecorder spanRecorder = mock(SpanRecorder.class);
        return new DisableTrace(traceRoot, spanRecorder, activeTraceHandle, uriStatStorage);
    }

    private Trace newChildTrace(LocalTraceRoot traceRoot) {
        LocalAsyncId localAsyncId = mock(LocalAsyncId.class);
        return new DisableAsyncChildTrace(traceRoot, localAsyncId);
    }
}