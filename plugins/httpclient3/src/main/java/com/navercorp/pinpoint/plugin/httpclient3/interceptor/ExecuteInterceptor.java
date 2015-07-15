/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.httpclient3.interceptor;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpConstants;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.bootstrap.util.SimpleSampler;
import com.navercorp.pinpoint.bootstrap.util.SimpleSamplerFactory;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3Constants;

/**
 * @author Minwoo Jung
 */
@Group(HttpClient3Constants.HTTP_CLIENT3_SCOPE)
public class ExecuteInterceptor implements SimpleAroundInterceptor, HttpClient3Constants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final int MAX_READ_SIZE = 1024;
    private static final Map<Integer, Integer> httpMethod_Index;
    static {
        httpMethod_Index = new HashMap<Integer, Integer>();
        httpMethod_Index.put(1, 0);
        httpMethod_Index.put(2, 1);
        httpMethod_Index.put(3, 1);
    }

    private TraceContext traceContext;
    private MethodDescriptor descriptor;

    protected boolean cookie;
    protected DumpType cookieDumpType;
    protected SimpleSampler cookieSampler;

    protected boolean entity;
    protected DumpType entityDumpType;
    protected SimpleSampler entitySampler;

    public ExecuteInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;
        
        final ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
        this.cookie = profilerConfig.isApacheHttpClient3ProfileCookie();
        this.cookieDumpType = profilerConfig.getApacheHttpClient3ProfileCookieDumpType();

        if (cookie) {
            this.cookieSampler = SimpleSamplerFactory.createSampler(cookie, profilerConfig.getApacheHttpClient3ProfileCookieSamplingRate());
        }

        this.entity = profilerConfig.isApacheHttpClient3ProfileEntity();
        this.entityDumpType = profilerConfig.getApacheHttpClient3ProfileEntityDumpType();

        if (entity) {
            this.entitySampler = SimpleSamplerFactory.createSampler(entity, profilerConfig.getApacheHttpClient3ProfileEntitySamplingRate());
        }
    }
    
    
    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        final HttpMethod httpMethod = getHttpMethod(args);
        final boolean sampling = trace.canSampled();
        if (!sampling) {
            if (isDebug) {
                logger.debug("set Sampling flag=false");
            }
            if (httpMethod != null) {
                httpMethod.setRequestHeader(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
            }

            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordServiceType(ServiceType.HTTP_CLIENT);

        if (httpMethod != null) {
            httpMethod.setRequestHeader(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
            httpMethod.setRequestHeader(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
            httpMethod.setRequestHeader(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));
            httpMethod.setRequestHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
            httpMethod.setRequestHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
            httpMethod.setRequestHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));
            final String host = getHost(httpMethod);
            if (host != null) {
                httpMethod.setRequestHeader(Header.HTTP_HOST.toString(), host);
            }
        }
    }

    private String getHost(HttpMethod httpMethod) {
        try {
            return httpMethod.getURI().getHost();
        } catch (URIException e) {
            logger.error("Fail get URI", e);
        }
        
        return null;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            HttpMethod httpMethod = getHttpMethod(args);
            if (httpMethod != null) {
                try {
                    final URI uri = httpMethod.getURI();
                    String uriString = uri.getURI();
                    recorder.recordAttribute(AnnotationKey.HTTP_URL, uriString);
                    recorder.recordDestinationId(getEndpoint(uri.getHost(), uri.getPort()));
                } catch (URIException e) {
                    logger.error("Fail get URI", e);
                }

                recordRequest(trace, httpMethod, throwable);
            }

            if (result != null) {
                recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, result);
            }

            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }

    private void recordRequest(Trace trace, HttpMethod httpMethod, Throwable throwable) {
        final boolean isException = InterceptorUtils.isThrowable(throwable);

        if (cookie) {
            if (DumpType.ALWAYS == cookieDumpType) {
                recordCookie(httpMethod, trace);
            } else if (DumpType.EXCEPTION == cookieDumpType && isException) {
                recordCookie(httpMethod, trace);
            }
        }
        if (entity) {
            if (DumpType.ALWAYS == entityDumpType) {
                recordEntity(httpMethod, trace);
            } else if (DumpType.EXCEPTION == entityDumpType && isException) {
                recordEntity(httpMethod, trace);
            }
        }
    }

    private void recordEntity(HttpMethod httpMethod, Trace trace) {
        if (httpMethod instanceof EntityEnclosingMethod) {
            final EntityEnclosingMethod entityEnclosingMethod = (EntityEnclosingMethod) httpMethod;
            final RequestEntity entity = entityEnclosingMethod.getRequestEntity();

            if (entity != null && entity.isRepeatable() && entity.getContentLength() > 0) {
                if (entitySampler.isSampling()) {
                    try {
                        String entityValue;
                        String charSet = entityEnclosingMethod.getRequestCharSet();

                        if (charSet == null || charSet.isEmpty()) {
                            charSet = HttpConstants.DEFAULT_CONTENT_CHARSET;
                        }
                        if (entity instanceof ByteArrayRequestEntity) {
                            entityValue = readByteArray((ByteArrayRequestEntity) entity, charSet);
                        } else if (entity instanceof StringRequestEntity) {
                            entityValue = readString((StringRequestEntity) entity);
                        } else {
                            entityValue = entity.getClass() + " (ContentType:" + entity.getContentType() + ")";
                        }

                        final SpanEventRecorder recorder = trace.currentSpanEventRecorder();                       
                        recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, entityValue);
                    } catch (Exception e) {
                        logger.debug("HttpEntityEnclosingRequest entity record fail. Caused:{}", e.getMessage(), e);
                    }
                }
            }
        }

    }

    private String readString(StringRequestEntity entity) {
        return StringUtils.drop(entity.getContent(), MAX_READ_SIZE);
    }

    private String readByteArray(ByteArrayRequestEntity entity, String charSet) throws UnsupportedEncodingException {
        if (entity.getContent() == null) {
            return "";
        }

        final int length = entity.getContent().length > MAX_READ_SIZE ? MAX_READ_SIZE : entity.getContent().length;

        if (length <= 0) {
            return "";
        }

        return new String(entity.getContent(), 0, length, charSet);

    }

    private void recordCookie(HttpMethod httpMethod, Trace trace) {
        org.apache.commons.httpclient.Header cookie = httpMethod.getRequestHeader("Cookie");
        if (cookie == null) {
            return;
        }

        final String value = cookie.getValue();

        if (value != null && !value.isEmpty()) {
            if (cookieSampler.isSampling()) {
                final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.drop(value, MAX_READ_SIZE));
            }
        }
    }

    private String getEndpoint(String host, int port) {
        if (host == null) {
            return "UnknownHttpClient";
        }
        if (port < 0) {
            return host;
        }
        StringBuilder sb = new StringBuilder(host.length() + 8);
        sb.append(host);
        sb.append(':');
        sb.append(port);
        return sb.toString();
    }

    private HttpMethod getHttpMethod(Object[] args) {
        Integer httpMethodIndex = httpMethod_Index.get(args.length);

        if (httpMethodIndex == null) {
            return null;
        }

        if (args[httpMethodIndex] != null && args[httpMethodIndex] instanceof HttpMethod) {
            return (HttpMethod) args[httpMethodIndex];
        }

        return null;
    }
}
