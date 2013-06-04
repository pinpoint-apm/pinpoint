package com.nhn.pinpoint.profiler.modifier.bloc.handler.interceptor;

import java.util.Enumeration;
import java.util.UUID;

import com.nhn.pinpoint.profiler.context.*;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.Logger;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.sampler.util.SamplingFlagUtils;
import com.nhn.pinpoint.profiler.util.NumberUtils;

/**
 * @author netspider
 */
public class ExecuteMethodInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final Logger logger = LoggerFactory.getLogger(ExecuteMethodInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isInfo = logger.isInfoEnabled();

    private MethodDescriptor descriptor;
    private TraceContext traceContext;

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            external.org.apache.coyote.Request request = (external.org.apache.coyote.Request) args[0];

            boolean sampling = samplingEnable(request);
            if (!sampling) {
                // 샘플링 대상이 아닐 경우도 TraceObject를 생성하여, sampling 대상이 아니라는것을 명시해야 한다.
                // sampling 대상이 아닐경우 rpc 호출에서 sampling 대상이 아닌 것에 rpc호출 파라미터에 sampling disable 파라미터를 박을수 있다.
                traceContext.disableSampling();
                if (isDebug) {
                    logger.debug("mark disable sampling. skip trace");
                }
                return;
            }

            String requestURL = request.requestURI().toString();
            String remoteAddr = request.remoteAddr().toString();


            TraceID traceId = populateTraceIdFromRequest(request);
            Trace trace;
            if (traceId != null) {
                if (isInfo) {
                    logger.debug("TraceID exist. continue trace. {} requestUrl:{}, remoteAddr:{}", new Object[] {traceId, requestURL, remoteAddr });
                }
                trace = traceContext.continueTraceObject(traceId);
            } else {
                if (isInfo) {
                    logger.debug("TraceID not exist. start new trace. {} requestUrl:{}, remoteAddr:{}", new Object[] {traceId, requestURL, remoteAddr });
                }
                trace = traceContext.newTraceObject();
            }

            trace.markBeforeTime();

            trace.recordServiceType(ServiceType.BLOC);
            trace.recordRpcName(requestURL);


            trace.recordEndPoint(request.protocol().toString() + ":" + request.serverName().toString() + ":" + request.getServerPort());
            trace.recordDestinationId(request.serverName().toString() + ":" + request.getServerPort());
            trace.recordAttribute(AnnotationKey.HTTP_URL, request.requestURI().toString());


        } catch (Throwable e) {
            if (logger.isWarnEnabled()) {
                logger.warn( "Tomcat StandardHostValve trace start fail. Caused:" + e.getMessage(), e);
            }
        }
    }



    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }

//        traceContext.getActiveThreadCounter().end();

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }
        traceContext.detachTraceObject();

        if (!trace.canSampled()) {
            return;
        }

        external.org.apache.coyote.Request request = (external.org.apache.coyote.Request) args[0];
        String parameters = getRequestParameter(request);
        if (parameters != null && parameters.length() > 0) {
            trace.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
        }

		trace.recordApi(descriptor);

        trace.recordException(result);

        trace.markAfterTime();
        trace.traceRootBlockEnd();
    }

    private boolean samplingEnable(external.org.apache.coyote.Request request) {
        // optional 값.
        String samplingFlag = request.getHeader(Header.HTTP_SAMPLED.toString());
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
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
            int parentSpanID = NumberUtils.parseInteger(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanID.NULL);
            int spanID = NumberUtils.parseInteger(request.getHeader(Header.HTTP_SPAN_ID.toString()), SpanID.NULL);
            short flags = NumberUtils.parseShort(request.getHeader(Header.HTTP_FLAGS.toString()), (short) 0);

            TraceID id = this.traceContext.createTraceId(uuid, parentSpanID, spanID, flags);
            if (logger.isInfoEnabled()) {
                logger.info("TraceID exist. continue trace. " + id);
            }
            return id;
        } else {
            return null;
        }
    }

    private String getRequestParameter(external.org.apache.coyote.Request request) {
        Enumeration<?> attrs = request.getParameters().getParameterNames();

        final StringBuilder params = new StringBuilder(32);

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
        traceContext.cacheApi(descriptor);
    }


    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}