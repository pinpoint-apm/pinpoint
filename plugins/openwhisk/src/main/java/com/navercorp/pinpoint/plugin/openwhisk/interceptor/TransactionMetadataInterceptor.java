package com.navercorp.pinpoint.plugin.openwhisk.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConstants;
import com.navercorp.pinpoint.plugin.openwhisk.descriptor.DefaultMethodDescriptor;

/**
 * @author upgle (Seonghyun, Oh)
 */
public class TransactionMetadataInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(TransactionMetadataInterceptor.class);

    private static final DefaultMethodDescriptor TRANSACTION_METHOD_DESCRIPTOR = new DefaultMethodDescriptor("TRANSACTION");
    private static final DefaultMethodDescriptor TRANSACTION_START_METHOD_DESCRIPTOR = new DefaultMethodDescriptor("TRANSACTION START");

    private final TraceContext traceContext;

    public TransactionMetadataInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

        AsyncContext currentContext = ((AsyncContextAccessor) target)._$PINPOINT$_getAsyncContext();
        if (currentContext != null) {
            return;
        }
        final Trace trace = populateTraceId((String) args[0]);
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(OpenwhiskConstants.OPENWHISK_INTERNAL);
            recorder.recordApi(TRANSACTION_START_METHOD_DESCRIPTOR);

            TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());

            String applicationName = traceContext.getApplicationName();
            String serverTypeCode = Short.toString(traceContext.getServerTypeCode());

            ((IdSetter) (target))._$PINPOINT$_setId(nextId.getTransactionId()+"@"+String.valueOf(nextId.getSpanId())+"@"+String.valueOf(nextId.getParentSpanId())+"@"+String.valueOf(nextId.getFlags()) + "@" + applicationName + "@" + serverTypeCode);

            final AsyncContext asyncContext = recorder.recordNextAsyncContext(true);
            ((AsyncContextAccessor) target)._$PINPOINT$_setAsyncContext(asyncContext);

        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", t.getMessage(), t);
            }
        } finally {
            trace.traceBlockEnd();
            deleteTrace(trace);
        }
    }

    private void deleteTrace(final Trace trace) {
        traceContext.removeTraceObject();
        trace.close();
    }

    private Trace populateTraceId(String id) {

        String[] tokens = id.split("@");

        if (tokens.length != 6) {
            return null;
        }
        String transactionId = tokens[0];
        String spanID = tokens[1];
        String parentSpanID = tokens[2];
        String flags = tokens[3];
        String applicationName = tokens[4];
        String serverTypeCode = tokens[5];

        TraceId traceId = traceContext.createTraceId(transactionId, NumberUtils.parseLong(parentSpanID, SpanId.NULL),  NumberUtils.parseLong(spanID, SpanId.NULL), NumberUtils.parseShort(flags, (short) 0));

        if (traceId != null) {
            Trace trace = traceContext.continueTraceObject(traceId);

            final SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordServiceType(OpenwhiskConstants.OPENWHISK_INTERNAL);
            recorder.recordApi(TRANSACTION_METHOD_DESCRIPTOR);
            recorder.recordParentApplication(applicationName, Short.valueOf(serverTypeCode));
            recorder.recordAcceptorHost(OpenwhiskConstants.CALLER);
            return trace;
        }
        return null;
    }
}

