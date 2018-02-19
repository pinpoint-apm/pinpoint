package com.navercorp.pinpoint.plugin.akka.http.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.akka.http.AkkaHttpConstants;

public class RequestContextImplRedirectInterceptor extends AsyncContextSpanEventEndPointInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(RequestContextImplRedirectInterceptor.class);

    public RequestContextImplRedirectInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {

    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(AkkaHttpConstants.AKKA_HTTP_SERVER_INTERNAL);
        recorder.recordException(throwable);
    }
}
