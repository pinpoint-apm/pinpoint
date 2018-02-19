package com.navercorp.pinpoint.plugin.openwhisk.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConfig;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConstants;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskInvokerMethodDescriptor;
import whisk.core.connector.ActivationMessage;


public class OpenwhiskInvokerReactiveInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(OpenwhiskInvokerReactiveInterceptor.class);
    private final MethodDescriptor descriptor;
    private final OpenwhiskConfig config;
    private final String CALL_SERVER;
    private TraceContext traceContext;
    private final OpenwhiskInvokerMethodDescriptor invokerMethodDescriptor = new OpenwhiskInvokerMethodDescriptor();

    public OpenwhiskInvokerReactiveInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;
        this.config = new OpenwhiskConfig(traceContext.getProfilerConfig());
        CALL_SERVER = config.getCaller();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (args[0] instanceof ActivationMessage) {
            String msgString = ((PinpointHeaderAccessor) (((ActivationMessage) args[0]).transid()))._$PINPOINT$_getPinpointHeader();
            if (msgString == null) return;
            String pinpointHeaders[] = msgString.split(OpenwhiskConstants.PINPOINT_HEADER_DELIMITIER);
            if (pinpointHeaders.length == OpenwhiskConstants.PINPOINT_HEADER_COUNT) {
                TraceId traceId;
                Trace trace = null;
                traceId = traceContext.createTraceId(pinpointHeaders[OpenwhiskConstants.TRACE_ID],
                        Long.parseLong(pinpointHeaders[OpenwhiskConstants.PARENT_SPAN_ID]),
                        Long.parseLong(pinpointHeaders[OpenwhiskConstants.SPAN_ID]),
                        Short.parseShort(pinpointHeaders[OpenwhiskConstants.FLAGS]));
                trace = traceContext.continueAsyncTraceObject(traceId);
                if (trace.canSampled()) {
                    final SpanRecorder recorder = trace.getSpanRecorder();
                    recordRootSpan(recorder);
                    recorder.recordParentApplication(pinpointHeaders[OpenwhiskConstants.PARENT_APPLICATION_NAME],
                            NumberUtils.parseShort(pinpointHeaders[OpenwhiskConstants.PARENT_APPLICATION_TYPE], ServiceType.UNDEFINED.getCode()));
                    recorder.recordAcceptorHost(CALL_SERVER);
                }
                final SpanEventRecorder recorder = trace.traceBlockBegin();
                recorder.recordServiceType(OpenwhiskConstants.OPENWHISK_INTERNAL);
                final AsyncContext asyncContext = recorder.recordNextAsyncContext(true);
                ((AsyncContextAccessor) ((ActivationMessage) args[0]).transid())._$PINPOINT$_setAsyncContext(asyncContext);
            }
        }
    }


    private void recordRootSpan(final SpanRecorder recorder) {
        recorder.recordServiceType(OpenwhiskConstants.OPENWHISK_INVOKER);
        recorder.recordApi(invokerMethodDescriptor);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", t.getMessage(), t);
            }
        } finally {
            trace.traceBlockEnd();
            deleteTrace(trace);
        }
    }

    private void deleteTrace(final Trace trace) {
        traceContext.removeTraceObject();
        trace.close();
    }
}
