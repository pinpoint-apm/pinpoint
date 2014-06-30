package com.nhn.pinpoint.profiler.modifier.bloc.handler.interceptor;

import java.util.Enumeration;

import com.nhn.pinpoint.bootstrap.context.Header;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.profiler.context.*;
import com.nhn.pinpoint.bootstrap.logging.PLogger;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.nhn.pinpoint.bootstrap.util.NumberUtils;
import com.nhn.pinpoint.bootstrap.util.StringUtils;
import external.org.apache.coyote.Request;

/**
 * @author netspider
 * @author emeroad
 */
public class ExecuteMethodInterceptor extends SpanSimpleAroundInterceptor implements ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {



    public ExecuteMethodInterceptor() {
        super(PLoggerFactory.getLogger(ExecuteMethodInterceptor.class));
    }

    @Override
    public void doInBeforeTrace(Trace trace, Object[] args) {

        final external.org.apache.coyote.Request request = (external.org.apache.coyote.Request) args[0];
        trace.markBeforeTime();
        if (trace.canSampled()) {
            trace.recordServiceType(ServiceType.BLOC);

            final String requestURL = request.requestURI().toString();
            trace.recordRpcName(requestURL);

            // TODO tomcat과 로직이 미묘하게 다름 차이점 알아내서 고칠것.
            // String remoteAddr = request.remoteAddr().toString();

            trace.recordEndPoint(request.protocol().toString() + ":" + request.serverName().toString() + ":" + request.getServerPort());
            trace.recordDestinationId(request.serverName().toString() + ":" + request.getServerPort());
            trace.recordAttribute(AnnotationKey.HTTP_URL, request.requestURI().toString());
        }
//      부모 정보를 샘플링여부를 따지면 안됨.
//      TODO 부모정보 레코딩로직이 없음.
//      if (!trace.isRoot()) {
//          recordParentInfo(trace, request);
//      }

    }

    @Override
    protected Trace createTrace(Object[] args) {
        final external.org.apache.coyote.Request request = (external.org.apache.coyote.Request) args[0];
        final boolean sampling = samplingEnable(request);
        if (!sampling) {
            // 샘플링 대상이 아닐 경우도 TraceObject를 생성하여, sampling 대상이 아니라는것을 명시해야 한다.
            // sampling 대상이 아닐경우 rpc 호출에서 sampling 대상이 아닌 것에 rpc호출 파라미터에 sampling disable 파라미터를 박을수 있다.
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("mark disable sampling. skip trace");
            }
            return trace;
        }


        final TraceId traceId = populateTraceIdFromRequest(request);
        if (traceId != null) {
            final Trace trace = traceContext.continueTraceObject(traceId);
            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[]{traceId, request.requestURI(), request.remoteAddr()});
                }
                return trace;
            } else {
                if (isDebug) {
                    logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[]{traceId, request.requestURI(), request.remoteAddr()});
                }
                return trace;
            }
        } else {
            final Trace trace = traceContext.newTraceObject();
            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", request.requestURI(), request.remoteAddr());
                }
                return trace;
            } else {
                if (isDebug) {
                    logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", request.requestURI(), request.remoteAddr());
                }
                return trace;
            }
        }
    }


    @Override
    public void doInAfterTrace(Trace trace, Object target, Object[] args, Object result) {

        if (trace.canSampled()) {
            external.org.apache.coyote.Request request = (external.org.apache.coyote.Request) args[0];
            String parameters = getRequestParameter(request, 64, 512);
            if (parameters != null && parameters.length() > 0) {
                trace.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
            }

            trace.recordApi(descriptor);
        }
        trace.recordException(result);
        trace.markAfterTime();
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
    private TraceId populateTraceIdFromRequest(external.org.apache.coyote.Request request) {
        String transactionId = request.getHeader(Header.HTTP_TRACE_ID.toString());
        if (transactionId != null) {
            long parentSpanId = NumberUtils.parseLong(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL);
            // TODO NULL이 되는게 맞는가?
            long spanId = NumberUtils.parseLong(request.getHeader(Header.HTTP_SPAN_ID.toString()), SpanId.NULL);
            short flags = NumberUtils.parseShort(request.getHeader(Header.HTTP_FLAGS.toString()), (short) 0);

            TraceId id = this.traceContext.createTraceId(transactionId, parentSpanId, spanId, flags);
            if (isDebug) {
                logger.debug("TraceID exist. continue trace. {}", id);
            }
            return id;
        } else {
            return null;
        }
    }

    private String getRequestParameter(external.org.apache.coyote.Request request, int eachLimit, int totalLimit) {
        Enumeration<?> attrs = request.getParameters().getParameterNames();
        final StringBuilder params = new StringBuilder(64);
        while (attrs.hasMoreElements()) {
            if (params.length() != 0 ) {
                params.append('&');
            }
            if (params.length() > totalLimit) {
                // 데이터 사이즈가 너무 클 경우 뒷 파라미터 생략.
                params.append("...");
                return  params.toString();
            }
            String key = attrs.nextElement().toString();
            params.append(StringUtils.drop(key, eachLimit));
            params.append("=");
            Object value = request.getParameters().getParameter(key);
            if (value != null) {
                params.append(StringUtils.drop(StringUtils.toString(value), eachLimit));
            }
        }

        return params.toString();
    }

}