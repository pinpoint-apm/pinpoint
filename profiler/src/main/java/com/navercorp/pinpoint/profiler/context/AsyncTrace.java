package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.CallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.RootCallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceType;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ParsingResult;

public class AsyncTrace implements Trace {
    private static final int BEGIN_STACKID = 1;
    
    private final Trace trace;
    private int asyncId;
    private short asyncSequence;

    public AsyncTrace(final Trace trace, final int asyncId, final short asyncSequence, final long startTime) {
        this.trace = trace;
        this.trace.rootCallStackFrame().recordStartTime(startTime);
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
    public CallStackFrame traceBlockBegin() {
        final CallStackFrame recorder = trace.traceBlockBegin();
        recorder.recordAsyncId(asyncId);
        recorder.recordAsyncSequence(asyncSequence);
        
        return recorder;
    }

    @Override
    public CallStackFrame traceBlockBegin(int stackId) {
        final CallStackFrame recorder = trace.traceBlockBegin(stackId);
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
        // TODO fix me
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
    public RootCallStackFrame rootCallStackFrame() {
        return trace.rootCallStackFrame();
    }

    @Override
    public CallStackFrame currentCallStackFrame() {
        return trace.currentCallStackFrame();
    }

    @Override
    public int getCallStackFrameId() {
        return trace.getCallStackFrameId();
    }
    
//    @Override
//    public void recordLogging(boolean isLogging) {
//        trace.recordLogging(isLogging);
//    }

    @Override
    public TraceType getTraceType() {
        return TraceType.ASYNC;
    }
}