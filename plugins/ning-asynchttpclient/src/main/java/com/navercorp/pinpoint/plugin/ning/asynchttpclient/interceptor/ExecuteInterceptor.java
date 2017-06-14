/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.ning.asynchttpclient.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.bootstrap.util.SimpleSampler;
import com.navercorp.pinpoint.bootstrap.util.SimpleSamplerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.EndPointUtils;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.NingAsyncHttpClientPlugin;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.NingAsyncHttpClientPluginConfig;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.Param;
import org.asynchttpclient.Request;
import org.asynchttpclient.cookie.Cookie;
import org.asynchttpclient.request.body.multipart.ByteArrayPart;
import org.asynchttpclient.request.body.multipart.FilePart;
import org.asynchttpclient.request.body.multipart.Part;
import org.asynchttpclient.request.body.multipart.StringPart;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ExecuteInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final NingAsyncHttpClientPluginConfig config;

    private final SimpleSampler cookieSampler;
    private final SimpleSampler entitySampler;
    private final SimpleSampler paramSampler;

    public ExecuteInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.config = new NingAsyncHttpClientPluginConfig(traceContext.getProfilerConfig());

        this.cookieSampler = config.isProfileCookie() ? SimpleSamplerFactory.createSampler(true, config.getCookieSamplingRate()) : null;
        this.entitySampler = config.isProfileEntity() ? SimpleSamplerFactory.createSampler(true, config.getEntitySamplingRate()) : null;
        this.paramSampler = config.isProfileParam() ? SimpleSamplerFactory.createSampler(true, config.getParamSamplingRate()) : null;
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

        if (!validate(args)) {
            return;
        }

        final Request httpRequest = (Request) args[0];
        final boolean sampling = trace.canSampled();
        if (!sampling) {
            if (isDebug) {
                logger.debug("set Sampling flag=false");
            }
            if (httpRequest != null) {
                final HttpHeaders httpRequestHeaders = httpRequest.getHeaders();
                httpRequestHeaders.add(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
            }
            return;
        }

        trace.traceBlockBegin();
        final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordServiceType(NingAsyncHttpClientPlugin.ASYNC_HTTP_CLIENT);

        if (httpRequest != null) {
            final HttpHeaders httpRequestHeaders = httpRequest.getHeaders();
            httpRequestHeaders.add(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
            httpRequestHeaders.add(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
            httpRequestHeaders.add(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));
            httpRequestHeaders.add(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
            httpRequestHeaders.add(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
            httpRequestHeaders.add(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));
            final String hostString = EndPointUtils.getEndPoint(httpRequest.getUrl(), null);
            if (hostString != null) {
                httpRequestHeaders.add(Header.HTTP_HOST.toString(), hostString);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            // Do not log result
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (!validate(args)) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            final Request httpRequest = (Request) args[0];
            if (httpRequest != null) {
                // Accessing httpRequest here not BEFORE() because it can cause side effect.
                recorder.recordAttribute(AnnotationKey.HTTP_URL, InterceptorUtils.getHttpUrl(httpRequest.getUrl(), config.isProfileParam()));
                final String endpoint = EndPointUtils.getEndPoint(httpRequest.getUrl(), "UnknownHttpClient");
                recorder.recordDestinationId(endpoint);
                recordHttpRequest(recorder, httpRequest, throwable);
            }
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean validate(final Object[] args) {
        if (args == null || args.length == 0 || !(args[0] instanceof Request)) {
            if (isDebug) {
                logger.debug("Invalid args[0] object. args={}.", args);
            }
            return false;
        }

        return true;
    }

    private void recordHttpRequest(SpanEventRecorder recorder, Request httpRequest, Throwable throwable) {
        final boolean isException = InterceptorUtils.isThrowable(throwable);
        if (config.isProfileCookie()) {
            switch (config.getCookieDumpType()) {
                case ALWAYS:
                    recordCookie(httpRequest, recorder);
                    break;
                case EXCEPTION:
                    if (isException) {
                        recordCookie(httpRequest, recorder);
                    }
                    break;
            }
        }
        if (config.isProfileEntity()) {
            switch (config.getEntityDumpType()) {
                case ALWAYS:
                    recordEntity(httpRequest, recorder);
                    break;
                case EXCEPTION:
                    if (isException) {
                        recordEntity(httpRequest, recorder);
                    }
                    break;
            }
        }
        if (config.isProfileParam()) {
            switch (config.getParamDumpType()) {
                case ALWAYS:
                    recordParam(httpRequest, recorder);
                    break;
                case EXCEPTION:
                    if (isException) {
                        recordParam(httpRequest, recorder);
                    }
                    break;
            }
        }
    }

    protected void recordCookie(Request httpRequest, SpanEventRecorder recorder) {
        if (cookieSampler.isSampling()) {
            List<Cookie> cookies = httpRequest.getCookies();
            if (cookies.isEmpty()) {
                return;
            }

            StringBuilder sb = new StringBuilder(config.getCookieDumpSize() * 2);
            Iterator<Cookie> iterator = cookies.iterator();
            while (iterator.hasNext()) {
                Cookie cookie = iterator.next();
                sb.append(cookie.getName()).append('=').append(cookie.getValue());
                if (iterator.hasNext()) {
                    sb.append(',');
                }
            }
            recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.abbreviate(sb.toString(), config.getCookieDumpSize()));
        }
    }

    protected void recordEntity(final Request httpRequest, final SpanEventRecorder recorder) {
        if (entitySampler.isSampling()) {
            recordNonMultipartData(httpRequest, recorder);
            recordMultipartData(httpRequest, recorder);
        }
    }

    /**
     * <pre>
     * Body could be String, byte array, Stream or EntityWriter.
     * We collect String data only.
     * </pre>
     *
     * @param httpRequest
     * @param recorder
     */
    protected void recordNonMultipartData(final Request httpRequest, final SpanEventRecorder recorder) {
        final String stringData = httpRequest.getStringData();
        if (stringData != null) {
            recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, StringUtils.abbreviate(stringData, config.getEntityDumpSize()));
            return;
        }

        final byte[] byteData = httpRequest.getByteData();
        if (byteData != null) {
            recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, "BYTE_DATA");
            return;
        }

        final InputStream streamData = httpRequest.getStreamData();
        if (streamData != null) {
            recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, "STREAM_DATA");
            return;
        }
    }

    protected void recordMultipartData(final Request httpRequest, final SpanEventRecorder recorder) {
        List<Part> parts = httpRequest.getBodyParts();
        // bug fix : parts != null && ****!parts.isEmpty()
        if (CollectionUtils.isNotEmpty(parts)) {
            StringBuilder sb = new StringBuilder(config.getEntityDumpSize() * 2);
            Iterator<Part> iterator = parts.iterator();
            while (iterator.hasNext()) {
                Part part = iterator.next();
                if (part instanceof ByteArrayPart) {
                    ByteArrayPart p = (ByteArrayPart) part;
                    sb.append(part.getName());
                    sb.append("=BYTE_ARRAY_");
                    sb.append(p.getBytes().length);
                } else if (part instanceof FilePart) {
                    FilePart p = (FilePart) part;
                    sb.append(part.getName());
                    sb.append("=FILE_");
                    sb.append(p.getContentType());
                } else if (part instanceof StringPart) {
                    StringPart p = (StringPart) part;
                    sb.append(part.getName());
                    sb.append("=STRING");
                }

                if (sb.length() >= config.getEntityDumpSize()) {
                    break;
                }

                if (iterator.hasNext()) {
                    sb.append(',');
                }
            }
            recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, StringUtils.abbreviate(sb.toString(), config.getEntityDumpSize()));
        }
    }

    protected void recordParam(final Request httpRequest, final SpanEventRecorder recorder) {
        if (paramSampler.isSampling()) {
            List<Param> requestParams = httpRequest.getFormParams();
            if (requestParams != null && !requestParams.isEmpty()) {
                String params = paramsToString(requestParams, config.getParamDumpSize());
                recorder.recordAttribute(AnnotationKey.HTTP_PARAM, StringUtils.abbreviate(params, config.getParamDumpSize()));
            }
        }
    }

    /**
     * Returns string without double quotations marks, spaces, semi-colons from com.ning.http.client.FluentStringsMap.toString()
     *
     * @param params
     * @param limit
     * @return
     */
    private String paramsToString(List<Param> params, int limit) {
        StringBuilder result = new StringBuilder(limit * 2);

        for (Param param : params) {
            if (result.length() > 0) {
                result.append(',');
            }
            result.append(param.getName());
            result.append('=');
            result.append(param.getValue());
            if (result.length() >= limit) {
                break;
            }
        }
        return result.toString();
    }
}