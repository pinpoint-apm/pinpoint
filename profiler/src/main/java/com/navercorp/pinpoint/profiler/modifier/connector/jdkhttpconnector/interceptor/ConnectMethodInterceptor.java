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

package com.navercorp.pinpoint.profiler.modifier.connector.jdkhttpconnector.interceptor;

import java.net.HttpURLConnection;
import java.net.URL;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.MetaObject;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * @author netspider
 * @author emeroad
 */
public class ConnectMethodInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {
    private final MetaObject<Boolean> isConnected = new MetaObject<Boolean>("__isConnected");
    private final MetaObject<Boolean> isConnecting = new MetaObject<Boolean>("__isConnecting");

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final boolean hasConnecting;
    private MethodDescriptor descriptor;
    private TraceContext traceContext;
    
    public ConnectMethodInterceptor(boolean hasConnecting) {
        this.hasConnecting = hasConnecting;
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
        final boolean setRequestHeader = !isConnected.invoke(target) && (!hasConnecting || !isConnecting.invoke(target));
        
        final boolean sampling = trace.canSampled();
        if (!sampling) {
            if (setRequestHeader) {
                request.setRequestProperty(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
            }
            return;
        }


        trace.traceBlockBegin();
        trace.markBeforeTime();

        TraceId nextId = trace.getTraceId().getNextTraceId();
        trace.recordNextSpanId(nextId.getSpanId());

        if (setRequestHeader) {
            request.setRequestProperty(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
            request.setRequestProperty(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
            request.setRequestProperty(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));
    
            request.setRequestProperty(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
            request.setRequestProperty(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
            request.setRequestProperty(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));
        }
        
        trace.recordServiceType(ServiceType.JDK_HTTPURLCONNECTOR);

        final URL url = request.getURL();
        final String host = url.getHost();
        final int port = url.getPort();

        // TODO How to represent protocol?
        String endpoint = getEndpoint(host, port);
        
        // Don't record end point because it's same with destination id.
        trace.recordDestinationId(endpoint);
        trace.recordAttribute(AnnotationKey.HTTP_URL, url.toString());
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

        try {
            trace.recordApi(descriptor);
            trace.recordException(throwable);

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        traceContext.cacheApi(descriptor);
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }
}