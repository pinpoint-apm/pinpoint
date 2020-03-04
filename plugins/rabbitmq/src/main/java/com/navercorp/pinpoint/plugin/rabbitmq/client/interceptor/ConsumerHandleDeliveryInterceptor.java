package com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.rabbitmq.client.RabbitMQClientConstants;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;

/**
 * @author Jinkai.Ma
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
public class ConsumerHandleDeliveryInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private static final String ASYNC_TRACE_SCOPE = AsyncContext.ASYNC_TRACE_SCOPE;

    private final MethodDescriptor methodDescriptor;

    public ConsumerHandleDeliveryInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext");
        }
        if (methodDescriptor == null) {
            throw new NullPointerException("methodDescriptor");
        }
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (!validate(target, args)) {
            return;
        }
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args[1]);
        if (asyncContext == null) {
            return;
        }

        final Trace trace = getAsyncTrace(asyncContext);
        if (trace == null) {
            return;
        }

        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        // entry scope.
        entryAsyncTraceScope(trace);

        try {
            // trace event for default & async
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(RabbitMQClientConstants.RABBITMQ_CLIENT_INTERNAL);
            recorder.recordApi(methodDescriptor);
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE error.", t);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (!validate(target, args)) {
            return;
        }
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args[1]);
        if (asyncContext == null) {
            return;
        }

        final Trace trace = asyncContext.currentAsyncTraceObject();
        if (trace == null) {
            return;
        }

        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        // leave scope
        if (!leaveAsyncTraceScope(trace)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to leave scope of async trace {}.", trace);
            }
            // delete unstable trace
            deleteAsyncContext(trace, asyncContext);
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (throwable != null) {
                recorder.recordException(throwable);
            }
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error.", t);
            }
        } finally {
            trace.traceBlockEnd();
            if (isAsyncTraceDestination(trace)) {
                deleteAsyncContext(trace, asyncContext);
            }
        }
    }

    private boolean validate(Object target, Object[] args) {
        if (!(target instanceof Consumer)) {
            return false;
        }
        if (args == null || args.length < 2) {
            return false;
        }
        if (!(args[1] instanceof Envelope)) {
            return false;
        }
        if (!(args[1] instanceof AsyncContextAccessor)) {
            if (isDebug) {
                logger.debug("Invalid args[1] object. Need accessor({}).", AsyncContextAccessor.class.getName());
            }
            return false;
        }
        return true;
    }

    private Trace getAsyncTrace(AsyncContext asyncContext) {
        final Trace trace = asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to continue async trace. 'result is null'");
            }
            return null;
        }
        if (isDebug) {
            logger.debug("getAsyncTrace() trace {}, asyncContext={}", trace, asyncContext);
        }
        return trace;
    }

    private void deleteAsyncContext(Trace trace, AsyncContext asyncContext) {
        if (isDebug) {
            logger.debug("Delete async trace {}.", trace);
        }
        trace.close();
        asyncContext.close();
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

    private boolean isAsyncTraceDestination(final Trace trace) {
        if (!trace.isAsync()) {
            return false;
        }
        final TraceScope scope = trace.getScope(ASYNC_TRACE_SCOPE);
        return scope != null && !scope.isActive();
    }
}
