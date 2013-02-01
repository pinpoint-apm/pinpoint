package com.profiler.modifier.bloc.handler.interceptors;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.common.AnnotationNames;
import com.profiler.common.ServiceType;
import com.profiler.context.Header;
import com.profiler.context.SpanID;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.context.TraceID;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.NumberUtils;
import com.profiler.util.StringUtils;

/**
 * @author netspider
 */
public class ExecuteMethodInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport {

    private final Logger logger = Logger.getLogger(ExecuteMethodInterceptor.class.getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

    private MethodDescriptor descriptor;
//    private int apiId;

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            LoggingUtils.logBefore(logger, target, className, methodName, parameterDescription, args);
        }

        try {
            TraceContext traceContext = TraceContext.getTraceContext();
            traceContext.getActiveThreadCounter().start();

            external.org.apache.coyote.Request request = (external.org.apache.coyote.Request) args[0];
            String requestURL = request.requestURI().toString();
            String clientIP = request.remoteAddr().toString();
            String parameters = getRequestParameter(request);

            TraceID traceId = populateTraceIdFromRequest(request);
            Trace trace;
            if (traceId != null) {
                // TraceID nextTraceId = traceId.getNextTraceId();
                if (logger.isLoggable(Level.INFO)) {
                    // logger.info("TraceID exist. continue trace. " + nextTraceId);
                    logger.info("TraceID exist. continue trace. " + traceId);
                    logger.log(Level.FINE, "requestUrl:" + requestURL + " clientIp" + clientIP + " parameter:" + parameters);
                }
                // trace = new Trace(nextTraceId);
                trace = new Trace(traceId);
                traceContext.attachTraceObject(trace);
            } else {
                trace = new Trace();
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("TraceID not exist. start new trace. " + trace.getTraceId());
                    logger.log(Level.FINE, "requestUrl:" + requestURL + " clientIp" + clientIP + " parameter:" + parameters);
                }
                traceContext.attachTraceObject(trace);
            }

            trace.markBeforeTime();
            trace.recordRpcName(ServiceType.BLOC, traceContext.getApplicationId(), requestURL);
            trace.recordEndPoint(request.protocol().toString() + ":" + request.serverName().toString() + ":" + request.getServerPort());
            trace.recordAttribute(AnnotationNames.HTTP_URL, request.requestURI().toString());
            if (parameters != null && parameters.length() > 0) {
                trace.recordAttribute(AnnotationNames.HTTP_PARAM, parameters);
            }

        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Tomcat StandardHostValve trace start fail. Caused:" + e.getMessage(), e);
            }
        }
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args, result);
        }

        TraceContext traceContext = TraceContext.getTraceContext();
        traceContext.getActiveThreadCounter().end();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        traceContext.detachTraceObject();
        if (trace.getCurrentStackFrame().getStackFrameId() != 0) {
            logger.warning("Corrupted CallStack found. StackId not Root(0)");
            // 문제 있는 callstack을 dump하면 도움이 될듯.
        }

		trace.recordApi(descriptor);
//        trace.recordApi(this.apiId);
        trace.recordException(result);

        trace.markAfterTime();
        trace.traceBlockEnd();
    }

    /**
     * Pupulate source trace from HTTP Header.
     *
     * @param request
     * @return
     */
    private TraceID populateTraceIdFromRequest(external.org.apache.coyote.Request request) {
        String strUUID = request.getHeader(Header.HTTP_TRACE_ID.toString());
        if (strUUID != null) {
            UUID uuid = UUID.fromString(strUUID);
            long parentSpanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanID.NULL);
            long spanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_SPAN_ID.toString()), SpanID.NULL);
            boolean sampled = Boolean.parseBoolean(request.getHeader(Header.HTTP_SAMPLED.toString()));
            short flags = NumberUtils.parseShort(request.getHeader(Header.HTTP_FLAGS.toString()), (short) 0);

            TraceID id = new TraceID(uuid, parentSpanID, spanID, sampled, flags);
            if (logger.isLoggable(Level.INFO)) {
                logger.info("TraceID exist. continue trace. " + id);
            }
            return id;
        } else {
            return null;
        }
    }

    private String getRequestParameter(external.org.apache.coyote.Request request) {
        Enumeration<?> attrs = request.getParameters().getParameterNames();

        StringBuilder params = new StringBuilder();

        while (attrs.hasMoreElements()) {
            String keyString = attrs.nextElement().toString();
            Object value = request.getParameters().getParameter(keyString);

            if (value != null) {
                String valueString = value.toString();
                int valueStringLength = valueString.length();

                if (valueStringLength > 0 && valueStringLength < 100)
                    params.append(keyString).append("=").append(valueString);
            }
        }

        return params.toString();
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        TraceContext traceContext = TraceContext.getTraceContext();
        traceContext.cacheApi(descriptor);
    }


}