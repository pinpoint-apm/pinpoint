package com.navercorp.pinpoint.plugin.httpclient5.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5Constants;
import org.apache.hc.core5.http.HttpHost;

public class DefaultAsyncClientConnectionOperatorConnectInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    public DefaultAsyncClientConnectionOperatorConnectInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        final HttpHost httpHost = ArrayArgumentUtils.getArgument(args, 1, HttpHost.class);
        if (httpHost != null) {
            recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, httpHost.toString());
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
        recorder.recordServiceType(HttpClient5Constants.HTTP_CLIENT5_INTERNAL);
    }
}
