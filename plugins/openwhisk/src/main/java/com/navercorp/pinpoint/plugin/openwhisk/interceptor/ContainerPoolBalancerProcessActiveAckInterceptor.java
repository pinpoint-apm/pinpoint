package com.navercorp.pinpoint.plugin.openwhisk.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConfig;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConstants;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskControllerMethodDescriptor;

import java.nio.charset.StandardCharsets;


public class ContainerPoolBalancerProcessActiveAckInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(OpenwhiskInvokerReactiveInterceptor.class);
    private final MethodDescriptor descriptor;
    private final OpenwhiskConfig config;
    private TraceContext traceContext;
    private static String CALL_SERVER ;
    private final OpenwhiskControllerMethodDescriptor controllerMethodDescriptor = new OpenwhiskControllerMethodDescriptor();

    public ContainerPoolBalancerProcessActiveAckInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;
        this.config = new OpenwhiskConfig(traceContext.getProfilerConfig());
        CALL_SERVER = config.getCaller();
    }

    @Override
    public void before(Object target, Object[] args) {
        byte[] msg = (byte[]) args[0];
        if (msg[0] == 64) {
            String msgString = new String(msg, StandardCharsets.UTF_8);
            msgString = msgString.substring(OpenwhiskConstants.PINPOINT_HEADER_PREFIX_LENGTH,
                    msgString.indexOf(OpenwhiskConstants.PINPOINT_HEADER_POSTFIX));
            String pinpointHeaders[] = msgString.split(OpenwhiskConstants.PINPOINT_HEADER_DELIMITIER);
            if (pinpointHeaders.length == OpenwhiskConstants.PINPOINT_HEADER_COUNT) {
                TraceId traceId;
                Trace trace;
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
                    SpanEventRecorder recorder1 = trace.traceBlockBegin();
                    recorder1.recordApi(descriptor);
                    recorder1.recordServiceType(OpenwhiskConstants.OPENWHISK_INTERNAL);
                }
            }
        }
    }

    private void recordRootSpan(final SpanRecorder recorder) {
        recorder.recordServiceType(OpenwhiskConstants.OPENWHISK_INVOKER);
        recorder.recordApi(controllerMethodDescriptor);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
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
