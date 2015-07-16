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

package com.navercorp.pinpoint.profiler.modifier.connector.asynchttpclient.interceptor;

import java.io.InputStream;
import java.util.*;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.bootstrap.util.SimpleSampler;
import com.navercorp.pinpoint.bootstrap.util.SimpleSamplerFactory;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.FluentStringsMap;
import com.ning.http.client.Part;
import com.ning.http.client.Request.EntityWriter;
import com.ning.http.client.cookie.Cookie;

/**
 * intercept com.ning.http.client.AsyncHttpClient.executeRequest(Request,
 * AsyncHandler<T>)
 * 
 * @author netspider
 * 
 */
public class ExecuteRequestInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

    protected final PLogger logger = PLoggerFactory.getLogger(ExecuteRequestInterceptor.class);
    protected final boolean isDebug = logger.isDebugEnabled();

    protected TraceContext traceContext;
    protected MethodDescriptor descriptor;

    protected boolean dumpCookie;
    protected DumpType cookieDumpType;
    protected SimpleSampler cookieSampler;
    protected int cookieDumpSize;

    protected boolean dumpEntity;
    protected DumpType entityDumpType;
    protected SimpleSampler entitySampler;
    protected int entityDumpSize;

    protected boolean dumpParam;
    protected DumpType paramDumpType;
    protected SimpleSampler paramSampler;
    protected int paramDumpSize;


    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if (args.length == 0 || !(args[0] instanceof com.ning.http.client.Request)) {
            return;
        }

        final com.ning.http.client.Request httpRequest = (com.ning.http.client.Request) args[0];

        final boolean sampling = trace.canSampled();

        if (!sampling) {
            if (isDebug) {
                logger.debug("set Sampling flag=false");
            }
            if (httpRequest != null) {
                final FluentCaseInsensitiveStringsMap httpRequestHeaders = httpRequest.getHeaders();
                httpRequestHeaders.add(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
            }
            return;
        }

        trace.traceBlockBegin();
        SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordServiceType(ServiceType.HTTP_CLIENT);

        if (httpRequest != null) {
            final FluentCaseInsensitiveStringsMap httpRequestHeaders = httpRequest.getHeaders();
            putHeader(httpRequestHeaders, Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
            putHeader(httpRequestHeaders, Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
            putHeader(httpRequestHeaders, Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));
            putHeader(httpRequestHeaders, Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
            putHeader(httpRequestHeaders, Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
            putHeader(httpRequestHeaders, Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));
            final String host = httpRequest.getURI().getHost();
            if(host != null) {
                putHeader(httpRequestHeaders, Header.HTTP_HOST.toString(), host);
            }
        }
    }

    private void putHeader(FluentCaseInsensitiveStringsMap httpRequestHeaders, String key, String value) {
        final List<String> valueList = new ArrayList<String>();
        valueList.add(value);
        httpRequestHeaders.put(key, valueList);
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

        if (args.length == 0 || !(args[0] instanceof com.ning.http.client.Request)) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            final com.ning.http.client.Request httpRequest = (com.ning.http.client.Request) args[0];
            if (httpRequest != null) {
                // Accessing httpRequest here not before() because it can cause side effect.
                recorder.recordAttribute(AnnotationKey.HTTP_URL, httpRequest.getUrl());

                String endpoint = getEndpoint(httpRequest.getURI().getHost(), httpRequest.getURI().getPort());
                recorder.recordDestinationId(endpoint);

                recordHttpRequest(recorder, httpRequest, throwable);
            }

            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }

    private String getEndpoint(String host, int port) {
        if (host == null) {
            return "UnknownHttpClient";
        }
        if (port < 0) {
            return host;
        }
        final StringBuilder sb = new StringBuilder(host.length() + 8);
        sb.append(host);
        sb.append(':');
        sb.append(port);
        return sb.toString();
    }

    private void recordHttpRequest(SpanEventRecorder recorder, com.ning.http.client.Request httpRequest, Throwable throwable) {
        final boolean isException = InterceptorUtils.isThrowable(throwable);
        if (dumpCookie) {
            if (DumpType.ALWAYS == cookieDumpType) {
                recordCookie(httpRequest, recorder);
            } else if (DumpType.EXCEPTION == cookieDumpType && isException) {
                recordCookie(httpRequest, recorder);
            }
        }
        if (dumpEntity) {
            if (DumpType.ALWAYS == entityDumpType) {
                recordEntity(httpRequest, recorder);
            } else if (DumpType.EXCEPTION == entityDumpType && isException) {
                recordEntity(httpRequest, recorder);
            }
        }
        if (dumpParam) {
            if (DumpType.ALWAYS == paramDumpType) {
                recordParam(httpRequest, recorder);
            } else if (DumpType.EXCEPTION == paramDumpType && isException) {
                recordParam(httpRequest, recorder);
            }
        }
    }

    protected void recordCookie(com.ning.http.client.Request httpRequest, SpanEventRecorder recorder) {
        if (cookieSampler.isSampling()) {
            Collection<Cookie> cookies = httpRequest.getCookies();

            if (cookies.isEmpty()) {
                return;
            }

            StringBuilder sb = new StringBuilder(cookieDumpSize * 2);
            Iterator<Cookie> iterator = cookies.iterator();
            while (iterator.hasNext()) {
                Cookie cookie = iterator.next();
                sb.append(cookie.getName()).append("=").append(cookie.getValue());
                if (iterator.hasNext()) {
                    sb.append(",");
                }
            }
            recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, StringUtils.drop(sb.toString(), cookieDumpSize));
        }
    }

    protected void recordEntity(final com.ning.http.client.Request httpRequest, final SpanEventRecorder recorder) {
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
    protected void recordNonMultipartData(final com.ning.http.client.Request httpRequest, final SpanEventRecorder recorder) {
        final String stringData = httpRequest.getStringData();
        if (stringData != null) {
            recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, StringUtils.drop(stringData, entityDumpSize));
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

        final EntityWriter entityWriter = httpRequest.getEntityWriter();
        if (entityWriter != null) {
            recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, "STREAM_DATA");
            return;
        }
    }

    /**
     * record http multipart data
     *
     * @param httpRequest
     * @param recorder
     */
    protected void recordMultipartData(final com.ning.http.client.Request httpRequest, final SpanEventRecorder recorder) {
        List<Part> parts = httpRequest.getParts();
        if (parts != null && parts.isEmpty()) {
            StringBuilder sb = new StringBuilder(entityDumpSize * 2);
            Iterator<Part> iterator = parts.iterator();
            while (iterator.hasNext()) {
                Part part = iterator.next();
                if (part instanceof com.ning.http.client.ByteArrayPart) {
                    com.ning.http.client.ByteArrayPart p = (com.ning.http.client.ByteArrayPart) part;
                    sb.append(part.getName());
                    sb.append("=BYTE_ARRAY_");
                    sb.append(p.getData().length);
                } else if (part instanceof com.ning.http.client.FilePart) {
                    com.ning.http.client.FilePart p = (com.ning.http.client.FilePart) part;
                    sb.append(part.getName());
                    sb.append("=FILE_");
                    sb.append(p.getMimeType());
                } else if (part instanceof com.ning.http.client.StringPart) {
                    com.ning.http.client.StringPart p = (com.ning.http.client.StringPart) part;
                    sb.append(part.getName());
                    sb.append("=");
                    sb.append(p.getValue());
                } else if (part instanceof com.ning.http.multipart.FilePart) {
                    com.ning.http.multipart.FilePart p = (com.ning.http.multipart.FilePart) part;
                    sb.append(part.getName());
                    sb.append("=FILE_");
                    sb.append(p.getContentType());
                } else if (part instanceof com.ning.http.multipart.StringPart) {
                    com.ning.http.multipart.StringPart p = (com.ning.http.multipart.StringPart) part;
                    sb.append(part.getName());
                    // Ignore value because there's no way to get string value and StringPart is an adaptation class of Apache HTTP client.
                    sb.append("=STRING");
                }

                if (sb.length() >= entityDumpSize) {
                    break;
                }

                if (iterator.hasNext()) {
                    sb.append(",");
                }
            }
            recorder.recordAttribute(AnnotationKey.HTTP_PARAM_ENTITY, StringUtils.drop(sb.toString(), entityDumpSize));
        }
    }

    /**
     * record http request parameter
     *
     * @param httpRequest
     * @param recorder
     */
    protected void recordParam(final com.ning.http.client.Request httpRequest, final SpanEventRecorder recorder) {
        if (paramSampler.isSampling()) {
            FluentStringsMap requestParams = httpRequest.getParams();
            if (requestParams != null) {
                String params = paramsToString(requestParams, paramDumpSize);
                recorder.recordAttribute(AnnotationKey.HTTP_PARAM, StringUtils.drop(params, paramDumpSize));
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
    private String paramsToString(FluentStringsMap params, int limit) {
        StringBuilder result = new StringBuilder(limit * 2);

        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            if (result.length() > 0) {
                result.append(",");
            }
            result.append(entry.getKey());
            result.append("=");

            boolean needsComma = false;

            for (String value : entry.getValue()) {
                if (needsComma) {
                    result.append(", ");
                } else {
                    needsComma = true;
                }
                result.append(value);
            }

            if (result.length() >= limit) {
                break;
            }
        }
        return result.toString();
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;

        final ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
        this.dumpCookie = profilerConfig.isNingAsyncHttpClientProfileCookie();
        this.cookieDumpType = profilerConfig.getNingAsyncHttpClientProfileCookieDumpType();
        this.cookieDumpSize = profilerConfig.getNingAsyncHttpClientProfileCookieDumpSize();
        if (dumpCookie) {
            this.cookieSampler = SimpleSamplerFactory.createSampler(dumpCookie, profilerConfig.getNingAsyncHttpClientProfileCookieSamplingRate());
        }

        this.dumpEntity = profilerConfig.isNingAsyncHttpClientProfileEntity();
        this.entityDumpType = profilerConfig.getNingAsyncHttpClientProfileEntityDumpType();
        this.entityDumpSize = profilerConfig.getNingAsyncHttpClientProfileEntityDumpSize();
        if (dumpEntity) {
            this.entitySampler = SimpleSamplerFactory.createSampler(dumpEntity, profilerConfig.getNingAsyncHttpClientProfileEntitySamplingRate());
        }

        this.dumpParam = profilerConfig.isNingAsyncHttpClientProfileParam();
        this.paramDumpType = profilerConfig.getNingAsyncHttpClientProfileParamDumpType();
        this.paramDumpSize = profilerConfig.getNingAsyncHttpClientProfileParamDumpSize();
        if (dumpParam) {
            this.paramSampler = SimpleSamplerFactory.createSampler(dumpParam, profilerConfig.getNingAsyncHttpClientProfileParamSamplingRate());
        }
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        traceContext.cacheApi(descriptor);
    }
}
