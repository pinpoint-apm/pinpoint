package com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.rabbitmq.client.RabbitMQClientConstants;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;

import java.util.Objects;

/**
 * @author Jinkai.Ma
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
public class ConsumerHandleDeliveryInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor methodDescriptor;

    public ConsumerHandleDeliveryInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        Objects.requireNonNull(traceContext, "traceContext");
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "methodDescriptor");
    }

    @Override
    public void before(Object target, Object[] args) {
        if (!validate(target, args)) {
            return;
        }
        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 1);
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
        ScopeUtils.entryAsyncTraceScope(trace);

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
        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 1);
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
        if (!ScopeUtils.leaveAsyncTraceScope(trace)) {
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
            if (ScopeUtils.isAsyncTraceEndScope(trace)) {
                deleteAsyncContext(trace, asyncContext);
            }
        }
    }

    private boolean validate(Object target, Object[] args) {
        if (!(target instanceof Consumer)) {
            return false;
        }
        if (ArrayUtils.getLength(args) < 2) {
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
            if (isDebug) {
                logger.debug("Failed to continue async trace. 'result is null'");
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

}
