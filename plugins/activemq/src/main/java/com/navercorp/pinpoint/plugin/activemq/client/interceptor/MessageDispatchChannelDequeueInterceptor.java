package com.navercorp.pinpoint.plugin.activemq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Name;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientConstants;

/**
 * @author HyunGil Jeong
 */
@Scope(value = ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE, executionPolicy = ExecutionPolicy.INTERNAL)
public class MessageDispatchChannelDequeueInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public MessageDispatchChannelDequeueInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    // These methods may be polled, producing a lot of garbage log.
    // Instead, only log when the method is actually traced.
    @Override
    protected void logBeforeInterceptor(Object target, Object[] args) {
        return;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        if (isDebug) {
            super.logBeforeInterceptor(target, args);
        }
    }

    // These methods may be polled, producing a lot of garbage log.
    // Instead, only log when the method is actually traced.
    @Override
    protected void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        return;
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            super.logAfterInterceptor(target, args, result, throwable);
        }
        recorder.recordServiceType(ActiveMQClientConstants.ACTIVEMQ_CLIENT_INTERNAL);
        recorder.recordApi(getMethodDescriptor());
        if (throwable != null) {
            recorder.recordException(throwable);
        }
    }
}