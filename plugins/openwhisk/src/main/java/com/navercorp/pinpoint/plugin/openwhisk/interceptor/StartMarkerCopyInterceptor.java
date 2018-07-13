package com.navercorp.pinpoint.plugin.openwhisk.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.openwhisk.accessor.PinpointTraceAccessor;

/**
 * @author upgle (Seonghyun, Oh)
 */
public class StartMarkerCopyInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(StartMarkerCopyInterceptor.class);

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public StartMarkerCopyInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        ((AsyncContextAccessor) (result))._$PINPOINT$_setAsyncContext(((AsyncContextAccessor) (target))._$PINPOINT$_getAsyncContext());
        ((PinpointTraceAccessor) (result))._$PINPOINT$_setPinpointTrace(((PinpointTraceAccessor) (target))._$PINPOINT$_getPinpointTrace());
    }


}

