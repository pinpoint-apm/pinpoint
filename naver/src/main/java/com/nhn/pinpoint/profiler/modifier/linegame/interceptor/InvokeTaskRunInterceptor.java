package com.nhn.pinpoint.profiler.modifier.linegame.interceptor;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.nhn.pinpoint.bootstrap.context.*;
import com.nhn.pinpoint.bootstrap.interceptor.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.nhn.pinpoint.bootstrap.util.MetaObject;
import com.nhn.pinpoint.bootstrap.util.NumberUtils;
import com.nhn.pinpoint.bootstrap.util.StringUtils;
import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.SpanId;
import org.jboss.netty.util.CharsetUtil;

/**
 * <pre>
 * com/linecorp/games/common/baseFramework/handlers/HttpCustomServerHandler$InvokeTask.run()를 인터셉트한다.
 * invoke task의 run메소드는 http request를 분석하고 bo를 찾아 실행 후 결과를 반환하는 역할을 담당하는 클래스이다.
 * </pre>
 *
 * @author netspider
 * @author emeroad
 */
public class InvokeTaskRunInterceptor extends SpanSimpleAroundInterceptor implements TargetClassLoader {

    private static final String DEFAULT_CHARSET = "UTF-8";

    public InvokeTaskRunInterceptor() {
        super(InvokeTaskRunInterceptor.class);
    }

    private MetaObject<org.jboss.netty.channel.ChannelHandlerContext> getChannelHandlerContext = new MetaObject<org.jboss.netty.channel.ChannelHandlerContext>("__getChannelHandlerContext");
    private MetaObject<org.jboss.netty.channel.MessageEvent> getMessageEvent = new MetaObject<org.jboss.netty.channel.MessageEvent>("__getMessageEvent");

    private int paramDumpSize = 512;
    private int entityDumpSize = 512;

    @Override
    public void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {

        org.jboss.netty.channel.ChannelHandlerContext channelHandlerContext = getChannelHandlerContext.invoke(target);
        org.jboss.netty.channel.MessageEvent e = getMessageEvent.invoke(target);

        if (channelHandlerContext == null) {
            logger.debug("ChannelHandlerContext is null.");
            return;
        }

        if (e == null) {
            logger.debug("MessageEvent is null.");
            // after에서도 체크하지않으면 사실 로직이 이상해짐.
            return;
        }

        if (!(e.getMessage() instanceof org.jboss.netty.handler.codec.http.HttpRequest)) {
            logger.debug("MessageEvent is not instance of org.jboss.netty.handler.codec.http.HttpRequest. {}", e.getMessage());
            return;
        }
        org.jboss.netty.handler.codec.http.HttpRequest request = (org.jboss.netty.handler.codec.http.HttpRequest) e.getMessage();
        Channel channel = e.getChannel();
        if (channel == null) {
            logger.debug("Channel is null.");
            return;
        }

        String requestURL = request.getUri();
        // FIXME 모든 address는 / 로 시작하나???


        final String endPoint = getLocalAddress(channel);

        trace.markBeforeTime();
        if (trace.canSampled()) {

            trace.recordServiceType(ServiceType.STAND_ALONE);
            trace.recordRpcName(requestURL);

            trace.recordEndPoint(endPoint);

            final String remoteAddr = getRemoteAddress(channel);
            trace.recordRemoteAddress(remoteAddr);
        }

        // 서버 맵을 통계정보에서 조회하려면 remote로 호출되는 WAS의 관계를 알아야해서 부모의 application
        // name을 전달받음.
        if (!trace.isRoot()) {
            String parentApplicationName = populateParentApplicationNameFromRequest(request);
            short parentApplicationType = populateParentApplicationTypeFromRequest(request);
            if (parentApplicationName != null) {
                trace.recordParentApplication(parentApplicationName, parentApplicationType);
                trace.recordAcceptorHost(endPoint);
            }
        } else {
            // TODO 여기에서 client 정보를 수집할 수 있다.
        }

    }



    @Override
    protected Trace createTrace(Object target, Object[] args) {
        org.jboss.netty.channel.MessageEvent messageEvent = getMessageEvent.invoke(target);
        org.jboss.netty.handler.codec.http.HttpRequest request = (org.jboss.netty.handler.codec.http.HttpRequest) messageEvent.getMessage();

        Channel channel = messageEvent.getChannel();
        if (channel == null) {
            logger.debug("Channel is null.");
            return null;
        }

        // FIXME 모든 address는 / 로 시작하나???
//            String remoteAddr = channel.getRemoteAddress().toString().substring(1);
//            String endPoint = channel.getLocalAddress().toString().substring(1);

        // check sampled
        final boolean sampling = isSamplingEnabled(request);
        if (!sampling) {
            // 샘플링 대상이 아닐 경우도 TraceObject를 생성하여, sampling 대상이 아니라는것을 명시해야
            // 한다.
            // sampling 대상이 아닐경우 rpc 호출에서 sampling 대상이 아닌 것에 rpc호출 파라미터에
            // sampling disable 파라미터를 박을수 있다.
            final TraceContext traceContext = getTraceContext();
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                String requestURL = request.getUri();
                String remoteAddr = getRemoteAddress(channel);
                logger.debug("remotecall sampling flag found. skip trace requestUrl:{}, remoteAddr:{}", requestURL, remoteAddr);
            }
            return trace;
        }

        final TraceId traceId = populateTraceIdFromRequest(request);
        if (traceId != null) {
            // TODO remote에서 sampling flag로 마크가되는 대상으로 왔을 경우도 추가로 샘플링 칠수 있어야
            // 할것으로 보임.
            final TraceContext traceContext = getTraceContext();
            final Trace trace = traceContext.continueTraceObject(traceId);
            if (trace.canSampled()) {
                if (isDebug) {
                    String requestURL = request.getUri();
                    String remoteAddr = getRemoteAddress(channel);
                    logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[]{traceId, requestURL, remoteAddr});
                }
                return trace;
            } else {
                if (isDebug) {
                    String requestURL = request.getUri();
                    String remoteAddr = getRemoteAddress(channel);
                    logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[]{traceId, requestURL, remoteAddr});

                }
                return trace;
            }
        } else {
            TraceContext traceContext = getTraceContext();
            final Trace trace = traceContext.newTraceObject();
            if (trace.canSampled()) {
                if (isDebug) {
                    String requestURL = request.getUri();
                    String remoteAddr = getRemoteAddress(channel);
                    logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", new Object[]{requestURL, remoteAddr});
                }
                return trace;
            } else {
                if (isDebug) {
                    String requestURL = request.getUri();
                    String remoteAddr = getRemoteAddress(channel);
                    logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", new Object[]{requestURL, remoteAddr});
                }
                return trace;
            }
        }
    }

    private String getLocalAddress(Channel channel) {
        return channel.getLocalAddress().toString().substring(1);
    }

    private String getRemoteAddress(Channel channel) {
        return channel.getRemoteAddress().toString().substring(1);
    }

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {

        if (trace.canSampled()) {
            org.jboss.netty.channel.MessageEvent e = getMessageEvent.invoke(target);

            if (e != null) {
                recordHttpParameter2(trace, e);
            }

            trace.recordApi(getMethodDescriptor());
        }
        trace.recordException(throwable);
        trace.markAfterTime();

    }

    private void recordHttpParameter2(RecordableTrace trace, MessageEvent e) {
        final Object message = e.getMessage();
        if (message instanceof org.jboss.netty.handler.codec.http.HttpRequest) {
            final org.jboss.netty.handler.codec.http.HttpRequest request = (org.jboss.netty.handler.codec.http.HttpRequest) message;
            HttpMethod reqMethod = request.getMethod();

            if (reqMethod.equals(HttpMethod.POST) || reqMethod.equals(HttpMethod.PUT) || reqMethod.equals(HttpMethod.DELETE)) {
                ChannelBuffer content = request.getContent();
                if (content.readable()) {
                    // FIXME request body type에 따른 처리가 필요함.
                    // HttpPostRequestDecoder
                    int contentSize = content.array().length;
                    if (contentSize > entityDumpSize) {
                        contentSize = entityDumpSize;
                    }
                    // netty 3.9 면 getHeader가 deprecated 되었다. 추후 없어질수는 있기는 한데 일단 동작에 문제가 없는듯 함.
                    final Charset charset = getCharset(request);
                    String bodyStr = content.toString(0, contentSize, charset);
                    if (bodyStr != null && bodyStr.length() > 0) {
                        trace.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, bodyStr);
                    }
                }
            } else if (reqMethod.equals(HttpMethod.GET)) {
                // String parameters = getRequestParameter_old(request, 64,
                // 512);
                String parameters = getRequestParameter(request, paramDumpSize);
                if (parameters != null && parameters.length() > 0) {
                    trace.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                }
            }
        }
    }

    // buffer index를 정확하게 계산하는 로직이나 before에서 데이터를 읽어야 함.
    private void recordHttpParameter(Trace trace, MessageEvent e) {
        final Object message = e.getMessage();
        if (message instanceof org.jboss.netty.handler.codec.http.HttpRequest) {
            final org.jboss.netty.handler.codec.http.HttpRequest request = (org.jboss.netty.handler.codec.http.HttpRequest) message;

            final HttpMethod reqMethod = request.getMethod();

            if (equalMethod(HttpMethod.POST, reqMethod) || equalMethod(HttpMethod.PUT, reqMethod) || equalMethod(HttpMethod.DELETE, reqMethod)) {
                ChannelBuffer content = request.getContent();
                int contentSize = content.readableBytes();
                if (contentSize > 0) {
                    // FIXME request body type에 따른 처리가 필요함.
                    // HttpPostRequestDecoder
                    if (contentSize > entityDumpSize) {
                        contentSize = entityDumpSize;
                    }
                    final Charset charset = getCharset(request);

                    // 해당 메소드의 경우 readerIndex를 after에서 읽을경우 다음으로 갈수 있음.
                    String bodyStr = content.toString(content.readerIndex(), contentSize, charset);
                    if (bodyStr != null && bodyStr.length() > 0) {
                        trace.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, bodyStr);
                    }
                }
            } else if (equalMethod(HttpMethod.GET, reqMethod)) {
                // String parameters = getRequestParameter_old(request, 64,
                // 512);
                String parameters = getRequestParameter(request, paramDumpSize);
                if (parameters != null && parameters.length() > 0) {
                    trace.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                }
            }
        }
    }

    private Charset getCharset(HttpRequest request) {
        return CharsetUtil.UTF_8;
        // http 스펙상으로는 아래가 맞긴한데. 강제 설정 변경수가 있어서. 아래 처럼할 경우 문제가 생길수 있는것 같음.
        // 코드에 utf8로 박혀 있으면 그냥 UTF-8로 하는게 더 안전할듯함.
//        // netty 3.9 면 getHeader가 deprecated 되었다. 추후 없어질수는 있기는 한데 일단 동작에 문제가 없는듯 함.
//        final String contentTypeValue = request.getHeader("Content-Type");
//        // charset.forName을 부르는게 그렇게 좋지는 않은것 같음.
//        return Charset.forName(HttpUtils.parseContentTypeCharset(contentTypeValue, DEFAULT_CHARSET));
    }


    private boolean equalMethod(HttpMethod thisMethod, HttpMethod thatMethod) {
        if (thatMethod == null) {
            return false;
        }
        return thisMethod.getName().equals(thatMethod.getName());
    }

    /**
     * request header에 sampling 정보가 포함되어있는지 확인.
     *
     * @param request
     * @return
     */
    private boolean isSamplingEnabled(final org.jboss.netty.handler.codec.http.HttpRequest request) {
        String samplingFlag = request.getHeader(Header.HTTP_SAMPLED.toString());
        if (isDebug) {
            logger.debug("SamplingFlag:{}", samplingFlag);
        }
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    /**
     * request header에서 traceid를 뽑아낸다.
     *
     * @param request
     * @return
     */
    private TraceId populateTraceIdFromRequest(final org.jboss.netty.handler.codec.http.HttpRequest request) {
        String transactionId = request.getHeader(Header.HTTP_TRACE_ID.toString());
        if (transactionId != null) {

            long parentSpanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL);
            long spanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_SPAN_ID.toString()), SpanId.NULL);
            short flags = NumberUtils.parseShort(request.getHeader(Header.HTTP_FLAGS.toString()), (short) 0);

            TraceId id = getTraceContext().createTraceId(transactionId, parentSpanID, spanID, flags);
            if (isDebug) {
                logger.debug("TraceID exist. continue trace. {}", id);
            }
            return id;
        } else {
            return null;
        }
    }

    /**
     * request header에서 parent application name 추출
     *
     * @param request
     * @return
     */
    private String populateParentApplicationNameFromRequest(final org.jboss.netty.handler.codec.http.HttpRequest request) {
        return request.getHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString());
    }

    /**
     * request header에서 parent application type 추출
     *
     * @param request
     * @return
     */
    private short populateParentApplicationTypeFromRequest(final org.jboss.netty.handler.codec.http.HttpRequest request) {
        String type = request.getHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString());
        if (type != null) {
            return Short.valueOf(type);
        }
        return ServiceType.UNDEFINED.getCode();
    }

    /**
     * request uri에서 query string 추출 (복잡한 처리 없이 그냥 ? 뒤로 붙어있는 문자열만 추출.)
     *
     * @param request
     * @param totalLimit
     * @return
     */
    private String getRequestParameter(final org.jboss.netty.handler.codec.http.HttpRequest request, int totalLimit) {
        String uri = request.getUri();

        if (uri == null || uri.length() < 2) {
            return null;
        }

        int pos = uri.indexOf("?") + 1;

        if (pos > 1 && pos < uri.length()) {
            return StringUtils.drop(uri.substring(pos), totalLimit);
        } else {
            return null;
        }
    }

    /**
     * request uri에서 querystring 추출
     *
     * @param request
     * @param eachLimit
     * @param totalLimit
     * @return
     */
    @Deprecated
    private String getRequestParameter_old(final org.jboss.netty.handler.codec.http.HttpRequest request, int eachLimit, int totalLimit) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
        Map<String, List<String>> queries = queryStringDecoder.getParameters();

        if (queries.isEmpty()) {
            return null;
        }

        StringBuilder params = new StringBuilder(64);
        Iterator<Entry<String, List<String>>> entryIterator = queries.entrySet().iterator();

        while (entryIterator.hasNext()) {
            Entry<String, List<String>> entry = entryIterator.next();

            params.append(entry.getKey());
            params.append("=");

            List<String> values = entry.getValue();

            if (values.size() > 1) {
                Iterator<String> valueIterator = values.iterator();
                while (valueIterator.hasNext()) {
                    params.append(StringUtils.drop(valueIterator.next(), eachLimit));
                    if (valueIterator.hasNext()) {
                        params.append(",");
                    }
                }
            } else {
                params.append(StringUtils.drop(values.get(0), eachLimit));
            }

            if (params.length() > totalLimit) {
                if (entryIterator.hasNext()) {
                    params.append("...");
                }
                break;
            }

            if (entryIterator.hasNext()) {
                params.append("&");
            }
        }

        return params.toString();
    }


    @Override
    public void setTraceContext(TraceContext traceContext) {
        super.setTraceContext(traceContext);

        final ProfilerConfig profilerConfig = traceContext.getProfilerConfig();

        this.paramDumpSize = profilerConfig.getLineGameNettyParamDumpSize();
        this.entityDumpSize = profilerConfig.getLineGameNettyEntityDumpSize();
    }

}
