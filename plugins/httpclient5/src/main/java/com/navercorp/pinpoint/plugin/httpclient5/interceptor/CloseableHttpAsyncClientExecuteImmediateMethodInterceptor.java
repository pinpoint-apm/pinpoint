package com.navercorp.pinpoint.plugin.httpclient5.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5Constants;

public class CloseableHttpAsyncClientExecuteImmediateMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    public CloseableHttpAsyncClientExecuteImmediateMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        final AsyncContextAccessor asyncContextAccessor = ArrayArgumentUtils.getArgument(args, 3, AsyncContextAccessor.class);
        if (asyncContextAccessor != null) {
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            asyncContextAccessor._$PINPOINT$_setAsyncContext(asyncContext);
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
        recorder.recordServiceType(HttpClient5Constants.HTTP_CLIENT5_INTERNAL);
    }
}
