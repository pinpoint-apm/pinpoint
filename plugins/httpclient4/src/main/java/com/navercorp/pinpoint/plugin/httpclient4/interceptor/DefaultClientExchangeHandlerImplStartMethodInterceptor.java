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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.common.Charsets;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HTTP;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.pair.NameIntValuePair;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.FixedByteArrayOutputStream;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.bootstrap.util.SimpleSampler;
import com.navercorp.pinpoint.bootstrap.util.SimpleSamplerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4Constants;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4PluginConfig;
import com.navercorp.pinpoint.plugin.httpclient4.RequestProducerGetter;
import com.navercorp.pinpoint.plugin.httpclient4.ResultFutureGetter;

/**
 * 
 * @author minwoo.jung
 * @author jaehong.kim
 *
 */
public class DefaultClientExchangeHandlerImplStartMethodInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;

    private final boolean param;
    protected final boolean cookie;
    protected final DumpType cookieDumpType;
    protected SimpleSampler cookieSampler;

    protected final boolean entity;
    protected final DumpType entityDumpType;
    protected SimpleSampler entitySampler;

    protected boolean statusCode;

    public DefaultClientExchangeHandlerImplStartMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;

        final HttpClient4PluginConfig config = new HttpClient4PluginConfig(traceContext.getProfilerConfig());
        this.param = config.isParam();
        this.cookie = config.isCookie();
        this.cookieDumpType = config.getCookieDumpType();
        if (cookie) {
            this.cookieSampler = SimpleSamplerFactory.createSampler(cookie, config.getCookieSamplingRate());
        }

        this.entity = config.isEntity();
        this.entityDumpType = config.getEntityDumpType();
        if (entity) {
            this.entitySampler = SimpleSamplerFactory.createSampler(entity, config.getEntitySamplingRate());
        }
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, "", methodDescriptor.getMethodName(), "", args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        final HttpRequest httpRequest = getHttpRequest(target);
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

        SpanEventRecorder recorder = trace.traceBlockBegin();

        // set remote trace
        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordServiceType(HttpClient4Constants.HTTP_CLIENT_4);

        if (httpRequest != null) {
            httpRequest.setHeader(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
            httpRequest.setHeader(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));

            httpRequest.setHeader(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));

            httpRequest.setHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
            httpRequest.setHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
            httpRequest.setHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));
            final NameIntValuePair<String> host = getHost(target);
            if (host != null) {
                final String endpoint = getEndpoint(host.getName(), host.getValue());
                logger.debug("Get host {}", endpoint);
                httpRequest.setHeader(Header.HTTP_HOST.toString(), endpoint);
            }
        }

        try {
            if (isAsynchronousInvocation(target, args)) {
                // set asynchronous trace
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();

                // check type isAsynchronousInvocation()
                ((AsyncContextAccessor)((ResultFutureGetter)target)._$PINPOINT$_getResultFuture())._$PINPOINT$_setAsyncContext(asyncContext);
                if (isDebug) {
                    logger.debug("Set AsyncContext {}", asyncContext);
                }
            }
        } catch (Throwable t) {
            logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
        }
    }

    private HttpRequest getHttpRequest(final Object target) {
        try {
            if (!(target instanceof RequestProducerGetter)) {
                return null;
            }

            final HttpAsyncRequestProducer requestProducer = ((RequestProducerGetter)target)._$PINPOINT$_getRequestProducer();
            return requestProducer.generateRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAsynchronousInvocation(final Object target, final Object[] args) {
        if (!(target instanceof ResultFutureGetter)) {
            logger.debug("Invalid target object. Need field accessor({}).", HttpClient4Constants.FIELD_RESULT_FUTURE);
            return false;
        }
        
        BasicFuture<?> future = ((ResultFutureGetter)target)._$PINPOINT$_getResultFuture();

        if (future == null) {
            logger.debug("Invalid target object. field is null({}).", HttpClient4Constants.FIELD_RESULT_FUTURE);
            return false;
        }

        if (!(future instanceof AsyncContextAccessor)) {
            logger.debug("Invalid resultFuture field object. Need metadata accessor({}).", HttpClient4Constants.METADATA_ASYNC_CONTEXT);
            return false;
        }

        return true;
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
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            final HttpRequest httpRequest = getHttpRequest(target);
            if (httpRequest != null) {
                // Accessing httpRequest here not BEFORE() because it can cause side effect.
                if(httpRequest.getRequestLine() != null) {
                    final String httpUrl = InterceptorUtils.getHttpUrl(httpRequest.getRequestLine().getUri(), param);
                    recorder.recordAttribute(AnnotationKey.HTTP_URL, httpUrl);
                }
                final NameIntValuePair<String> host = getHost(target);
                if (host != null) {
                    final String endpoint = getEndpoint(host.getName(), host.getValue());
                    recorder.recordDestinationId(endpoint);
                }
                recordHttpRequest(recorder, httpRequest, throwable);
            }
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }

    private NameIntValuePair<String> getHost(final Object target) {
        if (!(target instanceof RequestProducerGetter)) {
            return null;
        }

        final HttpAsyncRequestProducer producer = ((RequestProducerGetter)target)._$PINPOINT$_getRequestProducer();
        final HttpHost httpHost = producer.getTarget();
        if(httpHost != null) {
            return new NameIntValuePair<String>(httpHost.getHostName(), httpHost.getPort());
        } else {
            return null;
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

    private void recordHttpRequest(SpanEventRecorder recorder, HttpRequest httpRequest, Throwable throwable) {
        final boolean isException = InterceptorUtils.isThrowable(throwable);
        if (cookie) {
            if (DumpType.ALWAYS == cookieDumpType) {
                recordCookie(httpRequest, recorder);
            } else if (DumpType.EXCEPTION == cookieDumpType && isException) {
                recordCookie(httpRequest, recorder);
            }
        }
        if (entity) {
            if (DumpType.ALWAYS == entityDumpType) {
                recordEntity(httpRequest, recorder);
            } else if (DumpType.EXCEPTION == entityDumpType && isException) {
                recordEntity(httpRequest, recorder);
            }
        }
    }

    protected void recordCookie(HttpMessage httpMessage, SpanEventRecorder recorder) {
        org.apache.http.Header[] cookies = httpMessage.getHeaders("Cookie");
        for (org.apache.http.Header header : cookies) {
            final String value = header.getValue();
            if (value != null && !value.isEmpty()) {
                if (cookieSampler.isSampling()) {
                    recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.abbreviate(value, 1024));
                }

                // Can a cookie have 2 or more values?
                // PMD complains if we use break here
                return;
            }
        }
    }

    protected void recordEntity(HttpMessage httpMessage, SpanEventRecorder recorder) {
        if (httpMessage instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) httpMessage;
            try {
                final HttpEntity entity = entityRequest.getEntity();
                if (entity != null && entity.isRepeatable() && entity.getContentLength() > 0) {
                    if (entitySampler.isSampling()) {
                        final String entityString = entityUtilsToString(entity, Charsets.UTF_8_NAME, 1024);
                        recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, entityString);
                    }
                }
            } catch (Exception e) {
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
    public static String entityUtilsToString(final HttpEntity entity, final String defaultCharset, int maxLength) throws Exception {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity must not be null");
        }
        if (entity.getContentLength() > Integer.MAX_VALUE) {
            return "HTTP entity is too large to be buffered in memory length:" + entity.getContentLength();
        }
        if (entity.getContentType().getValue().startsWith("multipart/form-data")) {
            return "content type is multipart/form-data. content length:" + entity.getContentLength();
        }
        
        String charset = getContentCharSet(entity);
        
        if (charset == null) {
            charset = defaultCharset;
        }
        if (charset == null) {
            charset = HTTP.DEFAULT_CONTENT_CHARSET;
        }
        
        FixedByteArrayOutputStream outStream = new FixedByteArrayOutputStream(maxLength);
        entity.writeTo(outStream);
        
        String entityValue = outStream.toString(charset);
        
        if (entity.getContentLength() > maxLength) {
            StringBuilder sb = new StringBuilder();
            sb.append(entityValue);
            sb.append("HTTP entity large length: ");
            sb.append(entity.getContentLength());
            sb.append(" )");
            return sb.toString();
        }
        
        return entityValue;
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
            throw new IllegalArgumentException("HTTP entity must not be null");
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
