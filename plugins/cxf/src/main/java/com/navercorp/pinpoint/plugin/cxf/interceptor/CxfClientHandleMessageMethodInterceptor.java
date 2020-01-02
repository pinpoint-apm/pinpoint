/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.cxf.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.cxf.CxfPluginConfig;
import com.navercorp.pinpoint.plugin.cxf.CxfPluginConstants;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * The type Cxf client handle message method interceptor.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/08/16
 */
@Deprecated
public class CxfClientHandleMessageMethodInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final CxfPluginConfig pluginConfig;

    /**
     * Instantiates a new Cxf client handle message method interceptor.
     *
     * @param traceContext the trace context
     * @param descriptor   the descriptor
     */
    public CxfClientHandleMessageMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.pluginConfig = new CxfPluginConfig(traceContext.getProfilerConfig());
    }

    @Override
    public void before(Object target, Object[] args) {

        if (logger.isDebugEnabled()) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace != null) {
            String destination = getDestination(args);
            if (destination != null) {
                String httpUri = getHttpUri(args);
                String requestMethod = getRequestMethod(args);
                String contentType = getContentType(args);

                SpanEventRecorder recorder = trace.traceBlockBegin();
                TraceId nextId = trace.getTraceId().getNextTraceId();
                recorder.recordNextSpanId(nextId.getSpanId());
                recorder.recordServiceType(CxfPluginConstants.CXF_CLIENT_SERVICE_TYPE);
                recorder.recordDestinationId(destination);
                recorder.recordAttribute(CxfPluginConstants.CXF_ADDRESS, httpUri);
                recorder.recordAttribute(CxfPluginConstants.CXF_HTTP_METHOD, requestMethod);
                recorder.recordAttribute(CxfPluginConstants.CXF_CONTENT_TYPE, contentType);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.afterInterceptor(target, args);
        }
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
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

    private String getDestination(Object[] args) {

        if (args[0] instanceof Map) {

            Map message = (Map) args[0];

            String address = (String) message.get("org.apache.cxf.message.Message.ENDPOINT_ADDRESS");

            try {
                java.net.URL url = new java.net.URL(address);

                return url.getProtocol() + "://" + url.getAuthority();

            } catch (MalformedURLException e) {
            }
        }
        return null;
    }

    private String getHttpUri(Object[] args) {

        if (args[0] instanceof Map) {

            Map message = (Map) args[0];

            String httpUri = (String) message.get("org.apache.cxf.request.uri");

            return httpUri != null ? httpUri : "unknown";
        }

        return "unknown";
    }

    private String getRequestMethod(Object[] args) {

        if (args[0] instanceof Map) {

            Map message = (Map) args[0];

            String requestMethod = (String) message.get("org.apache.cxf.request.method");

            return requestMethod != null ? requestMethod : "unknown";
        }
        return "unknown";
    }

    private String getContentType(Object[] args) {

        if (args[0] instanceof Map) {

            Map message = (Map) args[0];

            String contentType = (String) message.get("Content-Type");

            return contentType != null ? contentType : "unknown";
        }
        return "unknown";
    }
}