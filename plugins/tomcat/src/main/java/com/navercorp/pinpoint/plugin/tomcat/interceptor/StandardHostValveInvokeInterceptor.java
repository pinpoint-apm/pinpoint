/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListenerInterceptorHelper;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.RemoteAddressResolverFactory;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.common.servlet.util.*;
import com.navercorp.pinpoint.plugin.tomcat.TomcatConfig;
import com.navercorp.pinpoint.plugin.tomcat.TomcatConstants;
import org.apache.catalina.connector.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class StandardHostValveInvokeInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isInfo = logger.isInfoEnabled();

    private final MethodDescriptor methodDescriptor;
    private final ArgumentValidator argumentValidator;
    private final TraceContext traceContext;

    private final String applicationName= System.getProperty("pinpoint.applicationName");
    private final String licence= System.getProperty("pinpoint.licence");

    private final ServletRequestListenerInterceptorHelper<HttpServletRequest> servletRequestListenerInterceptorHelper;


    public StandardHostValveInvokeInterceptor(TraceContext traceContext, MethodDescriptor descriptor, RequestRecorderFactory<HttpServletRequest> requestRecorderFactory) {
        this.methodDescriptor = descriptor;
        this.argumentValidator = new ServletArgumentValidator(logger, 0, HttpServletRequest.class, 1, HttpServletResponse.class);
        final TomcatConfig config = new TomcatConfig(traceContext.getProfilerConfig());
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, config.getRealIpHeader(), config.getRealIpEmptyValue());
        ParameterRecorder<HttpServletRequest> parameterRecorder = ParameterRecorderFactory.newParameterRecorderFactory(config.getExcludeProfileMethodFilter(), config.isTraceRequestParam());
        this.servletRequestListenerInterceptorHelper = new ServletRequestListenerInterceptorHelper<HttpServletRequest>(TomcatConstants.TOMCAT, traceContext, requestAdaptor, config.getExcludeUrlFilter(), parameterRecorder, requestRecorderFactory);
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!argumentValidator.validate(args)) {
            return;
        }

        try {
            final HttpServletRequest request = (HttpServletRequest) args[0];
            final Trace asyncTrace = (Trace) request.getAttribute(TomcatConstants.TOMCAT_SERVLET_REQUEST_TRACE);
            if (asyncTrace != null) {
                if (isDebug) {
                    logger.debug("Skip async servlet request event.");
                }
                // skip
                return;
            }
            this.servletRequestListenerInterceptorHelper.initialized(request, TomcatConstants.TOMCAT_METHOD, this.methodDescriptor);
        } catch (Throwable t) {
            if (isInfo) {
                logger.info("Failed to servlet request event handle.", t);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (!argumentValidator.validate(args)) {
            return;
        }

        Trace trace = traceContext.currentTraceObject();

        try {
            final HttpServletRequest request = (HttpServletRequest) args[0];
            final HttpServletResponse response = (HttpServletResponse) args[1];
            int statusCode = getStatusCode(response);

            if (null != trace) {
                recorderWebInfo(trace, request, response, statusCode);

            } else if (traceContext.bodyObtainEnable() && traceContext.bodyObtainStrategy() < PinpointConstants.STRATEGY_2) {
                // 此处代表被采样率过滤掉的trace，同时采样策略需要采集此部分调用报文
                Trace rawTraceObject = traceContext.currentRawTraceObject();
                if (null != rawTraceObject) {
                    recorderWebInfo(rawTraceObject, request, response, statusCode);
                }

            }

            this.servletRequestListenerInterceptorHelper.destroyed(request, throwable, statusCode);
        } catch (Throwable t) {
            if (isInfo) {
                logger.info("Failed to servlet request event handle.", t);
            }
        }
    }

    /**
     * 采集报文信息
     *
     * @param trace
     * @param request
     * @param response
     * @param statusCode
     */
    private void recorderWebInfo(Trace trace, HttpServletRequest request, HttpServletResponse response, int statusCode) {
        try {
            SpanRecorder spanRecorder = trace.getSpanRecorder();
            if (null != spanRecorder) {

                // 采集请求/响应头，标记请求异常/正常 ==========================
                if (traceContext.bodyObtainEnable()) {
                    headerObtain(request, response, spanRecorder, statusCode);
                }
                // =========================================
            }
        } catch (Throwable t) {
            if (isInfo) {
                logger.info("采集报文报错，异常信息：", t);
            }
        }
    }

    /**
     * 采集请求头、响应头、请求url
     *
     * @param request      请求
     * @param response     响应
     * @param spanRecorder 采集器
     * @param statusCode
     */
    private void headerObtain(HttpServletRequest request, HttpServletResponse response, SpanRecorder spanRecorder, int statusCode) {

        // 赋值采样策略
        spanRecorder.recordWebInfoStrategy(traceContext.bodyObtainStrategy());

        // 采集请求方式
        spanRecorder.recordWebInfoRequestMethod(request.getMethod());

        // 采集响应码
        spanRecorder.recordWebInfoStatusCode(statusCode);

        // 采集请求url
        spanRecorder.recordWebInfoRequestUrl(request.getRequestURL().toString());

        Enumeration<String> requestHeaderNames = request.getHeaderNames();
        Collection<String> responseHeaderNames = response.getHeaderNames();
        Map<String, String> requestHeaders = new HashMap<String, String>(16);
        while (requestHeaderNames.hasMoreElements()) {
            String name = requestHeaderNames.nextElement();
            String value = request.getHeader(name);
            requestHeaders.put(name, value);
        }
        // 采集请求头
        if (!requestHeaders.isEmpty()) {
            spanRecorder.recordWebInfoRequestHeader(requestHeaders);

        }
        if (!responseHeaderNames.isEmpty()) {
            Map<String, String> responseHeaders = new HashMap<String, String>(responseHeaderNames.size());
            for (String responseHeaderName : responseHeaderNames) {
                String value = response.getHeader(responseHeaderName);
                responseHeaders.put(responseHeaderName, value);
            }
            // 采集响应头
            spanRecorder.recordWebInfoResponseHeader(responseHeaders);
        }
    }

    private int getStatusCode(final HttpServletResponse response) {
        try {
            // Tomcat 6
            if (response instanceof Response) {
                final Response r = (Response) response;
                return r.getStatus();
            } else {
                response.getStatus();
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}