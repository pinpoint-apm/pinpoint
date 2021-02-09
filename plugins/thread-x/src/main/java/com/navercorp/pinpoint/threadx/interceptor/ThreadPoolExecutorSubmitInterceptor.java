package com.navercorp.pinpoint.threadx.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.threadx.ThreadPoolConstants;

public class ThreadPoolExecutorSubmitInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor descriptor;

    public ThreadPoolExecutorSubmitInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
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

        final SpanEventRecorder recorder = trace.traceBlockBegin();

        logger.info("zhangyinhao,loaddingThreadPoolPlugin...SpanEventRecorder={}",recorder);

        boolean r = validate(args);
        logger.info("zhangyinhao,loaddingThreadPoolPlugin...validate={}",r);
        if (r) {
            // make asynchronous trace-id
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            logger.info("zhangyinhao,loaddingThreadPoolPlugin...AsyncContext={}",asyncContext);
            ((AsyncContextAccessor) args[0])._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set asyncContext {}", asyncContext);
            }
        }
    }

    private boolean validate(final Object[] args) {
        if (args == null || args.length < 1) {
            if (isDebug) {
                logger.debug("Invalid args object. args={}.", args);
            }
            return false;
        }

        if (!(args[0] instanceof AsyncContextAccessor)) {
            if (isDebug) {
                logger.debug("Invalid args[0] object. Need metadata accessor({}).", AsyncContextAccessor.class.getName());
            }
            return false;
        }

        return true;
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
            recorder.recordApi(this.descriptor);
            recorder.recordServiceType(ThreadPoolConstants.THREAD_POOL_EXECUTOR);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }
}
