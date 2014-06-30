package com.nhn.pinpoint.profiler.modifier.tomcat.interceptor;

import java.util.Enumeration;

import com.nhn.pinpoint.bootstrap.context.*;
import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.profiler.context.*;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;

import javax.servlet.http.HttpServletRequest;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.nhn.pinpoint.bootstrap.util.NetworkUtils;
import com.nhn.pinpoint.bootstrap.util.NumberUtils;
import com.nhn.pinpoint.bootstrap.util.StringUtils;

/**
 * @author emeroad
 */
public class StandardHostValveInvokeInterceptor extends SpanSimpleAroundInterceptor implements ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

    public StandardHostValveInvokeInterceptor() {
        super(PLoggerFactory.getLogger(StandardHostValveInvokeInterceptor.class));
    }

    @Override
    protected void doInBeforeTrace(Trace trace, Object[] args) {
        final HttpServletRequest request = (HttpServletRequest) args[0];
        trace.markBeforeTime();
        if (trace.canSampled()) {
            trace.recordServiceType(ServiceType.TOMCAT);

            final String requestURL = request.getRequestURI();
            trace.recordRpcName(requestURL);

            final int port = request.getServerPort();
            final String endPoint = request.getServerName() + ":" + port;
            trace.recordEndPoint(endPoint);

            final String remoteAddr = request.getRemoteAddr();
            trace.recordRemoteAddress(remoteAddr);
        }

        if (!trace.isRoot()) {
            recordParentInfo(trace, request);
        }
    }

    @Override
    protected Trace createTrace(Object[] args) {
        final HttpServletRequest request = (HttpServletRequest) args[0];
        // remote call에 sampling flag가 설정되어있을 경우는 샘플링 대상으로 삼지 않는다.
        final boolean sampling = samplingEnable(request);
        if (!sampling) {
            // 샘플링 대상이 아닐 경우도 TraceObject를 생성하여, sampling 대상이 아니라는것을 명시해야 한다.
            // sampling 대상이 아닐경우 rpc 호출에서 sampling 대상이 아닌 것에 rpc호출 파라미터에 sampling disable 파라미터를 박을수 있다.
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("remotecall sampling flag found. skip trace requestUrl:{}, remoteAddr:{}", request.getRequestURI(), request.getRemoteAddr());
            }
            return trace;
        }


        final TraceId traceId = populateTraceIdFromRequest(request);
        if (traceId != null) {
            // TODO remote에서 sampling flag로 마크가되는 대상으로 왔을 경우도 추가로 샘플링 칠수 있어야 할것으로 보임.
            final Trace trace = traceContext.continueTraceObject(traceId);
            // 서버 맵을 통계정보에서 조회하려면 remote로 호출되는 WAS의 관계를 알아야해서 부모의 application name을 전달받음.

            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[] {traceId, request.getRequestURI(), request.getRemoteAddr()});
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[] {traceId, request.getRequestURI(), request.getRemoteAddr()});
                }
            }
            return trace;
        } else {
            final Trace trace = traceContext.newTraceObject();
            if (trace.canSampled()){
                if (isDebug) {
                    logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", request.getRequestURI(), request.getRemoteAddr());
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", request.getRequestURI(), request.getRemoteAddr());
                }
            }
            return trace;
        }
    }


    private void recordParentInfo(Trace trace, HttpServletRequest request) {
        String parentApplicationName = request.getHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString());
        if (parentApplicationName != null) {
            trace.recordAcceptorHost(NetworkUtils.getHostFromURL(request.getRequestURL().toString()));

            final String type = request.getHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString());
            final short parentApplicationType = NumberUtils.parseShort(type, ServiceType.UNDEFINED.getCode());
            trace.recordParentApplication(parentApplicationName, parentApplicationType);
        }
    }

    @Override
    protected void doInAfterTrace(Trace trace, Object[] args, Object result) {
        if (trace.canSampled()) {
            final HttpServletRequest request = (HttpServletRequest) args[0];
            final String parameters = getRequestParameter(request, 64, 512);
            if (parameters != null && parameters.length() > 0) {
                trace.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
            }

            trace.recordApi(descriptor);
        }
        trace.recordException(result);
        trace.markAfterTime();
    }

    /**
     * Pupulate source trace from HTTP Header.
     *
     * @param request
     * @return
     */
    private TraceId populateTraceIdFromRequest(HttpServletRequest request) {

        String transactionId = request.getHeader(Header.HTTP_TRACE_ID.toString());
        if (transactionId != null) {

            long parentSpanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL);
            long spanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_SPAN_ID.toString()), SpanId.NULL);
            short flags = NumberUtils.parseShort(request.getHeader(Header.HTTP_FLAGS.toString()), (short) 0);

            final TraceId id = this.traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);
            if (isDebug) {
                logger.debug("TraceID exist. continue trace. {}", id);
            }
            return id;
        } else {
            return null;
        }
    }

    private boolean samplingEnable(HttpServletRequest request) {
        // optional 값.
        final String samplingFlag = request.getHeader(Header.HTTP_SAMPLED.toString());
        if (isDebug) {
            logger.debug("SamplingFlag:{}", samplingFlag);
        }
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    private String getRequestParameter(HttpServletRequest request, int eachLimit, int totalLimit) {
        Enumeration<?> attrs = request.getParameterNames();
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
            Object value = request.getParameter(key);
            if (value != null) {
                params.append(StringUtils.drop(StringUtils.toString(value), eachLimit));
            }
        }
        return params.toString();
    }

}
