package com.profiler.modifier.servlet.interceptors;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.profiler.common.ServiceType;
import com.profiler.context.Header;
import com.profiler.context.SpanID;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.context.TraceID;
import com.profiler.interceptor.ApiIdSupport;
import com.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.interceptor.StaticAroundInterceptor;
import com.profiler.interceptor.TraceContextSupport;
import com.profiler.util.NumberUtils;
import com.profiler.util.StringUtils;

public class DoXXXInterceptor implements StaticAroundInterceptor, ByteCodeMethodDescriptorSupport, ApiIdSupport, TraceContextSupport {

    private final Logger logger = Logger.getLogger(DoXXXInterceptor.class.getName());
    private MethodDescriptor descriptor;
    private int apiId;
    private TraceContext traceContext;

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }

        try {
            traceContext.getActiveThreadCounter().start();

            HttpServletRequest request = (HttpServletRequest) args[0];
            String requestURL = request.getRequestURI();
            String clientIP = request.getRemoteAddr();

            TraceID traceId = populateTraceIdFromRequest(request);
            Trace trace;
            if (traceId != null) {
                TraceID nextTraceId = traceId.getNextTraceId();
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("TraceID exist. continue trace. " + nextTraceId);
                    logger.log(Level.FINE, "requestUrl:" + requestURL + " clientIp" + clientIP);
                }
                trace = new Trace(nextTraceId);
                traceContext.attachTraceObject(trace);
            } else {
                trace = new Trace();
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("TraceID not exist. start new trace. " + trace.getTraceId());
                    logger.log(Level.FINE, "requestUrl:" + requestURL + " clientIp" + clientIP);
                }
                traceContext.attachTraceObject(trace);
            }

            trace.markBeforeTime();
            trace.recordRpcName(ServiceType.TOMCAT, traceContext.getApplicationId(), requestURL);

            int port = request.getServerPort();
            trace.recordEndPoint(request.getProtocol() + ":" + request.getServerName() + ((port > 0) ? ":" + port : ""));
            trace.recordAttribute("http.url", request.getRequestURI());
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Tomcat StandardHostValve trace start fail. Caused:" + e.getMessage(), e);
            }
        }
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
        }

        traceContext.getActiveThreadCounter().end();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        traceContext.detachTraceObject();

        HttpServletRequest request = (HttpServletRequest) args[0];
        String parameters = getRequestParameter(request);
        if (parameters != null && parameters.length() > 0) {
            trace.recordAttribute("http.params", parameters);
        }


        if (trace.getCurrentStackFrame().getStackFrameId() != 0) {
            logger.warning("Corrupted CallStack found. StackId not Root(0)");
            // 문제 있는 callstack을 dump하면 도움이 될듯.
        }

        // trace.recordApi(descriptor);
        trace.recordApi(this.apiId);

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
    private TraceID populateTraceIdFromRequest(HttpServletRequest request) {
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

    private String getRequestParameter(HttpServletRequest request) {
        Enumeration<?> attrs = request.getParameterNames();
        StringBuilder params = new StringBuilder();

        while (attrs.hasMoreElements()) {
            String keyString = attrs.nextElement().toString();
            Object value = request.getParameter(keyString);

            if (value != null) {
                String valueString = value.toString();
                int valueStringLength = valueString.length();

                if (valueStringLength > 0 && valueStringLength < 100) {
                    params.append(keyString).append("=").append(valueString);
                }

                if (attrs.hasMoreElements()) {
                    params.append(", ");
                }
            }
        }
        return params.toString();
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}
