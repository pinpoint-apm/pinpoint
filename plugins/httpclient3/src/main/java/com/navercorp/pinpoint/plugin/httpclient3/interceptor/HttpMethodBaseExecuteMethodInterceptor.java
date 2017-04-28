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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConstants;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.FixedByteArrayOutputStream;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.bootstrap.util.SimpleSampler;
import com.navercorp.pinpoint.bootstrap.util.SimpleSamplerFactory;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3CallContext;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3CallContextFactory;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3Constants;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3PluginConfig;

/**
 * @author Minwoo Jung
 * @author jaehong.kim
 */
public class HttpMethodBaseExecuteMethodInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private static final int MAX_READ_SIZE = 1024;
    private static final Map<Integer, Integer> httpMethod_Index;

    static {
        httpMethod_Index = new HashMap<Integer, Integer>();
        httpMethod_Index.put(1, 0);
        httpMethod_Index.put(2, 1);
        httpMethod_Index.put(3, 1);
    }

    private TraceContext traceContext;
    private MethodDescriptor descriptor;
    private InterceptorScope interceptorScope;

    private boolean param;
    private boolean cookie;
    private DumpType cookieDumpType;
    private SimpleSampler cookieSampler;

    private boolean entity;
    private DumpType entityDumpType;
    private SimpleSampler entitySampler;

    private boolean io;

    public HttpMethodBaseExecuteMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;
        this.interceptorScope = interceptorScope;

        final HttpClient3PluginConfig config = new HttpClient3PluginConfig(traceContext.getProfilerConfig());
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

        this.io = config.isIo();
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

        if (!trace.canSampled()) {
            // set http header.
            setHttpSampledHeader(target);
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        // generate next trace id.
        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordServiceType(HttpClient3Constants.HTTP_CLIENT_3);
        // set http header for trace.
        setHttpTraceHeader(target, args, nextId);

        // init attachment for io(read/write).
        initAttachment();
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
            if (target instanceof HttpMethod) {
                HttpMethod httpMethod = (HttpMethod) target;
                recordDestination(trace, httpMethod, args);
                recordRequest(trace, httpMethod, throwable);
            }

            if (result != null) {
                recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, result);
            }

            recorder.recordApi(descriptor);
            recorder.recordException(throwable);

            final HttpClient3CallContext callContext = getAndCleanAttachment();
            if (callContext != null) {
                recordIo(recorder, callContext);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }


    private void setHttpSampledHeader(final Object target) {
        if (isDebug) {
            logger.debug("set Sampling flag=false");
        }
        if (target instanceof HttpMethod) {
            final HttpMethod httpMethod = (HttpMethod) target;
            httpMethod.setRequestHeader(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
        }
    }

    private void setHttpTraceHeader(final Object target, final Object[] args, TraceId nextId) {
        if (target instanceof HttpMethod) {
            final HttpMethod httpMethod = (HttpMethod) target;
            httpMethod.setRequestHeader(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
            httpMethod.setRequestHeader(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
            httpMethod.setRequestHeader(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));
            httpMethod.setRequestHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
            httpMethod.setRequestHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
            httpMethod.setRequestHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));
            final String host = getHost(httpMethod, args);
            if (host != null) {
                httpMethod.setRequestHeader(Header.HTTP_HOST.toString(), host);
            }
        }
    }

    private String getHost(HttpMethod httpMethod, Object[] args) {
        try {
            final URI url = httpMethod.getURI();
            if (url.isAbsoluteURI()) {
                return getEndpoint(url.getHost(), url.getPort());
            }

            if (isDebug) {
                logger.debug("URI is not absolute. {}", url.getURI());
            }

            // if not found schema, use httpConnection.
            final HttpConnection httpConnection = getHttpConnection(args);
            if (httpConnection != null) {
                final String host = httpConnection.getHost();
                int port = httpConnection.getPort();
                // if port is default port number.
                if (httpConnection.getProtocol() != null && port == httpConnection.getProtocol().getDefaultPort()) {
                    port = -1;
                }
                return getEndpoint(host, port);
            }
        } catch (URIException e) {
            // unexpected error, perhaps of user fault.
            logger.error("[HttpClient3] Fail get URI", e);
        }

        return null;
    }

    private void initAttachment() {
        InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
        if (invocation != null) {
            invocation.getOrCreateAttachment(HttpClient3CallContextFactory.HTTPCLIENT3_CONTEXT_FACTORY);
        }
    }

    private HttpClient3CallContext getAndCleanAttachment() {
        final InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
        if (invocation != null && invocation.getAttachment() != null && invocation.getAttachment() instanceof HttpClient3CallContext) {
            return (HttpClient3CallContext) invocation.removeAttachment();
        }

        return null;
    }

    private void recordDestination(final Trace trace, final HttpMethod httpMethod, final Object[] args) {
        final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        try {
            final URI uri = httpMethod.getURI();
            final HttpConnection httpConnection = getHttpConnection(args);
            // if uri have schema or not found HttpConnection argument.
            if (uri.isAbsoluteURI() || httpConnection == null) {
                recorder.recordAttribute(AnnotationKey.HTTP_URL, InterceptorUtils.getHttpUrl(uri.getURI(), param));
                recorder.recordDestinationId(getEndpoint(uri.getHost(), uri.getPort()));
                return;
            }

            if (isDebug) {
                logger.debug("URI is not absolute. {}", uri.getURI());
            }

            // use HttpConnection argument.
            final String host = httpConnection.getHost();
            int port = httpConnection.getPort();
            final StringBuilder httpUrl = new StringBuilder();
            final Protocol protocol = httpConnection.getProtocol();
            if (protocol != null) {
                httpUrl.append(protocol.getScheme()).append("://");
                httpUrl.append(httpConnection.getHost());
                // if port is default port number.
                if (httpConnection.getPort() == protocol.getDefaultPort()) {
                    port = -1;
                } else {
                    httpUrl.append(":").append(port);
                }
            }
            httpUrl.append(uri.getURI());
            recorder.recordAttribute(AnnotationKey.HTTP_URL, InterceptorUtils.getHttpUrl(httpUrl.toString(), param));
            recorder.recordDestinationId(getEndpoint(host, port));
        } catch (URIException e) {
            logger.error("Fail get URI", e);
            recorder.recordDestinationId("unknown");
        }
    }

    private void recordIo(SpanEventRecorder recorder, HttpClient3CallContext callContext) {
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
                        
                        if (entity instanceof ByteArrayRequestEntity || entity instanceof StringRequestEntity) {
                            entityValue = entityUtilsToString(entity, charSet);
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

    private String entityUtilsToString(RequestEntity entity, String charSet) throws Exception {
        FixedByteArrayOutputStream outStream = new FixedByteArrayOutputStream(MAX_READ_SIZE);
        entity.writeRequest(outStream);
        
        String entityValue = outStream.toString(charSet);
        
        if (entity.getContentLength() > MAX_READ_SIZE) {
            StringBuilder sb = new StringBuilder();
            sb.append(entityValue);
            sb.append(" (HTTP entity is large. length: ");
            sb.append(entity.getContentLength());
            sb.append(" )");
            return sb.toString();
        }
        
        return entityValue;
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
                recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.abbreviate(value, MAX_READ_SIZE));
            }
        }
    }

    private String getEndpoint(String host, int port) {
        if (host == null) {
            return "unknown";
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

    private HttpConnection getHttpConnection(final Object[] args) {
        if (args != null && args.length > 1 && args[1] instanceof HttpConnection) {
            return (HttpConnection) args[1];
        }

        return null;
    }
}