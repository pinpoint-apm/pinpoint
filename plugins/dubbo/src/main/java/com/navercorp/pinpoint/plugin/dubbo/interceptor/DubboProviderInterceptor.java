package com.navercorp.pinpoint.plugin.dubbo.interceptor;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.dubbo.DubboConstants;

/**
 * @author Jinkai.Ma
 * @author Jiaqi Feng
 */
public class DubboProviderInterceptor implements AroundInterceptor {
    private static final String SCOPE_NAME = "##DUBBO_PROVIDER_TRACE";
    protected final PLogger logger;
    protected final boolean isDebug;

    protected final MethodDescriptor methodDescriptor;
    protected final TraceContext traceContext;

    public DubboProviderInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;
        this.logger = PLoggerFactory.getLogger(DubboProviderInterceptor.class);
        this.isDebug = logger.isDebugEnabled();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            trace = createTrace(target, args);
            if (trace == null) {
                return;
            }

            try {
                final SpanRecorder recorder = trace.getSpanRecorder();
                doInBeforeTrace(recorder, target, args);
            } catch (Throwable th) {
                if (logger.isWarnEnabled()) {
                    logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
                }
            }
            return;
        }

        if(isDebug) {
            logger.debug("Found trace {}, sampled={}.", trace, trace.canSampled());
        }
        // adding scope as flag for not closing the trace created by other interceptor
        trace.addScope(SCOPE_NAME);

        if (!trace.canSampled()) {
            return;
        }

        RpcInvocation invocation = (RpcInvocation) args[0];
        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(DubboConstants.DUBBO_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
        recorder.recordApi(methodDescriptor);
        recorder.recordAttribute(DubboConstants.DUBBO_RPC_ANNOTATION_KEY,
                invocation.getInvoker().getInterface().getSimpleName() + ":" + invocation.getMethodName());
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        // TODO STATDISABLE this logic was added to disable statistics tracing
        if (!trace.canSampled()) {
            if (trace.getScope(SCOPE_NAME) == null) {
                deleteTrace(trace);
            }
            return;
        }

        try {
            if (trace.getScope(SCOPE_NAME) == null) {
                final SpanRecorder recorder = trace.getSpanRecorder();
                doInAfterTrace(recorder, target, args, result, throwable);
            } else {
                trace.traceBlockEnd();
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            if (trace.getScope(SCOPE_NAME) == null) {
                deleteTrace(trace);
            }
        }
    }

    private void deleteTrace(final Trace trace) {
        if (isDebug) {
            logger.debug("Delete provider include trace={}, sampled={}", trace, trace.canSampled());
        }
        traceContext.removeTraceObject();
        trace.close();
    }

    protected Trace createTrace(Object target, Object[] args) {
        Invoker invoker = (Invoker) target;

        // Ignore monitor service.
        if (DubboConstants.MONITOR_SERVICE_FQCN.equals(invoker.getInterface().getName())) {
            return traceContext.disableSampling();
        }

        RpcInvocation invocation = (RpcInvocation) args[0];

        // If this transaction is not traceable, mark as disabled.
        if (invocation.getAttachment(DubboConstants.META_DO_NOT_TRACE) != null) {
            return traceContext.disableSampling();
        }

        String transactionId = invocation.getAttachment(DubboConstants.META_TRANSACTION_ID);

        // If there's no trasanction id, a new trasaction begins here.
        // FIXME There seems to be cases where the invoke method is called after a span is already created.
        // We'll have to check if a trace object already exists and create a span event instead of a span in that case.
        if (transactionId == null) {
            return traceContext.newTraceObject();
        }

        // otherwise, continue tracing with given data.
        long parentSpanID = NumberUtils.parseLong(invocation.getAttachment(DubboConstants.META_PARENT_SPAN_ID), SpanId.NULL);
        long spanID = NumberUtils.parseLong(invocation.getAttachment(DubboConstants.META_SPAN_ID), SpanId.NULL);
        short flags = NumberUtils.parseShort(invocation.getAttachment(DubboConstants.META_FLAGS), (short) 0);
        TraceId traceId = traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);

        return traceContext.continueTraceObject(traceId);
    }

    protected void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args) {
        RpcInvocation invocation = (RpcInvocation) args[0];
        RpcContext rpcContext = RpcContext.getContext();

        // You have to record a service type within Server range.
        recorder.recordServiceType(DubboConstants.DUBBO_PROVIDER_SERVICE_TYPE);

        // Record rpc name, client address, server address.
        recorder.recordRpcName(invocation.getInvoker().getInterface().getSimpleName() + ":" + invocation.getMethodName());
        recorder.recordEndPoint(rpcContext.getLocalAddressString());
        recorder.recordRemoteAddress(rpcContext.getRemoteAddressString());

        // If this transaction did not begin here, record parent(client who sent this request) information
        if (!recorder.isRoot()) {
            String parentApplicationName = invocation.getAttachment(DubboConstants.META_PARENT_APPLICATION_NAME);

            if (parentApplicationName != null) {
                short parentApplicationType = NumberUtils.parseShort(invocation.getAttachment(DubboConstants.META_PARENT_APPLICATION_TYPE), ServiceType.UNDEFINED.getCode());
                recorder.recordParentApplication(parentApplicationName, parentApplicationType);

                // Pinpoint finds caller - callee relation by matching caller's end point and callee's acceptor host.
                // https://github.com/naver/pinpoint/issues/1395
                recorder.recordAcceptorHost(rpcContext.getLocalAddressString());
            }
        }
    }

    protected void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        RpcInvocation invocation = (RpcInvocation) args[0];

        recorder.recordApi(methodDescriptor);
        recorder.recordAttribute(DubboConstants.DUBBO_ARGS_ANNOTATION_KEY, invocation.getArguments());

        if (throwable == null) {
            recorder.recordAttribute(DubboConstants.DUBBO_RESULT_ANNOTATION_KEY, result);
        } else {
            recorder.recordException(throwable);
        }
    }
}
