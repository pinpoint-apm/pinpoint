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

package com.navercorp.pinpoint.plugin.jdk.http.interceptor;

import java.net.HttpURLConnection;
import java.net.URL;

import com.navercorp.pinpoint.bootstrap.FieldAccessor;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.TargetMethod;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Targets;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpConstants;

/**
 * @author netspider
 * @author emeroad
 */
@Group("HttpURLConnection")
@Targets(methods={
        @TargetMethod(name="connect"),
        @TargetMethod(name="getInputStream"),
        @TargetMethod(name="getOutputStream")
})
public class HttpURLConnectionInterceptor implements SimpleAroundInterceptor, JdkHttpConstants {
    private static final Object TRACE_BLOCK_BEGIN_MARKER = new Object();
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final FieldAccessor connectedAccessor;
    private final FieldAccessor connectingAccessor;
    private final InterceptorGroup group;
    
    public HttpURLConnectionInterceptor(TraceContext traceContext, MethodDescriptor descriptor, @Name("connected") FieldAccessor connectedAccessor, @Name("connecting") FieldAccessor connectingAccessor, InterceptorGroup group) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.connectedAccessor = connectedAccessor;
        this.connectingAccessor = connectingAccessor;
        this.group = group;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        HttpURLConnection request = (HttpURLConnection) target;
        
        boolean connected = (Boolean)connectedAccessor.get(target);
        boolean connecting = connectingAccessor.isApplicable(target) && (Boolean)connectingAccessor.get(target);
        
        if (connected || connecting) {
            return;
        }
        
        final boolean sampling = trace.canSampled();
        if (!sampling) {
            request.setRequestProperty(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
            return;
        }

        group.getCurrentInvocation().setAttachment(TRACE_BLOCK_BEGIN_MARKER);
        
        SpanEventRecorder recorder = trace.traceBlockBegin();
        TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());

        final URL url = request.getURL();
        final String host = url.getHost();
        final int port = url.getPort();

        request.setRequestProperty(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
        request.setRequestProperty(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
        request.setRequestProperty(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));

        request.setRequestProperty(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
        request.setRequestProperty(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
        request.setRequestProperty(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));
        if(host != null) {
            request.setRequestProperty(Header.HTTP_HOST.toString(), host);
        }

        recorder.recordServiceType(SERVICE_TYPE);

        // TODO How to represent protocol?
        String endpoint = getEndpoint(host, port);
        
        // Don't record end point because it's same with destination id.
        recorder.recordDestinationId(endpoint);
        recorder.recordAttribute(AnnotationKey.HTTP_URL, url.toString());
    }

    private String getEndpoint(String host, int port) {
        if (port < 0) {
            return host;
        }
        StringBuilder sb = new StringBuilder(32);
        sb.append(host);
        sb.append(':');
        sb.append(port);
        return sb.toString();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            // do not log result
            logger.afterInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        
        Object marker = group.getCurrentInvocation().getAttachment();
        
        if (marker != TRACE_BLOCK_BEGIN_MARKER) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }
}