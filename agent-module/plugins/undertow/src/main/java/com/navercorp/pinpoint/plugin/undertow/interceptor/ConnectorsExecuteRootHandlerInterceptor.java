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
import com.navercorp.pinpoint.bootstrap.util.argument.Predicate;
import com.navercorp.pinpoint.bootstrap.util.argument.Validation;
import com.navercorp.pinpoint.bootstrap.util.argument.Validator;
import com.navercorp.pinpoint.plugin.undertow.ParameterRecorderFactory;
import com.navercorp.pinpoint.plugin.undertow.UndertowConfig;
import com.navercorp.pinpoint.plugin.undertow.UndertowConstants;
import com.navercorp.pinpoint.plugin.undertow.UndertowHttpHeaderFilter;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * @author jaehong.kim
 */
public class ConnectorsExecuteRootHandlerInterceptor implements ApiIdAwareAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final Validator validator;
    private final UndertowHttpHeaderFilter httpHeaderFilter;
    private final ServletRequestListener<HttpServerExchange> servletRequestListener;
    private final ServletResponseListener<HttpServerExchange> servletResponseListener;

    public ConnectorsExecuteRootHandlerInterceptor(TraceContext traceContext, RequestRecorderFactory<HttpServerExchange> requestRecorderFactory) {
        final UndertowConfig config = new UndertowConfig(traceContext.getProfilerConfig());

        this.validator = buildValidator(config);

        RequestAdaptor<HttpServerExchange> requestAdaptor = new HttpServerExchangeAdaptor();
        ParameterRecorder<HttpServerExchange> parameterRecorder = ParameterRecorderFactory.newParameterRecorderFactory(config.getExcludeProfileMethodFilter(), config.isTraceRequestParam());

        ServletRequestListenerBuilder<HttpServerExchange> reqBuilder = new ServletRequestListenerBuilder<>(UndertowConstants.UNDERTOW, traceContext, requestAdaptor);
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

        this.servletResponseListener = new ServletResponseListenerBuilder<>(traceContext, new HttpServerExchangeResponseAdaptor()).build();

        this.httpHeaderFilter = new UndertowHttpHeaderFilter(config.isHidePinpointHeader());
    }

    private Validator buildValidator(UndertowConfig config) {
        Validation validation = new Validation(logger);
        validation.addPredicate(handlerPredicate(config.getHttpHandlerClassNameFilter()));
        validation.addArgument(HttpServerExchange.class, 1);
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
            final HttpServerExchange request = (HttpServerExchange) args[1];
            MethodDescriptor methodDescriptor = MethodDescriptorHelper.apiId(apiId);
            this.servletRequestListener.initialized(request, UndertowConstants.UNDERTOW_METHOD, methodDescriptor);
            this.servletResponseListener.initialized(request, UndertowConstants.UNDERTOW_METHOD, methodDescriptor); //must after request listener due to trace block begin
            this.httpHeaderFilter.filter(request);
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
            final HttpServerExchange request = (HttpServerExchange) args[1];
            final int statusCode = getStatusCode(request);
            this.servletResponseListener.destroyed(request, throwable, statusCode); //must before request listener due to trace block ending
            // TODO Get exception. e.g. request.getAttachment(DefaultResponseListener.EXCEPTION)
            this.servletRequestListener.destroyed(request, throwable, statusCode);
        } catch (Throwable t) {
            logger.info("Failed to servlet request event handle.", t);
        }
    }

    private int getStatusCode(final HttpServerExchange response) {
        try {
            return response.getStatusCode();
        } catch (Exception ignored) {
        }
        return 0;
    }

    public Predicate handlerPredicate(Filter<String> httpHandlerClassNameFilter) {
        return new Predicate() {
            private static final int index = 0;
            @Override
            public boolean test(Object[] args) {
                Object arg = args[index];
                if (!(arg instanceof HttpHandler)) {
                    return false;
                }

                final String httpHandlerClassName = arg.getClass().getName();
                if (!httpHandlerClassNameFilter.filter(httpHandlerClassName)) {
                    return false;
                }
                return true;
            }

            @Override
            public int index() {
                return index;
            }
        };
    }
}