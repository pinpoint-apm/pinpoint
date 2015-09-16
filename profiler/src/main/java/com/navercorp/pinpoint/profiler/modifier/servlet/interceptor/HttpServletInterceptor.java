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

package com.navercorp.pinpoint.profiler.modifier.servlet.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
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
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.context.SpanId;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @author emeroad
 * @authro netspider
 */
public class HttpServletInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private MethodDescriptor descriptor;
    private TraceContext traceContext;
    private boolean parameter;

/*    
    java.lang.IllegalStateException: already Trace Object exist.
    at com.profiler.context.TraceContext.attachTraceObject(TraceContext.java:54)
    at HttpServletInterceptor.before(HttpServletInterceptor.java:62)
    at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java)                                // profile method
**    at javax.servlet.http.HttpServlet.service(HttpServlet.java:617)                                                 // profile method
    at javax.servlet.http.HttpServlet.service(HttpServlet.java:717)
    at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:290)
    at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:206)
    at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:88)
    at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:76)
    at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:235)
    at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:206)
    at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:233)
    at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:191)
**    at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:127)                                  // make traceId here
    at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:102)
    at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:109)
    at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:293)
    at org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:859)
    at org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.process(Http11Protocol.java:602)
    at org.apache.tomcat.util.net.JIoEndpoint$Worker.run(JIoEndpoint.java:489)
    at java.lang.Thread.run(Thread.java:680)
*/
    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
//            traceContext.getActiveThreadCounter().start();

            HttpServletRequest request = (HttpServletRequest) args[0];
            boolean sampling = samplingEnable(request);
            if (!sampling) {
                // Even if this transaction is not a sampling target, we have to create Trace object to mark 'not sampling'.
                // For example, if this transaction invokes rpc call, we can add parameter to tell remote node 'don't sample this transaction'  
                traceContext.disableSampling();
                return;
            }

            String requestURL = request.getRequestURI();
            String remoteAddr = request.getRemoteAddr();

            TraceId traceId = populateTraceIdFromRequest(request);
            Trace trace;
            if (traceId != null) {
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. {} requestUrl:{}, remoteAddr:{}", new Object[]{traceId, requestURL, remoteAddr});
                }
                trace = traceContext.continueTraceObject(traceId);
                if (!trace.canSampled()) {
                    if (isDebug) {
                        logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[]{traceId, requestURL, remoteAddr});
                    }
                    return;
                } else {
                    if (isDebug) {
                        logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", new Object[]{traceId, requestURL, remoteAddr});
                    }
                }
            } else {
                trace = traceContext.newTraceObject();
                if (!trace.canSampled()){
                    if (isDebug) {
                        logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", new Object[]{requestURL, remoteAddr});
                    }
                    return;
                } else {
                    if (isDebug) {
                        logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", new Object[]{requestURL, remoteAddr});
                    }
                }
            }

            trace.markBeforeTime();
            // TODO Wrong. should be Servlet
            trace.recordServiceType(ServiceType.TOMCAT);
            trace.recordRpcName(requestURL);

            int port = request.getServerPort();
            trace.recordEndPoint(request.getProtocol() + ":" + request.getServerName() + ((port > 0) ? ":" + port : ""));
            trace.recordDestinationId(request.getServerName() + ((port > 0) ? ":" + port : ""));
            trace.recordAttribute(AnnotationKey.HTTP_URL, request.getRequestURI());
        } catch (Throwable e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Tomcat StandardHostValve trace start fail. Caused:{}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        try {
            traceContext.detachTraceObject();

            HttpServletRequest request = (HttpServletRequest) args[0];

            if (parameter) {
                String parameters = getRequestParameter(request);
                if (parameters != null && parameters.length() > 0) {
                    trace.recordAttribute(AnnotationKey.HTTP_PARAM, parameters);
                }
            }


            trace.recordApi(descriptor);

            trace.recordException(throwable);

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean samplingEnable(HttpServletRequest request) {
        // optional value.
        String samplingFlag = request.getHeader(Header.HTTP_SAMPLED.toString());
        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    /**
     * Pupulate source trace from HTTP Header.
     *
     * @param request
     * @return
     */
    private TraceId populateTraceIdFromRequest(HttpServletRequest request) {
        String transactionId = request.getHeader(Header.HTTP_TRACE_ID.toString());
        if (transactionId != null) {
            long parentSpanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL);
            long spanID = NumberUtils.parseLong(request.getHeader(Header.HTTP_SPAN_ID.toString()), SpanId.NULL);
            short flags = NumberUtils.parseShort(request.getHeader(Header.HTTP_FLAGS.toString()), (short) 0);

            TraceId id = this.traceContext.createTraceId(transactionId, parentSpanID, spanID, flags);
            if (isDebug) {
                logger.debug("TraceID exist. continue trace. {}", id);
            }
            return id;
        } else {
            return null;
        }
    }

    private String getRequestParameter(HttpServletRequest request) {
        Enumeration<?> attrs = request.getParameterNames();
        StringBuilder params = new StringBuilder();

        while (attrs.hasMoreElements()) {
            String keyString = attrs.nextElement().toString();
            Object value = request.getParameter(keyString);

            if (value != null) {
                String valueString = value.toString();
                int valueStringLength = valueString.length();

                if (valueStringLength > 0 && valueStringLength < 100) {
                    params.append(keyString).append("=").append(valueString);
                }

                if (attrs.hasMoreElements()) {
                    params.append(", ");
                }
            }
        }
        return params.toString();
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext.cacheApi(descriptor);
    }


    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
        ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
        this.parameter = profilerConfig.isServletProfileParameter();
    }
}
