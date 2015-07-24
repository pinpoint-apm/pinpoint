package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceType;

public class AsyncTrace implements Trace {
    private static final int BEGIN_STACKID = 1;
    
    private final Trace trace;
    private int asyncId;
    private short asyncSequence;

    public AsyncTrace(final Trace trace, final int asyncId, final short asyncSequence, final long startTime) {
        this.trace = trace;
        this.trace.getSpanRecorder().recordStartTime(startTime);
        this.asyncId = asyncId;
        this.asyncSequence = asyncSequence;
        traceBlockBegin(BEGIN_STACKID);
    }

    public int getAsyncId() {
        return asyncId;
    }

    @Override
    public long getId() {
        return -1;
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    @Override
    public Thread getBindThread() {
        return null;
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
    public boolean isRootStack() {
        return trace.getCallStackFrameId() == BEGIN_STACKID;
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
    public SpanEventRecorder currentSpanEventRecorder() {
        return trace.currentSpanEventRecorder();
    }

    @Override
    public int getCallStackFrameId() {
        return trace.getCallStackFrameId();
    }
    
    @Override
    public TraceType getTraceType() {
        return TraceType.ASYNC;
    }
}