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

package com.navercorp.pinpoint.plugin.tomcat;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.RequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.RemoteAddressResolver;
import com.navercorp.pinpoint.bootstrap.plugin.request.RemoteAddressResolverFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletRequestListenerInterceptorHelper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletServerRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServletServerRequestWrapperFactory;
import com.navercorp.pinpoint.common.util.Assert;
import org.apache.catalina.connector.Request;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jaehong.kim
 */
public class TomcatServletRequestListener implements ServletRequestListener {
    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isInfo = logger.isInfoEnabled();

    private final TraceContext traceContext;
    private final ServletServerRequestWrapperFactory servletServerRequestWrapperFactory;
    private final ServletRequestListenerInterceptorHelper servletRequestListenerInterceptorHelper;

    public TomcatServletRequestListener(final TraceContext traceContext) {
        this.traceContext = Assert.requireNonNull(traceContext, "traceContext must not be null");
        final TomcatConfig config = new TomcatConfig(traceContext.getProfilerConfig());
        final RemoteAddressResolver remoteAddressResolver = RemoteAddressResolverFactory.newRemoteAddressResolver(config.getRealIpHeader(), config.getRealIpEmptyValue());
        this.servletServerRequestWrapperFactory = new ServletServerRequestWrapperFactory(remoteAddressResolver);
        this.servletRequestListenerInterceptorHelper = new ServletRequestListenerInterceptorHelper(traceContext, config.getExcludeUrlFilter(), config.getExcludeProfileMethodFilter(), config.isTraceRequestParam());
    }

    @Override
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        if (isDebug) {
            logger.debug("Request initialized. event={}", servletRequestEvent);
        }

        if (servletRequestEvent == null) {
            if (isInfo) {
                logger.info("Invalid request. event is null");
            }
            return;
        }
        final ServletRequest servletRequest = servletRequestEvent.getServletRequest();
        if (!(servletRequest instanceof HttpServletRequest)) {
            if (isInfo) {
                logger.info("Invalid request. Request must implement the javax.servlet.http.HttpServletRequest interface. event={}, request={}", servletRequestEvent, servletRequest);
            }
            return;
        }

        try {
            final HttpServletRequest request = (HttpServletRequest) servletRequest;
            final ServletServerRequestWrapper serverRequestWrapper = this.servletServerRequestWrapperFactory.get(new RequestWrapper() {
                @Override
                public String getHeader(String name) {
                    return request.getHeader(name);
                }
            }, request.getRequestURI(), request.getServerName(), request.getServerPort(), request.getRemoteAddr(), request.getRequestURL(), request.getMethod(), request.getParameterMap());
            this.servletRequestListenerInterceptorHelper.initialized(serverRequestWrapper, TomcatConstants.TOMCAT);
        } catch (Throwable t) {
            if (isInfo) {
                logger.info("Failed to servlet request event handle. event={}", servletRequestEvent, t);
            }
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
        if (isDebug) {
            logger.debug("Request destroyed. event={}", servletRequestEvent);
        }

        if (servletRequestEvent == null) {
            if (isInfo) {
                logger.info("Invalid request. event is null");
            }
            return;
        }
        final ServletRequest servletRequest = servletRequestEvent.getServletRequest();
        if (!(servletRequest instanceof HttpServletRequest)) {
            if (isInfo) {
                logger.info("Invalid request. Request must implement the javax.servlet.http.HttpServletRequest interface. event={}, request={}", servletRequestEvent, servletRequest);
            }
            return;
        }

        try {

            final HttpServletRequest request = (HttpServletRequest) servletRequest;
            final Trace asyncTrace = (Trace) getTrace(request);
            if (asyncTrace != null) {
                // Check bind asyncTrace
                final Trace currentTrace = this.traceContext.currentRawTraceObject();
                if (currentTrace == null) {
                    // Set continue asyncTrace
                    this.traceContext.continueTraceObject(asyncTrace);
                }
            }
            final Throwable throwable = getException(request);
            int statusCode = getStatusCode(request);
            this.servletRequestListenerInterceptorHelper.destroyed(throwable, statusCode);
        } catch (Throwable t) {
            if (isInfo) {
                logger.info("Failed to servlet request event handle. event={}", servletRequestEvent, t);
            }
        }
    }

    private Throwable getException(ServletRequest request) {
        final Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (exception instanceof Throwable) {
            return (Throwable) exception;
        }
        return null;
    }

    private Object getTrace(ServletRequest request) {
        // Asynchronous operation
        // clear attribute, It is done by Request.recycle() but is defensive.
        final Object trace = request.getAttribute(TomcatConstants.TOMCAT_SERVLET_REQUEST_TRACE);
        if (trace != null) {
            request.removeAttribute(TomcatConstants.TOMCAT_SERVLET_REQUEST_TRACE);
        }
        return trace;
    }

    private int getStatusCode(final ServletRequest httpServletRequest) {
        try {
            if (httpServletRequest instanceof StatusCodeAccessor) {
                // tomcat 6/8/9
                return ((StatusCodeAccessor) httpServletRequest)._$PINPOINT$_getStatusCode();
            }
            if (httpServletRequest instanceof Request) {
                // tomcat 7
                return ((Request) httpServletRequest).getResponse().getStatus();
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}