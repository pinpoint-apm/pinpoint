package com.navercorp.pinpoint.plugin.arcus.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanAsyncEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;

/**
 * @author emeroad
 * @author jaehong.kim
 */
@Group(ArcusConstants.ARCUS_FUTURE_SCOPE)
public class FutureInternalMethodInterceptor extends SpanAsyncEventSimpleAroundInterceptor implements ArcusConstants {

    public FutureInternalMethodInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext, @Name(METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor) {
        super(traceContext, methodDescriptor, asyncTraceIdAccessor);
    }

    @Override
    protected void doInBeforeTrace(Trace trace, AsyncTraceId asyncTraceId, Object target, Object[] args) {
        trace.markBeforeTime();
    }

    @Override
    protected void doInAfterTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.recordServiceType(ARCUS_INTERNAL);
        trace.recordException(throwable);
        trace.recordApi(methodDescriptor);
        trace.markAfterTime();
    }
}