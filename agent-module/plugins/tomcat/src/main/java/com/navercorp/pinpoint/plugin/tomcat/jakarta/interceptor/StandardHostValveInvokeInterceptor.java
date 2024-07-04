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

package com.navercorp.pinpoint.plugin.tomcat.jakarta.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptorHelper;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.AsyncListenerInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerCookieRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerHeaderRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListener;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListenerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.response.ServletResponseListener;
import com.navercorp.pinpoint.bootstrap.plugin.response.ServletResponseListenerBuilder;
import com.navercorp.pinpoint.bootstrap.util.argument.Validation;
import com.navercorp.pinpoint.bootstrap.util.argument.Validator;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.common.servlet.jakarta.util.HttpServletRequestAdaptor;
import com.navercorp.pinpoint.plugin.common.servlet.jakarta.util.HttpServletResponseAdaptor;
import com.navercorp.pinpoint.plugin.common.servlet.jakarta.util.ParameterRecorderFactory;
import com.navercorp.pinpoint.plugin.tomcat.TomcatConfig;
import com.navercorp.pinpoint.plugin.tomcat.TomcatConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Response;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class StandardHostValveInvokeInterceptor implements ApiIdAwareAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final Validator validator;

    private final ServletRequestListener<HttpServletRequest> servletRequestListener;
    private final ServletResponseListener<HttpServletResponse> servletResponseListener;

    private final TraceContext traceContext;
    private final boolean uriStatEnable;
    private final boolean uriStatUseUserInput;
    private final boolean uriStatCollectMethod;

    public StandardHostValveInvokeInterceptor(TraceContext traceContext,
                                              RequestRecorderFactory<HttpServletRequest> requestRecorderFactory) {
        this.traceContext = traceContext;

        this.validator = buildValidator();
        final TomcatConfig config = new TomcatConfig(traceContext.getProfilerConfig());

        this.uriStatEnable = config.isUriStatEnable();
        this.uriStatUseUserInput = config.isUriStatUseUserInput();
        this.uriStatCollectMethod = config.isUriStatCollectMethod();

        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        ParameterRecorder<HttpServletRequest> parameterRecorder = ParameterRecorderFactory.newParameterRecorderFactory(config.getExcludeProfileMethodFilter(), config.isTraceRequestParam());

        ServletRequestListenerBuilder<HttpServletRequest> reqBuilder = new ServletRequestListenerBuilder<>(TomcatConstants.TOMCAT, traceContext, requestAdaptor);
        reqBuilder.setExcludeURLFilter(config.getExcludeUrlFilter());
        reqBuilder.setTraceExcludeMethodFilter(config.getTraceExcludeMethodFilter());
        reqBuilder.setParameterRecorder(parameterRecorder);
        reqBuilder.setRequestRecorderFactory(requestRecorderFactory);

        final ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
        reqBuilder.setRealIpSupport(config.getRealIpHeader(), config.getRealIpEmptyValue());
        reqBuilder.setHttpStatusCodeRecorder(profilerConfig.getHttpStatusCodeErrors());
        reqBuilder.setServerHeaderRecorder(profilerConfig.readList(ServerHeaderRecorder.CONFIG_KEY_RECORD_REQ_HEADERS));
        reqBuilder.setServerCookieRecorder(profilerConfig.readList(ServerCookieRecorder.CONFIG_KEY_RECORD_REQ_COOKIES));

        this.servletRequestListener = reqBuilder.build();

        this.servletResponseListener = new ServletResponseListenerBuilder<>(traceContext, new HttpServletResponseAdaptor()).build();
    }

    private Validator buildValidator() {
        Validation validation = new Validation(logger);
        validation.addArgument(HttpServletRequest.class, 0);
        validation.addArgument(HttpServletResponse.class, 1);
        return validation.build();
    }

    @Override
    public void before(Object target, int apiId, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!validator.validate(args)) {
            return;
        }

        try {
            final HttpServletRequest request = (HttpServletRequest) args[0];
            final AsyncListenerInterceptor asyncListenerInterceptor = (AsyncListenerInterceptor) request.getAttribute(TomcatConstants.TOMCAT_SERVLET_REQUEST_TRACE);
            if (asyncListenerInterceptor != null) {
                if (isDebug) {
                    logger.debug("Skip async servlet request event.");
                }
                // skip
                return;
            }
            final HttpServletResponse response = (HttpServletResponse) args[1];
            MethodDescriptor methodDescriptor = MethodDescriptorHelper.apiId(apiId);
            this.servletRequestListener.initialized(request, TomcatConstants.TOMCAT_METHOD, methodDescriptor);
            this.servletResponseListener.initialized(response, TomcatConstants.TOMCAT_METHOD, methodDescriptor); //must after request listener due to trace block begin
        } catch (Throwable t) {
            logger.info("Failed to servlet request event handle.", t);
        }
    }

    @Override
    public void after(Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (!validator.validate(args)) {
            return;
        }

        try {
            final HttpServletRequest request = (HttpServletRequest) args[0];
            final HttpServletResponse response = (HttpServletResponse) args[1];
            int statusCode = getStatusCode(response);
            final Throwable t = (Throwable) request.getAttribute(TomcatConstants.JAKARTA_ERROR_EXCEPTION);
            if (t != null) {
                final AsyncListenerInterceptor asyncListenerInterceptor = (AsyncListenerInterceptor) request.getAttribute(TomcatConstants.TOMCAT_SERVLET_REQUEST_TRACE);
                if (asyncListenerInterceptor != null) {
                    // Has an error occurred during async processing that needs to be processed by the application's error page mechanism
                    // Affected version: Tomcat 7, 8, 9, 10. https://bz.apache.org/bugzilla/show_bug.cgi?id=56739
                    asyncListenerInterceptor.complete(t, statusCode);
                    if (isDebug) {
                        logger.debug("Has an error occurred during async processing, asyncListener={}, throwable={}, statusCode={}", asyncListenerInterceptor, t, statusCode);
                    }
                }
            }

            if (isDebug) {
                logger.debug("Raw Request URI: {}", request.getRequestURI());
            }
            if (uriStatEnable && uriStatUseUserInput) {
                recordUserUriTemplate(request);
            }
            this.servletResponseListener.destroyed(response, throwable, statusCode); //must before request listener due to trace block ending
            this.servletRequestListener.destroyed(request, throwable, statusCode);
        } catch (Throwable t) {
            logger.info("Failed to servlet request event handle.", t);
        }
    }

    private void recordUserUriTemplate(HttpServletRequest request) {
        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        final SpanRecorder spanRecorder = trace.getSpanRecorder();
        String uriTemplate = getUserUriTemplate(request);
        if (StringUtils.hasLength(uriTemplate)) {
            spanRecorder.recordUriTemplate(uriTemplate, true);
        }
        if (uriStatCollectMethod) {
            String method = request.getMethod();
            if (StringUtils.hasLength(method)) {
                spanRecorder.recordUriHttpMethod(request.getMethod());
            }
        }
    }

    private String getUserUriTemplate(HttpServletRequest request) {
        for (String attributeName : TomcatConstants.TOMCAT_URI_USER_INPUT_ATTRIBUTE_KEYS) {
            final Object uriMapping = request.getAttribute(attributeName);
            if (!(uriMapping instanceof String)) {
                continue;
            }

            String uriTemplate = (String) uriMapping;
            if (StringUtils.hasLength(uriTemplate)) {
                return uriTemplate;
            }
        }
        return null;
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