package com.navercorp.pinpoint.plugin.dubbo.interceptor;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.dubbo.DubboConstants;

/**
 * @author Jinkai.Ma
 */
public class DubboConsumerInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    public DubboConsumerInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        // Ignore monitor service
        if (isMonitorService(target)) {
            return;
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        final RpcInvocation invocation = (RpcInvocation) args[0];

        if (trace.canSampled()) {
            final SpanEventRecorder recorder = trace.traceBlockBegin();

            // RPC call trace have to be recorded with a service code in RPC client code range.
            recorder.recordServiceType(DubboConstants.DUBBO_CONSUMER_SERVICE_TYPE);

            // You have to issue a TraceId the receiver of this request will use.
            final TraceId nextId = trace.getTraceId().getNextTraceId();

            // Then record it as next span id.
            recorder.recordNextSpanId(nextId.getSpanId());

            // Finally, pass some tracing data to the server.
            // How to put them in a message is protocol specific.
            // This example assumes that the target protocol message can include any metadata (like HTTP headers).
            setAttachment(invocation, DubboConstants.META_TRANSACTION_ID, nextId.getTransactionId());
            setAttachment(invocation, DubboConstants.META_SPAN_ID, Long.toString(nextId.getSpanId()));
            setAttachment(invocation, DubboConstants.META_PARENT_SPAN_ID, Long.toString(nextId.getParentSpanId()));
            setAttachment(invocation, DubboConstants.META_PARENT_APPLICATION_TYPE, Short.toString(traceContext.getServerTypeCode()));
            setAttachment(invocation, DubboConstants.META_PARENT_APPLICATION_NAME, traceContext.getApplicationName());
            setAttachment(invocation, DubboConstants.META_FLAGS, Short.toString(nextId.getFlags()));

            setAttachment(invocation, DubboConstants.META_HOST, getHostAddress(invocation));
        } else {
            // If sampling this transaction is disabled, pass only that infomation to the server.
            setAttachment(invocation, DubboConstants.META_DO_NOT_TRACE, "1");
        }
    }

    private String getHostAddress(RpcInvocation invocation) {
        final URL url = invocation.getInvoker().getUrl();
        return HostAndPort.toHostAndPortString(url.getHost(), url.getPort());
    }

    private void setAttachment(RpcInvocation invocation, String name, String value) {
        invocation.setAttachment(name, value);
        if (isDebug) {
            logger.debug("Set attachment {}={}", name, value);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        // Ignore monitor service
        if (isMonitorService(target)) {
            return;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final RpcInvocation invocation = (RpcInvocation) args[0];
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            if (throwable == null) {
                String endPoint = RpcContext.getContext().getRemoteAddressString();
                // RPC client have to record end point (server address)
                recorder.recordEndPoint(endPoint);

                // Optionally, record the destination id (logical name of server. e.g. DB name)
                recorder.recordDestinationId(endPoint);
                recorder.recordAttribute(DubboConstants.DUBBO_ARGS_ANNOTATION_KEY, invocation.getArguments());
                recorder.recordAttribute(DubboConstants.DUBBO_RESULT_ANNOTATION_KEY, result);
            } else {
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean isMonitorService(Object target) {
        if (target instanceof Invoker) {
            Invoker invoker = (Invoker) target;
            return DubboConstants.MONITOR_SERVICE_FQCN.equals(invoker.getInterface().getName());
        }
        return false;
    }
}
