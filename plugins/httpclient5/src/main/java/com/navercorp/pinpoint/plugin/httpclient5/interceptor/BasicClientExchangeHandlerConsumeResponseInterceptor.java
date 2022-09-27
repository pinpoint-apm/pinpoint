package com.navercorp.pinpoint.plugin.httpclient5.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5Constants;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5PluginConfig;
import org.apache.hc.core5.http.HttpResponse;

public class BasicClientExchangeHandlerConsumeResponseInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
    private final boolean statusCode;

    public BasicClientExchangeHandlerConsumeResponseInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
        final HttpClient5PluginConfig config = new HttpClient5PluginConfig(traceContext.getProfilerConfig());
        this.statusCode = config.isStatusCode();
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        if (statusCode) {
            final HttpResponse httpResponse = ArrayArgumentUtils.getArgument(args, 0, HttpResponse.class);
            if (httpResponse != null) {
                int statusCode = httpResponse.getCode();
                recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, statusCode);
            }
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
        recorder.recordServiceType(HttpClient5Constants.HTTP_CLIENT5_INTERNAL);
    }
}
