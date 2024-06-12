package com.navercorp.pinpoint.profiler.context.id;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.profiler.context.CloseListener;
import com.navercorp.pinpoint.profiler.context.DisableChildTrace;
import com.navercorp.pinpoint.profiler.context.DisableTrace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class LocalTraceRootTest {

    private static final AgentId AGENT_ID = AgentId.of("testAgent");

    @Test
    public void testGetScope() {
        LocalTraceRoot traceRoot = TraceRoot.local(AGENT_ID, 2, 1);
        Trace trace = newTrace(traceRoot);
        Trace childTrace = newChildTrace(traceRoot);
        Assertions.assertNull(trace.addScope("empty"));
        Assertions.assertNull(childTrace.addScope("empty"));
    }

    @Test
    public void testAddScope() {
        LocalTraceRoot traceRoot = TraceRoot.local(AGENT_ID, 2, 1);
        Trace trace = newTrace(traceRoot);
        Trace childTrace = newChildTrace(traceRoot);

        trace.addScope("aaa");
        childTrace.addScope("bbb");
        Assertions.assertNotNull(trace.getScope("aaa"));
        Assertions.assertNotNull(childTrace.getScope("bbb"));
    }

    @Test
    public void testSampled() {
        LocalTraceRoot traceRoot = TraceRoot.local(AGENT_ID, 2, 1);
        Trace trace = newTrace(traceRoot);
        Trace childTrace = newChildTrace(traceRoot);

        Assertions.assertFalse(trace.canSampled());
        Assertions.assertFalse(childTrace.canSampled());
    }

    @Test
    public void testSpanRecorder() {
        LocalTraceRoot traceRoot = TraceRoot.local(AGENT_ID, 2, 1);
        Trace trace = newTrace(traceRoot);
        Trace childTrace = newChildTrace(traceRoot);
        SpanRecorder spanRecorder = trace.getSpanRecorder();
        SpanRecorder childSpanRecorder = childTrace.getSpanRecorder();

        Assertions.assertNotNull(spanRecorder);
        Assertions.assertNotNull(childSpanRecorder);
    }

    @Test
    public void testCurrentSpanEventRecorder() {
        LocalTraceRoot traceRoot = TraceRoot.local(AGENT_ID, 2, 1);
        Trace trace = newTrace(traceRoot);
        Trace childTrace = newChildTrace(traceRoot);
        SpanEventRecorder spanEventRecorder = trace.currentSpanEventRecorder();
        SpanEventRecorder childSpanEventRecorder = childTrace.currentSpanEventRecorder();

        Assertions.assertNotNull(spanEventRecorder);
        Assertions.assertNotNull(childSpanEventRecorder);
    }

    private Trace newTrace(LocalTraceRoot traceRoot) {
        SpanRecorder spanRecorder = mock(SpanRecorder.class);
        SpanEventRecorder spanEventRecorder = mock(SpanEventRecorder.class);

        return new DisableTrace(traceRoot, spanRecorder, spanEventRecorder, CloseListener.EMPTY);
    }

    private Trace newChildTrace(LocalTraceRoot traceRoot) {
        SpanRecorder spanRecorder = mock(SpanRecorder.class);
        SpanEventRecorder spanEventRecorder = mock(SpanEventRecorder.class);
        return new DisableChildTrace(traceRoot, spanRecorder, spanEventRecorder);
    }
}