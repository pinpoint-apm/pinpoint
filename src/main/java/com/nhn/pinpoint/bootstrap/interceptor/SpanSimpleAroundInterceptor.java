package com.nhn.pinpoint.bootstrap.interceptor;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.logging.PLogger;

/**
 * @author emeroad
 */
public abstract class SpanSimpleAroundInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {
    protected final PLogger logger;
    protected final boolean isDebug;

    protected MethodDescriptor descriptor;

    protected TraceContext traceContext;

    protected SpanSimpleAroundInterceptor(PLogger logger) {
        this.logger = logger;
        this.isDebug = logger.isDebugEnabled();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            final Trace trace = createTrace(target, args);
            doInBeforeTrace(trace, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("before. Caused:{}", th.getMessage(), th);
            }
        }
    }

    protected abstract void doInBeforeTrace(final Trace trace, Object target, final Object[] args);

    protected abstract Trace createTrace(final Object target, final Object[] args);

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }
        traceContext.detachTraceObject();
        try {
            doInAfterTrace(trace, target, args, result);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("after. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceRootBlockEnd();
        }
    }

    protected abstract void doInAfterTrace(final Trace trace, final Object target, final Object[] args, final Object result);


    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext.cacheApi(descriptor);
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}
