package com.nhn.pinpoint.bootstrap.interceptor;

import com.nhn.pinpoint.bootstrap.context.RecordableTrace;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @author emeroad
 */
public abstract class SpanSimpleAroundInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {
    protected final PLogger logger;
    protected final boolean isDebug;

    private MethodDescriptor methodDescriptor;

    private TraceContext traceContext;

    protected SpanSimpleAroundInterceptor(Class<? extends SpanSimpleAroundInterceptor> childClazz) {
        this.logger = PLoggerFactory.getLogger(childClazz);
        this.isDebug = logger.isDebugEnabled();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            final Trace trace = createTrace(target, args);
            if (trace == null) {
                return;
            }
            doInBeforeTrace(trace, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("before. Caused:{}", th.getMessage(), th);
            }
        }
    }

    protected abstract void doInBeforeTrace(final RecordableTrace trace, Object target, final Object[] args);

    protected abstract Trace createTrace(final Object target, final Object[] args);

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }
        traceContext.detachTraceObject();
        try {
            doInAfterTrace(trace, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("after. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceRootBlockEnd();
        }
    }

    protected abstract void doInAfterTrace(final RecordableTrace trace, final Object target, final Object[] args, final Object result, Throwable throwable);


    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.methodDescriptor = descriptor;
        this.traceContext.cacheApi(descriptor);
    }

    public MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }
}
