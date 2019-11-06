package com.navercorp.pinpoint.plugin.hystrix.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.hystrix.HystrixPluginConstants;
import com.navercorp.pinpoint.plugin.hystrix.field.EnclosingInstanceAccessor;

/**
 * for hystrix-core above 1.4
 *
 * Created by jack on 4/21/16.
 */

public abstract class HystrixObservableCallInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    protected HystrixObservableCallInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE);
        recorder.recordApi(methodDescriptor);
        recorder.recordAttribute(HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_ANNOTATION_KEY, getExecutionType());
        recorder.recordException(throwable);
    }

    protected abstract String getExecutionType();

    protected Object getEnclosingInstance(Object target) {
        if (!(target instanceof EnclosingInstanceAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", EnclosingInstanceAccessor.class.getName());
            }
            return null;
        } else {
            return ((EnclosingInstanceAccessor) target)._$PINPOINT$_getEnclosingInstance();
        }
    }
}
