package com.nhn.pinpoint.bootstrap.interceptor;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.logging.PLogger;

/**
 * @author emeroad
 */
public abstract class SpanSimpleAroundInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {
    protected PLogger logger;
    protected boolean isDebug = false;

    protected MethodDescriptor descriptor;

    protected TraceContext traceContext;

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            final Trace trace = createTrace(args);
            if (!trace.canSampled()) {
                return;
            }
            doInBeforeTrace(trace);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("trace start fail. Caused:{}", th.getMessage(), th);
            }
        }
    }

    protected abstract void doInBeforeTrace(Trace trace);

    protected abstract Trace createTrace(Object[] args);

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
        if (!trace.canSampled()) {
            return;
        }
        try {
            doInAfterTrace(trace, args, result);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("Tomcat StandardHostValve trace start fail. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceRootBlockEnd();
        }
    }

    protected abstract void doInAfterTrace(Trace trace, Object[] args, Object result);


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
