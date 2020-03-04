package com.navercorp.pinpoint.plugin.resin.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.resin.ResinAsyncListener;
import com.navercorp.pinpoint.plugin.resin.ResinConstants;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;

/**
 * @author huangpengjie@fang.com
 * @author jaehong.kim
 */
public class HttpServletRequestImplInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public HttpServletRequestImplInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (validate(target, result, throwable)) {
            final AsyncContext asyncContext = (AsyncContext) result;
            final AsyncListener asyncListener = new ResinAsyncListener(this.traceContext, recorder.recordNextAsyncContext(true));
            asyncContext.addListener(asyncListener);
            if (isDebug) {
                logger.debug("Add async listener {}", asyncListener);
            }
        }
        recorder.recordServiceType(ResinConstants.RESIN_METHOD);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }

    private boolean validate(final Object target, final Object result, final Throwable throwable) {
        if (throwable != null || result == null) {
            return false;
        }

        if (!(target instanceof HttpServletRequest)) {
            if (isDebug) {
                logger.debug("Invalid target object, The javax.servlet.http.HttpServletRequest interface is not implemented. target={}", target);
            }
            return false;
        }

        if (!(result instanceof AsyncContext)) {
            if (isDebug) {
                logger.debug("Invalid result object, The javax.servlet.AsyncContext interface is not implemented. result={}.", result);
            }
            return false;
        }
        return true;
    }

}