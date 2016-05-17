package com.navercorp.pinpoint.plugin.activemq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientConstants;

/**
 * @author HyunGil Jeong
 */
@Scope(value = ActiveMQClientConstants.ACTIVEMQ_CLIENT_SCOPE, executionPolicy = ExecutionPolicy.INTERNAL)
public class MessageDispatchChannelEnqueueInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public MessageDispatchChannelEnqueueInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {

    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(ActiveMQClientConstants.ACTIVEMQ_CLIENT_INTERNAL);
        recorder.recordApi(getMethodDescriptor());
        if (throwable != null) {
            recorder.recordException(throwable);
        }
    }
}