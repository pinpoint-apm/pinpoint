package com.navercorp.pinpoint.plugin.resin.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.resin.AsyncAccessor;
import com.navercorp.pinpoint.plugin.resin.ResinConstants;

/**
 * 
 * @author huangpengjie@fang.com
 *
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
            logger.beforeInterceptor(target, "", descriptor.getMethodName(), "", args);
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
            logger.afterInterceptor(target, "", descriptor.getMethodName(), "", args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (validate(target, result, throwable)) {
                ((AsyncAccessor) target)._$PINPOINT$_setAsync(Boolean.TRUE);

                // make asynchronous trace-id
                final AsyncTraceId asyncTraceId = trace.getAsyncTraceId();
                recorder.recordNextAsyncId(asyncTraceId.getAsyncId());
                // result is BasicFuture type check validate()
                ((AsyncTraceIdAccessor) result)._$PINPOINT$_setAsyncTraceId(asyncTraceId);
                if (isDebug) {
                    logger.debug("Set asyncTraceId metadata {}", asyncTraceId);
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

        if (!(target instanceof AsyncAccessor)) {
            logger.debug("Invalid target object. Need field accessor({}).", AsyncAccessor.class.getName());
            return false;
        }

        if (!(result instanceof AsyncTraceIdAccessor)) {
            logger.debug("Invalid target object. Need metadata accessor({}).", AsyncTraceIdAccessor.class.getName());
            return false;
        }

        return true;
    }

}
