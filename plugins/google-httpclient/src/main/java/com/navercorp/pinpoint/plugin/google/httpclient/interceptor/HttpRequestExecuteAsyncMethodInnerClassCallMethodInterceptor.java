package com.navercorp.pinpoint.plugin.google.httpclient.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanAsyncEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.google.httpclient.HttpClientConstants;

public class HttpRequestExecuteAsyncMethodInnerClassCallMethodInterceptor extends SpanAsyncEventSimpleAroundInterceptor {

    public HttpRequestExecuteAsyncMethodInnerClassCallMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor, @Name(HttpClientConstants.METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor) {
        super(traceContext, descriptor, asyncTraceIdAccessor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncTraceId asyncTraceId, Object target, Object[] args) {
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(ServiceType.HTTP_CLIENT_INTERNAL);
        recorder.recordException(throwable);
    }
}