package com.navercorp.pinpoint.plugin.thread.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.thread.ThreadConstants;

/**
 * @author echo
 */
public class ThreadConstructorInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public ThreadConstructorInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        if (target instanceof AsyncContextAccessor) {
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            ((AsyncContextAccessor) target)._$PINPOINT$_setAsyncContext(asyncContext);
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordApi(this.methodDescriptor);
        recorder.recordServiceType(ThreadConstants.SERVICE_TYPE);
        recorder.recordException(throwable);
    }

}