package com.navercorp.pinpoint.plugin.resin.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.resin.ResinAsyncListener;
import com.navercorp.pinpoint.plugin.resin.ResinConstants;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;

/**
 * @author huangpengjie@fang.com
 * @author jaehong.kim
 */
public class HttpServletRequestImplInterceptor implements AroundInterceptor {
    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor descriptor;

    public HttpServletRequestImplInterceptor(TraceContext context, MethodDescriptor descriptor) {
        this.traceContext = context;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.traceBlockBegin();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (validate(target, result, throwable)) {
                final AsyncContext asyncContext = (AsyncContext) result;
                final AsyncListener asyncListener = new ResinAsyncListener(this.traceContext, recorder.recordNextAsyncContext(true));
                asyncContext.addListener(asyncListener);
                if (isDebug) {
                    logger.debug("Add async listener {}", asyncListener);
                }
            }
            recorder.recordServiceType(ResinConstants.RESIN_METHOD);
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } catch (Throwable t) {
            logger.warn("Failed to AFTER process. {}", t.getMessage(), t);
        } finally {
            trace.traceBlockEnd();
        }
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