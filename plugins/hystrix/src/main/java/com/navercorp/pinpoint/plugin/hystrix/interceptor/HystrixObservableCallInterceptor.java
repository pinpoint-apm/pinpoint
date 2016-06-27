package com.navercorp.pinpoint.plugin.hystrix.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanAsyncEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.plugin.hystrix.HystrixPluginConstants;

import java.lang.reflect.Field;

/**
 * for hystrix-core above 1.4
 *
 * Created by jack on 4/21/16.
 */

public class HystrixObservableCallInterceptor extends SpanAsyncEventSimpleAroundInterceptor {

    public HystrixObservableCallInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncTraceId asyncTraceId, Object target, Object[] arqgs) {
        if (isDebug) {
            logger.debug("doInBeforeTrace()");
        }
    }

    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.debug("doInAfterTrace()");
        }
        recorder.recordServiceType(HystrixPluginConstants.HYSTRIX_SERVICE_TYPE);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
        target=getRealTarget(target);
        if (target != null)
            recorder.recordAttribute(HystrixPluginConstants.HYSTRIX_SUBCLASS_ANNOTATION_KEY, target.getClass().getSimpleName());
    }

    private Object getRealTarget(Object target) {
        Object cmd=null;

        try {
            Field field = target.getClass().getDeclaredField("this$0");
            if (field != null ) {
                field.setAccessible(true);
                cmd = field.get(target);
                if (isDebug) {
                    logger.debug("got outclass name is {}", cmd.getClass().getName());
                }
                return cmd;
            }
        } catch (NoSuchFieldException e) {
            if (isDebug) {
                logger.debug("got NoSuchFieldException exception for outer class this$0 does not exist");
            }
        } catch (IllegalAccessException e) {
            if (isDebug) {
                logger.debug("got IllegalAccessException exception when access outer class this$0");
            }
        }
        return cmd;
    }

    protected AsyncTraceId getAsyncTraceId(Object target) {
        target=getRealTarget(target);
        return target != null && target instanceof AsyncTraceIdAccessor ? ((AsyncTraceIdAccessor) target)._$PINPOINT$_getAsyncTraceId() : null;
    }
}
