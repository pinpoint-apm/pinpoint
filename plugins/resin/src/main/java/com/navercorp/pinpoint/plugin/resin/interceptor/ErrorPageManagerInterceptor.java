package com.navercorp.pinpoint.plugin.resin.interceptor;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.resin.TraceAccessor;

/**
 * 
 * @author huangpengjie@fang.com
 *
 */
public class ErrorPageManagerInterceptor implements AroundInterceptor {

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor methodDescriptor;

    public ErrorPageManagerInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super();
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }
        // TODO STATDISABLE this logic was added to disable statistics tracing
        if (!trace.canSampled()) {
            traceContext.removeTraceObject();
            return;
        }
        final Exception exception = (Exception) args[0];
        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();

            recorder.recordApi(methodDescriptor);
            recorder.recordException(exception);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            traceContext.removeTraceObject();
            deleteTrace(trace, target, args, result, throwable);
        }
    }

    private void deleteTrace(Trace trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.traceBlockEnd();

        final HttpServletRequest request = (HttpServletRequest) args[1];
        if (!isAsynchronousProcess(request)) {
            trace.close();
            // reset
            setTraceMetadata(request, null);
        }
    }

    private boolean isAsynchronousProcess(final HttpServletRequest request) {
        return request.getDispatcherType() == DispatcherType.ASYNC;
    }

    private void setTraceMetadata(final HttpServletRequest request, final Trace trace) {
        if (request instanceof TraceAccessor) {
            ((TraceAccessor) request)._$PINPOINT$_setTrace(trace);
        }
    }
}
