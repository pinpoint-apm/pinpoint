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

package com.navercorp.pinpoint.plugin.jetty;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.AsyncListenerInterceptorHelper;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author jaehong.kim
 */
public class JettyAsyncListener implements AsyncListener {
    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private boolean isDebug = logger.isDebugEnabled();
    private final boolean isInfo = logger.isInfoEnabled();

    private final AsyncListenerInterceptorHelper asyncListenerInterceptorHelper;

    public JettyAsyncListener(final TraceContext traceContext, final AsyncContext asyncContext) {
        this.asyncListenerInterceptorHelper = new AsyncListenerInterceptorHelper(traceContext, asyncContext);
    }

    @Override
    public void onComplete(AsyncEvent asyncEvent) throws IOException {
        if (isDebug) {
            logger.debug("Complete asynchronous operation. event={}", asyncEvent);
        }

        if (asyncEvent == null) {
            if (isInfo) {
                logger.info("Invalid event. event is null");
            }
            return;
        }

        try {
            final int statusCode = getStatusCode(asyncEvent);
            final Throwable throwable = getThrowable(asyncEvent);
            this.asyncListenerInterceptorHelper.complete(throwable, statusCode);
        } catch (Throwable t) {
            if (isInfo) {
                logger.info("Failed to async event handle. asyncEvent={}", asyncEvent, t);
            }
        }
    }

    @Override
    public void onTimeout(AsyncEvent asyncEvent) throws IOException {
        if (isDebug) {
            logger.debug("Timeout asynchronous operation. event={}", asyncEvent);
        }

        if (asyncEvent == null) {
            if (isDebug) {
                logger.debug("Invalid event. event is null");
            }
            return;
        }

        try {
            this.asyncListenerInterceptorHelper.timeout(asyncEvent.getThrowable());
        } catch (Throwable t) {
            logger.info("Failed to async event handle. event={}", asyncEvent, t);
        }
    }

    @Override
    public void onError(AsyncEvent asyncEvent) throws IOException {
        if (isDebug) {
            logger.debug("Error asynchronous operation. event={}", asyncEvent);
        }

        if (asyncEvent == null) {
            if (isInfo) {
                logger.info("Invalid event. event is null");
            }
            return;
        }

        try {
            final Throwable throwable = getThrowable(asyncEvent);
            this.asyncListenerInterceptorHelper.error(throwable);
        } catch (Throwable t) {
            if (isInfo) {
                logger.info("Failed to async event handle. asyncEvent={}", asyncEvent, t);
            }
        }
    }

    @Override
    public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
    }

    private int getStatusCode(AsyncEvent asyncEvent) {
        try {
            if (asyncEvent.getSuppliedResponse() instanceof HttpServletResponse) {
                return ((HttpServletResponse) asyncEvent.getSuppliedResponse()).getStatus();
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private Throwable getThrowable(AsyncEvent asyncEvent) {
        try {
            if (asyncEvent.getThrowable() != null) {
                return asyncEvent.getThrowable();
            }
            // Jetty 8.x
            final ServletRequest request = asyncEvent.getSuppliedRequest();
            if (request != null) {
                final Object errorException = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
                if (errorException instanceof Throwable) {
                    return (Throwable) errorException;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}