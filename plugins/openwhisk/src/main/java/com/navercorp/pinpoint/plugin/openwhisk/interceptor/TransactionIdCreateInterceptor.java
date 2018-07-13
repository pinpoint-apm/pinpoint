package com.navercorp.pinpoint.plugin.openwhisk.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConstants;
import com.navercorp.pinpoint.plugin.openwhisk.descriptor.DefaultMethodDescriptor;

/**
 * @author upgle (Seonghyun, Oh)
 */
public class TransactionIdCreateInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(TransactionIdCreateInterceptor.class);

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    private static final DefaultMethodDescriptor TRANSACTION_START_METHOD_DESCRIPTOR = new DefaultMethodDescriptor("TRANSACTION START");

    public TransactionIdCreateInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;
    }

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (args[0] instanceof AsyncContextAccessor) {

            AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args[0]);
            if (asyncContext == null) {
                return;
            }

            Trace trace = asyncContext.continueAsyncTraceObject();

            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(OpenwhiskConstants.OPENWHISK_INTERNAL);
            recorder.recordApi(TRANSACTION_START_METHOD_DESCRIPTOR);
            recorder.recordEndPoint(OpenwhiskConstants.CALLER);

            ((AsyncContextAccessor) (result))._$PINPOINT$_setAsyncContext(recorder.recordNextAsyncContext(true));

            String applicationName = traceContext.getApplicationName();
            String serverTypeCode = Short.toString(traceContext.getServerTypeCode());


            TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());
            recorder.recordEndPoint(OpenwhiskConstants.CALLER);

            // Replace original transactionId to Pinpoint TransactionId
            ((IdSetter) (result))._$PINPOINT$_setId(nextId.getTransactionId()+"@"+String.valueOf(nextId.getSpanId())+"@"+String.valueOf(nextId.getParentSpanId())+"@"+String.valueOf(nextId.getFlags()) + "@" + applicationName + "@" + serverTypeCode);

            trace.traceBlockEnd();
        }
    }

}

