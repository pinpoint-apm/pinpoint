package com.navercorp.pinpoint.plugin.openwhisk.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConstants;
import com.navercorp.pinpoint.plugin.openwhisk.accessor.PinpointTraceAccessor;
import com.navercorp.pinpoint.plugin.openwhisk.descriptor.DefaultMethodDescriptor;
import scala.runtime.AbstractFunction0;
import whisk.common.LogMarkerToken;

/**
 * @author upgle (Seonghyun, Oh)
 */
public class TransactionIdStartedInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(TransactionIdStartedInterceptor.class);

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    protected final boolean isDebug = logger.isDebugEnabled();

    public TransactionIdStartedInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    /**
     *
     * @param target
     * @param args TransactionMetadata, Identify, LogMarkerToken
     */
    @Override
    public void before(Object target, Object[] args) {

        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args[0]);
        if (asyncContext == null) {
            logger.debug("Not found asynchronous invocation metadata {}", (LogMarkerToken)args[2]);
            return;
        }

        Trace trace = asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            logger.debug("trace object null");
        }

        try {
            trace.traceBlockBegin();
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }


    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args[0]);
        if (asyncContext == null) {
            logger.debug("Not found asynchronous invocation metadata");
            return;
        }
        if (isDebug) {
            logger.debug("Asynchronous invocation. asyncContext={}", asyncContext);
        }

        final Trace trace = asyncContext.currentAsyncTraceObject();
        if (trace == null) {
            return;
        }
        if (isDebug) {
            logger.debug("Asynchronous invocation. asyncTraceId={}, trace={}", asyncContext, trace);
        }
        traceContext.removeTraceObject();

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();

            String token = args[2].toString();
            String message = ((AbstractFunction0) args[3]).apply().toString();

            recorder.recordApi(new DefaultMethodDescriptor(token));

            if (token.startsWith("database_")) {
                recorder.recordServiceType(OpenwhiskConstants.COUCHDB_EXECUTE_QUERY);
                recorder.recordDestinationId("COUCHDB");
            } else {
                recorder.recordServiceType(OpenwhiskConstants.OPENWHISK_INTERNAL);
            }

            if (isDebug) {
                if (message.length() > 0) {
                    recorder.recordAttribute(OpenwhiskConstants.MARKER_MESSAGE, message);
                }
            }

            if (result instanceof AsyncContextAccessor) {
                ((AsyncContextAccessor) (result))._$PINPOINT$_setAsyncContext(asyncContext);
                ((PinpointTraceAccessor) (result))._$PINPOINT$_setPinpointTrace(trace);
            }

        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        }
    }

}

