package com.navercorp.pinpoint.plugin.hystrix.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanAsyncEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.plugin.hystrix.HystrixPluginConstants;

/**
 * @author Jiaqi Feng
 */
public class HystrixCommandExecuteCommandInterceptor extends SpanAsyncEventSimpleAroundInterceptor {

    public HystrixCommandExecuteCommandInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncTraceId asyncTraceId, Object target, Object[] args) {
        // do nothing
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(HystrixPluginConstants.HYSTRIX_SERVICE_TYPE);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }
}
