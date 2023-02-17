package com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.rabbitmq.client.RabbitMQClientConstants;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;

/**
 * @author Jinkai.Ma
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
public class ConsumerHandleDeliveryInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    public ConsumerHandleDeliveryInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args) {
        if (!validate(target, args)) {
            return null;
        }

        return AsyncContextAccessorUtils.getAsyncContext(args, 1);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        if (!validate(target, args)) {
            return null;
        }

        return AsyncContextAccessorUtils.getAsyncContext(args, 1);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(RabbitMQClientConstants.RABBITMQ_CLIENT_INTERNAL);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }

    private boolean validate(Object target, Object[] args) {
        if (!(target instanceof Consumer)) {
            return false;
        }
        if (ArrayUtils.getLength(args) < 2) {
            return false;
        }
        if (!(args[1] instanceof Envelope)) {
            return false;
        }
        if (!(args[1] instanceof AsyncContextAccessor)) {
            return false;
        }
        return true;
    }
}
