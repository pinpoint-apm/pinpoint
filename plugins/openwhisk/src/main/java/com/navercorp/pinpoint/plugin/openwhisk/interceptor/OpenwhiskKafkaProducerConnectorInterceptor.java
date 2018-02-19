package com.navercorp.pinpoint.plugin.openwhisk.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConstants;
import whisk.core.connector.Message;


public class OpenwhiskKafkaProducerConnectorInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(OpenwhiskKafkaProducerConnectorInterceptor.class);
    protected static final String ASYNC_TRACE_SCOPE = AsyncContext.ASYNC_TRACE_SCOPE;
    private final MethodDescriptor descriptor;

    public OpenwhiskKafkaProducerConnectorInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor) {
        this.descriptor = methodDescriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (!(args[1] instanceof Message)) {
            return;
        }

        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(((Message) args[1]).transid());

        if (asyncContext == null) {
            logger.debug("AsyncContext not found");
            return;
        }

        final Trace trace = asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            return;
        }

        entryAsyncTraceScope(trace);

        try {
            // trace event for default & async.
            final SpanEventRecorder recorder = trace.traceBlockBegin();

        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    private void entryAsyncTraceScope(final Trace trace) {
        final TraceScope scope = trace.getScope(ASYNC_TRACE_SCOPE);
        if (scope != null) {
            scope.tryEnter();
        }
    }

    private boolean leaveAsyncTraceScope(final Trace trace) {
        final TraceScope scope = trace.getScope(ASYNC_TRACE_SCOPE);
        if (scope != null) {
            if (scope.canLeave()) {
                scope.leave();
            } else {
                return false;
            }
        }
        return true;
    }

    private void deleteAsyncContext(final Trace trace, AsyncContext asyncContext) {
        trace.close();
        asyncContext.close();
    }

    private boolean isAsyncTraceDestination(final Trace trace) {
        if (!trace.isAsync()) {
            return false;
        }

        final TraceScope scope = trace.getScope(ASYNC_TRACE_SCOPE);
        return scope != null && !scope.isActive();
    }


    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (!(args[1] instanceof Message)) {
            return;
        }
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(((Message) args[1]).transid());
        if (asyncContext == null) {
            logger.debug("AsyncContext not found");

            return;
        }

        final Trace trace = asyncContext.currentAsyncTraceObject();
        if (trace == null) {
            return;
        }

        // leave scope.
        if (!leaveAsyncTraceScope(trace)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to leave scope of async trace {}.", trace);
            }
            // delete unstable trace.
            deleteAsyncContext(trace, asyncContext);
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordServiceType(OpenwhiskConstants.OPENWHISK_INTERNAL);
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
            if (isAsyncTraceDestination(trace)) {
                deleteAsyncContext(trace, asyncContext);
            }
        }
    }
}
