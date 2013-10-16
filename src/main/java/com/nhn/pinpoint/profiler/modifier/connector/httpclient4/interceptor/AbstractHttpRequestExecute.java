package com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.config.DumpType;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.context.Header;
import com.nhn.pinpoint.profiler.context.Trace;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.context.TraceId;
import com.nhn.pinpoint.profiler.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.pair.NameIntValuePair;
import com.nhn.pinpoint.profiler.sampler.util.SamplingFlagUtils;
import com.nhn.pinpoint.profiler.util.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 *
 */
public abstract class AbstractHttpRequestExecute implements TraceContextSupport, ByteCodeMethodDescriptorSupport, SimpleAroundInterceptor {

    protected PLogger logger;
    protected boolean isDebug;

    protected TraceContext traceContext;
    protected MethodDescriptor descriptor;

    protected boolean cookie;
    protected DumpType cookieDumpType;
    protected SimpleSampler cookieSampler;

    protected boolean entity;
    protected DumpType entityDumpType;
    protected SimpleSampler entitySampler;

    abstract NameIntValuePair<String> getHost(Object[] args);

    abstract HttpRequest getHttpRequest(Object[] args);

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        final HttpRequest httpRequest = getHttpRequest(args);

        final boolean sampling = trace.canSampled();
        if (!sampling) {
            if(isDebug) {
                logger.debug("set Sampling flag=false");
            }
            if (httpRequest != null) {
                httpRequest.addHeader(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
            }
            return;
        }

        trace.traceBlockBegin();
        trace.markBeforeTime();

        TraceId nextId = trace.getTraceId().getNextTraceId();
        trace.recordNextSpanId(nextId.getSpanId());
        trace.recordServiceType(ServiceType.HTTP_CLIENT);

        if (httpRequest != null) {
            httpRequest.addHeader(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
            httpRequest.addHeader(Header.HTTP_SPAN_ID.toString(), Integer.toString(nextId.getSpanId()));
            httpRequest.addHeader(Header.HTTP_PARENT_SPAN_ID.toString(), Integer.toString(nextId.getParentSpanId()));

            httpRequest.addHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
            httpRequest.addHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
            httpRequest.addHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));
        }
    }



    private String getEndpoint(String host, int port) {
        if (port < 0) {
            return host;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(host);
        sb.append(':');
        sb.append(port);
        return sb.toString();
    }

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            // result는 로깅하지 않는다.
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        try {
            final HttpRequest httpRequest = getHttpRequest(args);
            if (httpRequest != null) {
                // httpRequest에 뭔가 access하는 작업은 위험이 있으므로 after에서 작업한다.
                trace.recordAttribute(AnnotationKey.HTTP_URL, httpRequest.getRequestLine().getUri());
                final NameIntValuePair<String> host = getHost(args);
                if (host != null) {
                    int port = host.getValue();
                    String endpoint = getEndpoint(host.getName(), port);
                    trace.recordDestinationId(endpoint);
                }

                recordHttpRequest(trace, httpRequest, result);
            }
            trace.recordApi(descriptor);
            trace.recordException(result);

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }

    private void recordHttpRequest(Trace trace, HttpRequest httpRequest, Object result) {
        final boolean isException = InterceptorUtils.isThrowable(result);
        if (cookie) {
            if (DumpType.ALWAYS == cookieDumpType) {
                recordCookie(httpRequest, trace);
            } else if(DumpType.EXCEPTION == cookieDumpType && isException){
                recordCookie(httpRequest, trace);
            }
        }
        if (entity) {
            if (DumpType.ALWAYS == entityDumpType) {
                recordEntity(httpRequest, trace);
            } else if(DumpType.EXCEPTION == entityDumpType && isException) {
                recordEntity(httpRequest, trace);
            }
        }
    }

    protected void recordCookie(HttpMessage httpMessage, Trace trace) {
        org.apache.http.Header[] cookies = httpMessage.getHeaders("Cookie");
        for (org.apache.http.Header header: cookies) {
            final String value = header.getValue();
            if (value != null && !value.isEmpty()) {
                if (cookieSampler.isSampling()) {
                    trace.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.drop(value, 1024));
                }
            }
            // Cookie값이 2개 이상일수가 있나?
            break;
        }
    }

    protected void recordEntity(HttpMessage httpMessage, Trace trace) {
        if (httpMessage instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) httpMessage;
            try {
                final HttpEntity entity = entityRequest.getEntity();
                if (entity != null && entity.isRepeatable() && entity.getContentLength() > 0) {
                    if (entitySampler.isSampling()) {
                        // entity utils의 toString시 일정 length까지만 데이터를 읽도록하는 기능이 필요함.
                        String entityString = EntityUtils.toString(entity, "UTF8");
                        trace.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, StringUtils.drop(entityString, 1024));
                    }
                }
            } catch (IOException e) {
                logger.debug("HttpEntityEnclosingRequest entity record fail. Caused:{}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;

        final ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
        this.cookie = profilerConfig.isApacheHttpClient4ProfileCookie();
        this.cookieDumpType = profilerConfig.getApacheHttpClient4ProfileCookieDumpType();
        if (cookie){
            this.cookieSampler = SimpleSamplerFactory.createSampler(cookie, profilerConfig.getApacheHttpClient4ProfileCookieSamplingRate());
        }

        this.entity = profilerConfig.isApacheHttpClient4ProfileEntity();
        this.entityDumpType = profilerConfig.getApacheHttpClient4ProfileEntityDumpType();
        if (entity) {
            this.entitySampler = SimpleSamplerFactory.createSampler(entity, profilerConfig.getApacheHttpClient4ProfileEntitySamplingRate());
        }
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        traceContext.cacheApi(descriptor);
    }
}
