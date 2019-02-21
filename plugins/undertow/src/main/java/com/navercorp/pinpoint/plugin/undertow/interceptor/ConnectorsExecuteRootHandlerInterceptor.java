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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListenerInterceptorHelper;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.ParameterRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.RemoteAddressResolverFactory;
import com.navercorp.pinpoint.plugin.common.servlet.util.ArgumentValidator;
import com.navercorp.pinpoint.plugin.undertow.ParameterRecorderFactory;
import com.navercorp.pinpoint.plugin.undertow.UndertowConfig;
import com.navercorp.pinpoint.plugin.undertow.UndertowConstants;
import com.navercorp.pinpoint.plugin.undertow.UndertowHttpHeaderFilter;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * @author jaehong.kim
 */
public class ConnectorsExecuteRootHandlerInterceptor implements AroundInterceptor {
    private static final String SERVLET_INITIAL_HANDLER_CLASS_NAME = "io.undertow.servlet.handlers.ServletInitialHandler$1";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isInfo = logger.isInfoEnabled();

    private final MethodDescriptor methodDescriptor;
    private final ArgumentValidator argumentValidator;
    private final UndertowHttpHeaderFilter httpHeaderFilter;
    private final ServletRequestListenerInterceptorHelper<HttpServerExchange> servletRequestListenerInterceptorHelper;

    public ConnectorsExecuteRootHandlerInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.methodDescriptor = descriptor;
        final UndertowConfig config = new UndertowConfig(traceContext.getProfilerConfig());
        this.argumentValidator = new ConnectorsArgumentValidator(config.isDeployServlet());
        RequestAdaptor<HttpServerExchange> requestAdaptor = new HttpServerExchangeAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, config.getRealIpHeader(), config.getRealIpEmptyValue());
        ParameterRecorder<HttpServerExchange> parameterRecorder = ParameterRecorderFactory.newParameterRecorderFactory(config.getExcludeProfileMethodFilter(), config.isTraceRequestParam());
        this.servletRequestListenerInterceptorHelper = new ServletRequestListenerInterceptorHelper<HttpServerExchange>(UndertowConstants.UNDERTOW, traceContext, requestAdaptor, config.getExcludeUrlFilter(), parameterRecorder);
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
            // TODO Get exception. e.g. request.getAttachment(DefaultResponseListener.EXCEPTION)
            this.servletRequestListenerInterceptorHelper.destroyed(request, throwable, statusCode);
        } catch (Throwable t) {
            if (isInfo) {
                logger.info("Failed to servlet request event handle.", t);
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
        private final boolean deployServlet;

        public ConnectorsArgumentValidator(boolean deployServlet) {
            this.deployServlet = deployServlet;
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

            // For servlet deployments, use only the inner class of the ServletInitialHandler class.
            if (deployServlet) {
                // Compares the class name in case the servlet jar file is detached.
                final String httpHandlerClassName = args[0].getClass().getName();
                if (!SERVLET_INITIAL_HANDLER_CLASS_NAME.equals(httpHandlerClassName)) {
                    return false;
                }
            }

            if (!(args[1] instanceof HttpServerExchange)) {
                return false;
            }
            return true;
        }
    }
}