/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.axis2.jaxws.interceptor;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.axis2.jaxws.Axis2JaxWsConstants;
import com.navercorp.pinpoint.plugin.axis2.jaxws.Axis2JaxWsSyncMethodDescriptor;

public abstract class AbstractMessageHandleInterceptor implements AroundInterceptor {

    public static final Axis2JaxWsSyncMethodDescriptor AXIS2_JAXWS_SYNC_API_TAG = new Axis2JaxWsSyncMethodDescriptor();

    protected PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isTrace = logger.isTraceEnabled();

    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;
    private final Filter<String> excludeUrlFilter;

    public AbstractMessageHandleInterceptor(TraceContext traceContext, MethodDescriptor descriptor, Filter<String> excludeFilter) {

        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;
        this.excludeUrlFilter = excludeFilter;

        traceContext.cacheApi(AXIS2_JAXWS_SYNC_API_TAG);
    }

    protected abstract MessageContext getRequest(Object[] args);

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            final Trace trace = createTrace(target, args);
            if (trace == null) {
                return;
            }
            // TODO STATDISABLE this logic was added to disable statistics tracing
            if (!trace.canSampled()) {
                return;
            }
            // ------------------------------------------------------
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("before. Caused:{}", th.getMessage(), th);
            }
        }
    }


    private Trace createTrace(Object target, Object[] args) {
        final MessageContext request = getRequest(args);

        final EndpointDescription endpointDescription = request.getEndpointDescription();
        final String requestURI = endpointDescription.getEndpointAddress();
        if (excludeUrlFilter.filter(requestURI)) {
            if (isTrace) {
                logger.trace("filter requestURI:{}", requestURI);
            }
            return null;
        }
        
        Trace trace = traceContext.currentTraceObject();
        if(trace == null) {
            return null;
        }

        TraceId traceId = trace.getTraceId();
        if (trace.canSampled()) {
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recordRootSpan(recorder, request);
            if (isDebug) {
                logger.debug("TraceID exist. continue trace. traceId:{}, remoteAddr:{}", traceId, requestURI);
            }
        } else {
            if (isDebug) {
                logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, remoteAddr:{}", traceId, requestURI);
            }
        }
        
        return trace;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        // TODO STATDISABLE this logic was added to disable statistics tracing
        if (!trace.canSampled()) {
            return;
        }
        // ------------------------------------------------------
        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            final MessageContext request = getRequest(args);
            final String parameters = getRequestParameter(request, 512);
            if (parameters != null && parameters.length() > 0) {
                recorder.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
            }

            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("after. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private String getRequestParameter(MessageContext request, int totalLimit) {
        final OperationDescription operationDescription = request.getOperationDescription();
        String requestString;
        
        if(operationDescription != null) {
            String methodName = operationDescription.getJavaMethodName();
            String[] params = operationDescription.getJavaParameters();
            int length = params.length;
            String paramsPart = "?";
            for (int i = 0 ; i < length; i++) {
                paramsPart += params[i];
                if(i < length - 1) {
                    paramsPart += "&";
                }
                // skip appending parameters if parameter size is bigger than
                // totalLimit
                if (paramsPart.length() > totalLimit) {
                    paramsPart += "...";
                    break;
                }
            }
            requestString = methodName + paramsPart;
        } else {
            QName operationName = request.getOperationName();
            if(operationName != null) {
                requestString = operationName.getLocalPart();
            } else {
                requestString = ServiceClient.ANON_OUT_IN_OP.getLocalPart();
            }
        }
        
        return requestString;
    }
    
    private void recordRootSpan(final SpanEventRecorder recorder, final MessageContext request) {
        // root
        recorder.recordServiceType(Axis2JaxWsConstants.AXIS2_JAXWS_METHOD);
        final EndpointDescription endpointDescription = request.getEndpointDescription();

        final String requestURL = endpointDescription.getEndpointAddress();
        String endPoint = requestURL;
        String remoteAddress = requestURL;
        try {
            final URI uri = new URI(requestURL);
            int port = uri.getPort();
            String host = uri.getHost();
            endPoint = host + ":" + port;
            remoteAddress = host;
        } catch (URISyntaxException e) {
            logger.warn("URI {} Caused:{}", requestURL, e.getMessage(), e);
        }
        
        recorder.recordRpcName(requestURL);

        recorder.recordEndPoint(endPoint);

        recorder.recordDestinationId(remoteAddress);

        recorder.recordApi(AXIS2_JAXWS_SYNC_API_TAG);
    }

}
