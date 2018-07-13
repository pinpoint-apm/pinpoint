package com.navercorp.pinpoint.plugin.openwhisk.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.plugin.openwhisk.accessor.PinpointTraceAccessor;

/**
 * @author upgle (Seonghyun, Oh)
 */
public class TransactionIdFinishedInterceptor implements AroundInterceptor {

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public TransactionIdFinishedInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {

        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args[2]);
        final Trace trace = ((PinpointTraceAccessor) (args[2]))._$PINPOINT$_getPinpointTrace();

        if (asyncContext == null || trace == null) {
            return;
        }
        trace.traceBlockEnd();
        trace.close();
        asyncContext.close();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }

}

