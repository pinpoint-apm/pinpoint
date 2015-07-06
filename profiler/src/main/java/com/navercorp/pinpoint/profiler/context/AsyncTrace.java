package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ParsingResult;

public class AsyncTrace implements Trace {
    private static final int BEGIN_STACKID = 1;
    
    private final Trace trace;
    private int asyncId;
    private short asyncSequence;

    public AsyncTrace(final Trace trace, final int asyncId, final short asyncSequence) {
        this.trace = trace;
        this.asyncId = asyncId;
        this.asyncSequence = asyncSequence;
        traceBlockBegin(BEGIN_STACKID);
    }

    public int getAsyncId() {
        return asyncId;
    }

    @Override
    public TraceId getTraceId() {
        return trace.getTraceId();
    }

    @Override
    public boolean canSampled() {
        return trace.canSampled();
    }

    @Override
    public boolean isRoot() {
        return trace.isRoot();
    }

    @Override
    public int getStackFrameId() {
        return trace.getStackFrameId();
    }

    @Override
    public SpanEventRecorder traceBlockBegin() {
        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordAsyncId(asyncId);
        recorder.recordAsyncSequence(asyncSequence);
        
        return recorder;
    }

    @Override
    public SpanEventRecorder traceBlockBegin(int stackId) {
        final SpanEventRecorder recorder = trace.traceBlockBegin(stackId);
        recorder.recordAsyncId(asyncId);
        recorder.recordAsyncSequence(asyncSequence);
        
        return recorder;
    }

    @Override
    public void traceBlockEnd() {
        trace.traceBlockEnd();
    }

    @Override
    public void traceBlockEnd(int stackId) {
        trace.traceBlockEnd(stackId);
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public long getTraceStartTime() {
        return trace.getTraceStartTime();
    }

    @Override
    public boolean isRootStack() {
        return getStackFrameId() == BEGIN_STACKID;
    }

    @Override
    public AsyncTraceId getAsyncTraceId() {
        return trace.getAsyncTraceId();
    }

    @Override
    public void close() {
        traceBlockEnd(BEGIN_STACKID);
        trace.close();
    }

    @Override
    public SpanRecorder getSpanRecorder() {
        return trace.getSpanRecorder();
    }

    @Override
    public SpanEventRecorder getSpanEventRecorder() {
        return trace.getSpanEventRecorder();
    }
}