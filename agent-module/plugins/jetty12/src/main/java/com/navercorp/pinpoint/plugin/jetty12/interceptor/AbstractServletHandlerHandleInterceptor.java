/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jetty12.interceptor;

import com.navercorp.pinpoint.bootstrap.config.HttpStatusCodeErrors;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptorHelper;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerCookieRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerHeaderRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListener;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListenerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.response.ServletResponseListener;
import com.navercorp.pinpoint.bootstrap.plugin.response.ServletResponseListenerBuilder;
import com.navercorp.pinpoint.plugin.common.servlet.jakarta.util.HttpServletRequestAdaptor;
import com.navercorp.pinpoint.plugin.common.servlet.jakarta.util.HttpServletResponseAdaptor;
import com.navercorp.pinpoint.plugin.common.servlet.jakarta.util.ParameterRecorderFactory;
import com.navercorp.pinpoint.plugin.jetty12.Jetty12Configuration;
import com.navercorp.pinpoint.plugin.jetty12.Jetty12Constants;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Base interceptor for Jetty 12 EE10/EE11 entry point
 * {@code ServletHandler#handle(Request, Response, Callback)}.
 * <p>
 * The entry point is {@code ServletHandler.handle} rather than
 * {@code ServletChannel.handle()} because {@code ServletChannel.handle()} is
 * re-entered during asynchronous completion ({@code AsyncContext.complete()} ->
 * {@code ServletChannelState.complete()} -> {@code runInContext} ->
 * {@code ServletChannel.handle()}). On that re-entry the servlet
 * {@code DispatcherType} is still {@code REQUEST}, so the async-dispatch guard
 * does not suppress it and a second (duplicate) trace creation is attempted on a
 * thread that already holds the async trace, raising
 * {@code "already Trace Object exist"}. {@code ServletHandler.handle} is invoked
 * only on the initial request dispatch and on async re-dispatch (DispatcherType
 * ASYNC, which the guard skips); the completion path does not pass through it.
 * <p>
 * Concrete subclasses extract the request/response from the core
 * {@code Request} argument ({@code args[0]}); the {@code ServletContextRequest}
 * is already populated by the {@code ServletContextHandler} before
 * {@code ServletHandler.handle} runs (independent of {@code ServletChannel.associate}).
 */
public abstract class AbstractServletHandlerHandleInterceptor implements ApiIdAwareAroundInterceptor {
    protected final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    private final boolean isDebug = logger.isDebugEnabled();

    private final ServletRequestListener<HttpServletRequest> servletRequestListener;
    private final ServletResponseListener<HttpServletResponse> servletResponseListener;

    public AbstractServletHandlerHandleInterceptor(TraceContext traceContext, RequestRecorderFactory<HttpServletRequest> requestRecorderFactory) {
        final Jetty12Configuration config = new Jetty12Configuration(traceContext.getProfilerConfig());
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        ParameterRecorder<HttpServletRequest> parameterRecorder = ParameterRecorderFactory.newParameterRecorderFactory(
                config.getExcludeProfileMethodFilter(),
                config.isTraceRequestParam()
        );

        ServletRequestListenerBuilder<HttpServletRequest> reqBuilder = new ServletRequestListenerBuilder<>(Jetty12Constants.JETTY, traceContext, requestAdaptor);
        reqBuilder.setExcludeURLFilter(config.getExcludeUrlFilter());
        reqBuilder.setTraceExcludeMethodFilter(config.getTraceExcludeMethodFilter());
        reqBuilder.setParameterRecorder(parameterRecorder);
        reqBuilder.setRequestRecorderFactory(requestRecorderFactory);

        final ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
        reqBuilder.setRealIpSupport(config.getRealIpHeader(), config.getRealIpEmptyValue());
        reqBuilder.setHttpStatusCodeRecorder(HttpStatusCodeErrors.of(profilerConfig::readString));
        reqBuilder.setServerHeaderRecorder(profilerConfig.readList(ServerHeaderRecorder.CONFIG_KEY_RECORD_REQ_HEADERS));
        reqBuilder.setServerCookieRecorder(profilerConfig.readList(ServerCookieRecorder.CONFIG_KEY_RECORD_REQ_COOKIES));

        this.servletRequestListener = reqBuilder.build();
        this.servletResponseListener = new ServletResponseListenerBuilder<>(traceContext, new HttpServletResponseAdaptor()).build();
    }

    protected abstract HttpServletRequest toHttpServletRequest(Object[] args);

    protected abstract HttpServletResponse toHttpServletResponse(Object[] args);

    @Override
    public void before(Object target, int apiId, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        try {
            final HttpServletRequest request = toHttpServletRequest(args);
            if (request == null) {
                return;
            }
            if (isAsyncDispatch(request)) {
                if (isDebug) {
                    logger.debug("Skip async servlet request event. isAsyncStarted={}, dispatcherType={}", request.isAsyncStarted(), request.getDispatcherType());
                }
                return;
            }
            final MethodDescriptor methodDescriptor = MethodDescriptorHelper.apiId(apiId);
            this.servletRequestListener.initialized(request, Jetty12Constants.JETTY_METHOD, methodDescriptor);
            final HttpServletResponse response = toHttpServletResponse(args);
            this.servletResponseListener.initialized(response, Jetty12Constants.JETTY_METHOD, methodDescriptor);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    @Override
    public void after(Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        try {
            final HttpServletRequest request = toHttpServletRequest(args);
            if (request == null) {
                return;
            }
            if (isAsyncDispatch(request)) {
                if (isDebug) {
                    logger.debug("Skip async servlet request event. isAsyncStarted={}, dispatcherType={}", request.isAsyncStarted(), request.getDispatcherType());
                }
                return;
            }
            final HttpServletResponse response = toHttpServletResponse(args);
            final int statusCode = getStatusCode(response);
            this.servletResponseListener.destroyed(response, throwable, statusCode);
            this.servletRequestListener.destroyed(request, throwable, statusCode);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        }
    }

    private boolean isAsyncDispatch(HttpServletRequest request) {
        final DispatcherType dispatcherType = request.getDispatcherType();
        return dispatcherType == DispatcherType.ASYNC || dispatcherType == DispatcherType.ERROR;
    }

    private int getStatusCode(final HttpServletResponse response) {
        if (response == null) {
            return 0;
        }
        try {
            return response.getStatus();
        } catch (Exception ignored) {
        }
        return 0;
    }
}
