/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.undertow.interceptor;

import com.navercorp.pinpoint.bootstrap.config.Filter;
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
import com.navercorp.pinpoint.plugin.common.servlet.util.ArgumentValidator;
import com.navercorp.pinpoint.plugin.undertow.ParameterRecorderFactory;
import com.navercorp.pinpoint.plugin.undertow.UndertowConfig;
import com.navercorp.pinpoint.plugin.undertow.UndertowConstants;
import com.navercorp.pinpoint.plugin.undertow.UndertowHttpHeaderFilter;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jaehong.kim
 */
public class ConnectorsExecuteRootHandlerInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isInfo = logger.isInfoEnabled();

    private final MethodDescriptor methodDescriptor;
    private final ArgumentValidator argumentValidator;
    private final UndertowHttpHeaderFilter httpHeaderFilter;
    private final ServletRequestListenerInterceptorHelper<HttpServerExchange> servletRequestListenerInterceptorHelper;

    private final TraceContext traceContext;
    public ConnectorsExecuteRootHandlerInterceptor(TraceContext traceContext, MethodDescriptor descriptor, RequestRecorderFactory<HttpServerExchange> requestRecorderFactory) {
        this.methodDescriptor = descriptor;
        this.traceContext = traceContext;
        final UndertowConfig config = new UndertowConfig(traceContext.getProfilerConfig());
        this.argumentValidator = new ConnectorsArgumentValidator(config.getHttpHandlerClassNameFilter());
        RequestAdaptor<HttpServerExchange> requestAdaptor = new HttpServerExchangeAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, config.getRealIpHeader(), config.getRealIpEmptyValue());
        ParameterRecorder<HttpServerExchange> parameterRecorder = ParameterRecorderFactory.newParameterRecorderFactory(config.getExcludeProfileMethodFilter(), config.isTraceRequestParam());
        this.servletRequestListenerInterceptorHelper = new ServletRequestListenerInterceptorHelper<HttpServerExchange>(UndertowConstants.UNDERTOW, traceContext, requestAdaptor, config.getExcludeUrlFilter(), parameterRecorder, requestRecorderFactory);
        this.httpHeaderFilter = new UndertowHttpHeaderFilter(config.isHidePinpointHeader());
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
            final HttpServerExchange request = (HttpServerExchange) args[1];
            this.servletRequestListenerInterceptorHelper.initialized(request, UndertowConstants.UNDERTOW_METHOD, this.methodDescriptor);
            this.httpHeaderFilter.filter(request);
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

        try {
            final HttpServerExchange request = (HttpServerExchange) args[1];
            final int statusCode = getStatusCode(request);

            // 请求、响应头信息采集逻辑===================
            if (traceContext.bodyObtainEnable()) {
                Trace currentTraceObject = traceContext.currentTraceObject();
                if (null != currentTraceObject) {
                    recorderWebInfo(currentTraceObject, request, statusCode);

                } else if (traceContext.bodyObtainStrategy() <  PinpointConstants.STRATEGY_2) {
                    // 此处代表被采样率过滤掉的trace，同时采样策略需要采集此部分调用报文
                    Trace rawTraceObject = traceContext.currentRawTraceObject();
                    if (null != rawTraceObject) {
                        recorderWebInfo(rawTraceObject, request, statusCode);
                    }
                }
            }
            // =========================================

            // TODO Get exception. e.g. request.getAttachment(DefaultResponseListener.EXCEPTION)
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
     * @param statusCode
     */
    private void recorderWebInfo(Trace trace, HttpServerExchange request, int statusCode) {
        try {
            SpanRecorder spanRecorder = trace.getSpanRecorder();
            if (null != spanRecorder) {
                // 采集请求/响应头，标记请求异常/正常 ==========================
                headerObtain(request, spanRecorder, statusCode);
                // =========================================
            }
        } catch (Throwable t) {
            if (isInfo) {
                logger.info("采集报文报错，异常信息：", t);
            }
        }
    }

    private int getStatusCode(final HttpServerExchange response) {
        try {
            return response.getStatusCode();
        } catch (Exception ignored) {
        }
        return 0;
    }

    private static class ConnectorsArgumentValidator implements ArgumentValidator {
        private final Filter<String> httpHandlerClassNameFilter;

        public ConnectorsArgumentValidator(final Filter<String> httpHandlerClassNameFilter) {
            this.httpHandlerClassNameFilter = httpHandlerClassNameFilter;
        }

        @Override
        public boolean validate(Object[] args) {
            if (args == null) {
                return false;
            }

            if (args.length < 2) {
                return false;
            }

            if (!(args[0] instanceof HttpHandler)) {
                return false;
            }

            final String httpHandlerClassName = args[0].getClass().getName();
            if (!this.httpHandlerClassNameFilter.filter(httpHandlerClassName)) {
                return false;
            }

            if (!(args[1] instanceof HttpServerExchange)) {
                return false;
            }
            return true;
        }
    }


    /**
     * 采集请求头、响应头、请求url
     *
     * @param request      HttpServerExchange
     * @param spanRecorder 采集器
     * @param statusCode   响应码
     */
    private void headerObtain(HttpServerExchange request, SpanRecorder spanRecorder, int statusCode) {

        // 赋值采样策略
        spanRecorder.recordWebInfoStrategy(traceContext.bodyObtainStrategy());

        // 采集请求方式
        spanRecorder.recordWebInfoRequestMethod(request.getRequestMethod().toString());

        // 采集响应码
        spanRecorder.recordWebInfoStatusCode(statusCode);

        // 采集请求url
        spanRecorder.recordWebInfoRequestUrl(request.getRequestURL());

        HeaderMap requestHeaders = request.getRequestHeaders();
        HeaderMap responseHeaders = request.getResponseHeaders();

        // 遍历请求头
        if (null != requestHeaders) {
            Map<String, HeaderValues> requestHeadersMap = new HashMap<String, HeaderValues>(requestHeaders.size());
            for (HttpString headerName : requestHeaders.getHeaderNames()) {
                HeaderValues headerValues = requestHeaders.get(headerName);
                requestHeadersMap.put(headerName.toString(), headerValues);
            }
            // 采集请求头
            spanRecorder.recordWebInfoRequestHeader(requestHeadersMap);
        }

        // 遍历响应头
        if (null != responseHeaders) {
            Map<String, HeaderValues> responseHeadersMap = new HashMap<String, HeaderValues>(responseHeaders.size());
            for (HttpString headerName : responseHeaders.getHeaderNames()) {
                HeaderValues headerValues = responseHeaders.get(headerName);
                responseHeadersMap.put(headerName.toString(), headerValues);
            }
            // 采集响应头
            spanRecorder.recordWebInfoResponseHeader(responseHeadersMap);
        }

    }

}