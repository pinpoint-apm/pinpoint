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

package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.tomcat.TomcatServletRequestListener;
import org.apache.catalina.core.StandardContext;

import javax.servlet.ServletRequestListener;

/**
 * @author jaehong.kim
 */
public class StandardContextListenerStartInterceptor implements AroundInterceptor {
    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private ServletRequestListener servletRequestListener;

    public StandardContextListenerStartInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
        this.servletRequestListener = new TomcatServletRequestListener(this.traceContext);
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        try {
            if (target instanceof StandardContext) {
                final StandardContext standardContext = (StandardContext) target;
                // For compatibility with 6.x tomcat
                final Object[] originalApplicationEventListeners = standardContext.getApplicationEventListeners();
                final Object[] copyApplicationEventListeners = originalApplicationEventListeners != null ? new Object[originalApplicationEventListeners.length + 1] : new Object[1];
                copyApplicationEventListeners[0] = this.servletRequestListener;
                if (originalApplicationEventListeners != null) {
                    System.arraycopy(originalApplicationEventListeners, 0, copyApplicationEventListeners, 1, originalApplicationEventListeners.length);
                }
                standardContext.setApplicationEventListeners(copyApplicationEventListeners);
                if (isDebug) {
                    logger.debug("Set application event listeners. applicationEventListeners={}", copyApplicationEventListeners);
                }
            } else {
                logger.info("Failed to add application event listener. The target object is not an org.apache.catalina.core.StandardContext implementation. target={}", target);
            }
        } catch (Exception e) {
            logger.info("Failed to add application event listener. target={}, servletRequestListener={}", target, this.servletRequestListener, e);
        }
    }
}