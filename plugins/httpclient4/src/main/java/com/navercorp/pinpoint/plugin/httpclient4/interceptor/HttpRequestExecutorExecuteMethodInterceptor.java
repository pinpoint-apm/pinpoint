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

package com.navercorp.pinpoint.plugin.httpclient4.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.protocol.HTTP;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.AttachmentFactory;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.interceptor.http.HttpCallContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.pair.NameIntValuePair;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.bootstrap.util.SimpleSampler;
import com.navercorp.pinpoint.bootstrap.util.SimpleSamplerFactory;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4Constants;

/**
 * @author minwoo.jung
 * @author jaehong.kim
 */
@Group(value = HttpClient4Constants.HTTP_CLIENT4_SCOPE, executionPolicy = ExecutionPolicy.ALWAYS)
public class HttpRequestExecutorExecuteMethodInterceptor implements SimpleAroundInterceptor, HttpClient4Constants {
    private static final int HTTP_REQUEST_INDEX = 1;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;

    private final boolean cookie;
    private final DumpType cookieDumpType;
    private final SimpleSampler cookieSampler;

    private final boolean entity;
    private final DumpType entityDumpType;
    private final SimpleSampler entitySampler;

    private final boolean statusCode;
    private final InterceptorGroup interceptorGroup;

    private final boolean io;

    public HttpRequestExecutorExecuteMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorGroup interceptorGroup) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.interceptorGroup = interceptorGroup;

        final ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
        this.cookie = profilerConfig.isApacheHttpClient4ProfileCookie();
        this.cookieDumpType = profilerConfig.getApacheHttpClient4ProfileCookieDumpType();
        if (cookie) {
            this.cookieSampler = SimpleSamplerFactory.createSampler(cookie, profilerConfig.getApacheHttpClient4ProfileCookieSamplingRate());
        } else {
            this.cookieSampler = null;
        }

        this.entity = profilerConfig.isApacheHttpClient4ProfileEntity();
        this.entityDumpType = profilerConfig.getApacheHttpClient4ProfileEntityDumpType();
        if (entity) {
            this.entitySampler = SimpleSamplerFactory.createSampler(entity, profilerConfig.getApacheHttpClient4ProfileEntitySamplingRate());
        } else {
            this.entitySampler = null;
        }
        this.statusCode = profilerConfig.isApacheHttpClient4ProfileStatusCode();
        this.io = profilerConfig.isApacheHttpClient4ProfileIo();
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

        final HttpRequest httpRequest = getHttpRequest(args);

        final boolean sampling = trace.canSampled();
        if (!sampling) {
            if (isDebug) {
                logger.debug("set Sampling flag=false");
            }
            if (httpRequest != null) {
                httpRequest.setHeader(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
            }
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordServiceType(HttpClient4Constants.HTTP_CLIENT_4);

        if (httpRequest != null) {
            httpRequest.setHeader(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
            httpRequest.setHeader(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));

            httpRequest.setHeader(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));

            httpRequest.setHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
            httpRequest.setHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
            httpRequest.setHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));
            final NameIntValuePair<String> host = getHost();
            if (host != null) {
                logger.debug("Get host {}", host.getName());
                httpRequest.setHeader(Header.HTTP_HOST.toString(), host.getName());
            }
        }

        InterceptorGroupInvocation invocation = interceptorGroup.getCurrentInvocation();
        if (invocation != null) {
            HttpCallContext callContext = (HttpCallContext) invocation.getOrCreateAttachment(new AttachmentFactory() {
                @Override
                public Object createAttachment() {
                    return new HttpCallContext();
                }
            });
            invocation.setAttachment(callContext);
        }
    }

    private HttpRequest getHttpRequest(Object[] args) {
        if (args != null && args.length >= 1 && args[0] != null && args[0] instanceof HttpRequest) {
            return (HttpRequest) args[0];
        }

        return null;
    }

    private NameIntValuePair<String> getHost() {
        InterceptorGroupInvocation transaction = interceptorGroup.getCurrentInvocation();
        if (transaction != null && transaction.getAttachment() != null) {
            HttpCallContext callContext = (HttpCallContext) transaction.getAttachment();
            return new NameIntValuePair<String>(callContext.getHost(), callContext.getPort());
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
            final HttpRequest httpRequest = getHttpRequest(args);
            if (httpRequest != null) {
                // Accessing httpRequest here not BEFORE() becuase it can cause side effect.
                recorder.recordAttribute(AnnotationKey.HTTP_URL, httpRequest.getRequestLine().getUri());
                final NameIntValuePair<String> host = getHost();
                if (host != null) {
                    int port = host.getValue();
                    String endpoint = getEndpoint(host.getName(), port);
                    recorder.recordDestinationId(endpoint);
                }

                recordHttpRequest(trace, httpRequest, throwable);
            }

            if (statusCode) {
                final Integer statusCodeValue = getStatusCode(result);
                if (statusCodeValue != null) {
                    recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, statusCodeValue);
                }
            }

            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);

            InterceptorGroupInvocation invocation = interceptorGroup.getCurrentInvocation();
            if (invocation != null && invocation.getAttachment() != null) {
                final HttpCallContext callContext = (HttpCallContext) invocation.getAttachment();
                logger.debug("Check call context {}", callContext);
                if (io) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("write=").append(callContext.getWriteElapsedTime());
                    if (callContext.isWriteFail()) {
                        sb.append("(fail)");
                    }
                    sb.append(", read=").append(callContext.getReadElapsedTime());
                    if (callContext.isReadFail()) {
                        sb.append("(fail)");
                    }
                    recorder.recordAttribute(AnnotationKey.HTTP_IO, sb.toString());
                }
                // clear
                invocation.removeAttachment();
            }

        } finally {
            trace.traceBlockEnd();
        }
    }

    private Integer getStatusCode(Object result) {
        return getStatusCodeFromResponse(result);
    }

    Integer getStatusCodeFromResponse(Object result) {
        if (result instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) result;

            final StatusLine statusLine = response.getStatusLine();
            if (statusLine != null) {
                return statusLine.getStatusCode();
            } else {
                return null;
            }
        }
        return null;
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

    private void recordHttpRequest(Trace trace, HttpRequest httpRequest, Throwable throwable) {
        final boolean isException = InterceptorUtils.isThrowable(throwable);
        if (cookie) {
            if (DumpType.ALWAYS == cookieDumpType) {
                recordCookie(httpRequest, trace);
            } else if (DumpType.EXCEPTION == cookieDumpType && isException) {
                recordCookie(httpRequest, trace);
            }
        }
        if (entity) {
            if (DumpType.ALWAYS == entityDumpType) {
                recordEntity(httpRequest, trace);
            } else if (DumpType.EXCEPTION == entityDumpType && isException) {
                recordEntity(httpRequest, trace);
            }
        }
    }

    protected void recordCookie(HttpMessage httpMessage, Trace trace) {
        org.apache.http.Header[] cookies = httpMessage.getHeaders("Cookie");
        for (org.apache.http.Header header : cookies) {
            final String value = header.getValue();
            if (value != null && !value.isEmpty()) {
                if (cookieSampler.isSampling()) {
                    final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                    recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.drop(value, 1024));
                }

                // Can a cookie have 2 or more values?
                // PMD complains if we use break here
                return;
            }
        }
    }

    protected void recordEntity(HttpMessage httpMessage, Trace trace) {
        if (httpMessage instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) httpMessage;
            try {
                final HttpEntity entity = entityRequest.getEntity();
                if (entity != null && entity.isRepeatable() && entity.getContentLength() > 0) {
                    if (entitySampler.isSampling()) {
                        final String entityString = entityUtilsToString(entity, "UTF8", 1024);
                        final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                        recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, StringUtils.drop(entityString, 1024));
                    }
                }
            } catch (IOException e) {
                logger.debug("HttpEntityEnclosingRequest entity record fail. Caused:{}", e.getMessage(), e);
            }
        }
    }

    /**
     * copy: EntityUtils Get the entity content as a String, using the provided default character set if none is found in the entity. If defaultCharset is null, the default "ISO-8859-1" is used.
     *
     * @param entity
     *            must not be null
     * @param defaultCharset
     *            character set to be applied if none found in the entity
     * @return the entity content as a String. May be null if {@link HttpEntity#getContent()} is null.
     * @throws ParseException
     *             if header elements cannot be parsed
     * @throws IllegalArgumentException
     *             if entity is null or if content length > Integer.MAX_VALUE
     * @throws IOException
     *             if an error occurs reading the input stream
     */
    @SuppressWarnings("deprecation")
    public static String entityUtilsToString(final HttpEntity entity, final String defaultCharset, int maxLength) throws IOException, ParseException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        final InputStream instream = entity.getContent();
        if (instream == null) {
            return null;
        }
        try {
            if (entity.getContentLength() > Integer.MAX_VALUE) {
                return "HTTP entity too large to be buffered in memory length:" + entity.getContentLength();
            }
            String charset = getContentCharSet(entity);
            if (charset == null) {
                charset = defaultCharset;
            }
            if (charset == null) {
                charset = HTTP.DEFAULT_CONTENT_CHARSET;
            }
            Reader reader = new InputStreamReader(instream, charset);
            final StringBuilder buffer = new StringBuilder(maxLength * 2);
            char[] tmp = new char[1024];
            int l;
            while ((l = reader.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
                if (buffer.length() >= maxLength) {
                    break;
                }
            }
            return buffer.toString();
        } finally {
            instream.close();
        }
    }

    /**
     * copy: EntityUtils Obtains character set of the entity, if known.
     *
     * @param entity
     *            must not be null
     * @return the character set, or null if not found
     * @throws ParseException
     *             if header elements cannot be parsed
     * @throws IllegalArgumentException
     *             if entity is null
     */
    public static String getContentCharSet(final HttpEntity entity) throws ParseException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        String charset = null;
        if (entity.getContentType() != null) {
            HeaderElement values[] = entity.getContentType().getElements();
            if (values.length > 0) {
                NameValuePair param = values[0].getParameterByName("charset");
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }
        return charset;
    }
}