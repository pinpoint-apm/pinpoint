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

package com.navercorp.pinpoint.plugin.jetty.interceptor;

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
import com.navercorp.pinpoint.plugin.common.servlet.util.HttpServletRequestAdaptor;
import com.navercorp.pinpoint.plugin.common.servlet.util.ParameterRecorderFactory;
import com.navercorp.pinpoint.plugin.jetty.JettyConfiguration;
import com.navercorp.pinpoint.plugin.jetty.JettyConstants;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chaein Jung
 * @author jaehong.kim
 */
public abstract class AbstractServerHandleInterceptor implements AroundInterceptor {
    protected final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isInfo = logger.isInfoEnabled();
    private final TraceContext traceContext;

    private final MethodDescriptor methodDescriptor;
    private final ServletRequestListenerInterceptorHelper<HttpServletRequest> servletRequestListenerInterceptorHelper;

    public AbstractServerHandleInterceptor(TraceContext traceContext, MethodDescriptor descriptor, RequestRecorderFactory<HttpServletRequest> requestRecorderFactory) {

        this.methodDescriptor = descriptor;
        final JettyConfiguration config = new JettyConfiguration(traceContext.getProfilerConfig());
        RequestAdaptor<HttpServletRequest> requestRequestAdaptor = new HttpServletRequestAdaptor();
        requestRequestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestRequestAdaptor, config.getRealIpHeader(), config.getRealIpEmptyValue());
        ParameterRecorder<HttpServletRequest> parameterRecorder = ParameterRecorderFactory.newParameterRecorderFactory(config.getExcludeProfileMethodFilter(), config.isTraceRequestParam());
        this.servletRequestListenerInterceptorHelper = new ServletRequestListenerInterceptorHelper<HttpServletRequest>(JettyConstants.JETTY, traceContext, requestRequestAdaptor, config.getExcludeUrlFilter(), parameterRecorder, requestRecorderFactory);
        this.traceContext = traceContext;
    }

    abstract HttpServletRequest toHttpServletRequest(Object[] args);

    abstract HttpServletResponse toHttpServletResponse(Object[] args);

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            final HttpServletRequest request = toHttpServletRequest(args);
            if (request.getDispatcherType() == DispatcherType.ASYNC || request.getDispatcherType() == DispatcherType.ERROR) {
                if (isDebug) {
                    logger.debug("Skip async servlet request event. isAsyncStarted={}, dispatcherType={}", request.isAsyncStarted(), request.getDispatcherType());
                }
                return;
            }
            this.servletRequestListenerInterceptorHelper.initialized(request, JettyConstants.JETTY_METHOD, this.methodDescriptor);
        } catch (Throwable t) {
            logger.info("Failed to servlet request event handle.", t);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        try {
            final HttpServletRequest request = toHttpServletRequest(args);
            final HttpServletResponse response = toHttpServletResponse(args);
            final int statusCode = getStatusCode(response);
            if (request.getDispatcherType() == DispatcherType.ASYNC || request.getDispatcherType() == DispatcherType.ERROR) {
                if (isDebug) {
                    logger.debug("Skip async servlet request event. isAsyncStarted={}, dispatcherType={}", request.isAsyncStarted(), request.getDispatcherType());
                }
                return;
            }

            Trace trace = traceContext.currentTraceObject();
            if (null != trace) {
                recorderWebInfo(trace, request, response, statusCode);

            } else if (traceContext.bodyObtainEnable() && traceContext.bodyObtainStrategy() <  PinpointConstants.STRATEGY_2) {
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
                // 采集请求/响应头==========================
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

    private int getStatusCode(final HttpServletResponse response) {
        try {
            return response.getStatus();
        } catch (Exception ignored) {
        }
        return 0;
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
}