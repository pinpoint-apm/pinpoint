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

package com.navercorp.pinpoint.plugin.weblogic.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.weblogic.WeblogicServletRequestListener;
import weblogic.servlet.internal.WebAppServletContext;

import javax.servlet.ServletRequestListener;

/**
 * @author jaehong.kim
 */

public class HttpServerLoadWebAppInterceptor implements AroundInterceptor {
    protected PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isTrace = logger.isTraceEnabled();

    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;
    private final ServletRequestListener servletRequestListener;

    public HttpServerLoadWebAppInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;
        this.servletRequestListener = new WeblogicServletRequestListener(traceContext);
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        try {
            if (result instanceof WebAppServletContext) {
                final WebAppServletContext webAppServletContext = (WebAppServletContext) result;
                webAppServletContext.addListener(this.servletRequestListener);
                if (isDebug) {
                    logger.debug("Add servlet request listener {}", this.servletRequestListener);
                }
            } else {
                logger.info("Failed to add servlet request listener. The target object is not an org.apache.catalina.core.StandardContext implementation. target={}", target);
            }
        } catch (Exception e) {
            logger.info("Failed to add servlet request listener. target={}, servletRequestListener={}", target, this.servletRequestListener, e);
        }
    }
}