package com.navercorp.pinpoint.plugin.hystrix.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.hystrix.HystrixPluginConstants;

/**
 * @author Jiaqi Feng
 */
public class HystrixCommandQueueInterceptor implements AroundInterceptor1 {
    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;
    private final InterceptorScope scope;

    public HystrixCommandQueueInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object arg0) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(HystrixPluginConstants.HYSTRIX_SERVICE_TYPE);
        recorder.recordApi(descriptor);

        // To trace async invocations, you have to get async trace id like below.
        AsyncTraceId asyncTraceId = trace.getAsyncTraceId();

        // Then record the AsyncTraceId as next async id.
        recorder.recordNextAsyncId(asyncTraceId.getAsyncId());

        // Finally, you have to pass the AsyncTraceId to the thread which handles the async task.
        ((AsyncTraceIdAccessor)target)._$PINPOINT$_setAsyncTraceId(asyncTraceId);
    }

    @Override
    public void after(Object target, Object arg0, Object result, Throwable throwable) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            if (throwable != null) {
                SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}
