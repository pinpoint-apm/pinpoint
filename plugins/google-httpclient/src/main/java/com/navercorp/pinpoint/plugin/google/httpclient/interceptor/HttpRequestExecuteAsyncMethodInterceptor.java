package com.navercorp.pinpoint.plugin.google.httpclient.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.google.httpclient.HttpClientConstants;

@Group(value = HttpClientConstants.EXECUTE_ASYNC_SCOPE, executionPoint = ExecutionPolicy.ALWAYS)
public class HttpRequestExecuteAsyncMethodInterceptor implements SimpleAroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor methodDescriptor;
    private InterceptorGroup interceptorGroup;

    public HttpRequestExecuteAsyncMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorGroup interceptorGroup) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.interceptorGroup = interceptorGroup;
    }
    
    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        try {
            // set asynchronous trace
            final AsyncTraceId asyncTraceId = trace.getAsyncTraceId();
            recorder.recordNextAsyncId(asyncTraceId.getAsyncId());
            
            // set async id.
            InterceptorGroupInvocation transaction = interceptorGroup.getCurrentInvocation();
            transaction.setAttachment(asyncTraceId);
            if (isDebug) {
                logger.debug("Set asyncTraceId metadata {}", asyncTraceId);
            }
        } catch (Throwable t) {
            logger.warn("Failed to before process. {}", t.getMessage(), t);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            recorder.recordServiceType(ServiceType.HTTP_CLIENT_INTERNAL);
            recorder.recordException(throwable);
            
            // remove async id.
            InterceptorGroupInvocation transaction = interceptorGroup.getCurrentInvocation();
            transaction.removeAttachment();
        } finally {
            trace.traceBlockEnd();
        }
    }
}