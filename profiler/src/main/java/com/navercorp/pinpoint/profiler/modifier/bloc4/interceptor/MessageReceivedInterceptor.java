package com.nhn.pinpoint.profiler.modifier.bloc4.interceptor;

import com.nhn.pinpoint.bootstrap.context.RecordableTrace;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.common.ServiceType;

public class MessageReceivedInterceptor extends SpanSimpleAroundInterceptor implements TargetClassLoader {

    public MessageReceivedInterceptor() {
        super(MessageReceivedInterceptor.class);
    }

    @Override
    public void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {
        trace.markBeforeTime();
        
        if (trace.canSampled()) {
            trace.recordServiceType(ServiceType.BLOC);
            trace.recordRpcName("NPC Call");

            final external.org.apache.mina.common.IoSession ioSession = (external.org.apache.mina.common.IoSession)args[1];
            trace.recordEndPoint(ioSession.getLocalAddress().toString());
            trace.recordRemoteAddress(ioSession.getRemoteAddress().toString());
        }

    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {
        final Trace trace = getTraceContext().newTraceObject();
        
        if (isDebug) {
            final external.org.apache.mina.common.IoSession ioSession = (external.org.apache.mina.common.IoSession)args[1];
            
            if (trace.canSampled()) {
                logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", new Object[] {ioSession.getRemoteAddress().toString()});
            } else {
                logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", new Object[] {ioSession.getRemoteAddress().toString()});
            }
        }
        
        return trace;
    }

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.recordApi(getMethodDescriptor());
        trace.recordException(throwable);
        trace.markAfterTime();
    }
}
