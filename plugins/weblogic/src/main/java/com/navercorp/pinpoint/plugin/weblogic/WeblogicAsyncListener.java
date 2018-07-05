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

package com.navercorp.pinpoint.plugin.weblogic;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.AsyncListenerInterceptorHelper;
import weblogic.servlet.internal.ServletResponseImpl;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author jaehong.kim
 */
public class WeblogicAsyncListener implements AsyncListener {
    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isInfo = logger.isInfoEnabled();

    private final AsyncListenerInterceptorHelper asyncListenerInterceptorHelper;

    public WeblogicAsyncListener(final TraceContext traceContext, final AsyncContext asyncContext) {
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
            this.asyncListenerInterceptorHelper.complete(asyncEvent.getThrowable(), statusCode);
        } catch (Throwable t) {
            if (isInfo) {
                logger.info("Failed to async event handle. event={}", asyncEvent, t);
            }
        }
    }

    @Override
    public void onTimeout(AsyncEvent asyncEvent) throws IOException {
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
            // TODO Can not get final status
            final int statusCode = getStatusCode(asyncEvent);
            this.asyncListenerInterceptorHelper.error(asyncEvent.getThrowable(), statusCode);
        } catch (Throwable t) {
            if (isInfo) {
                logger.info("Failed to async event handle. event={}", asyncEvent, t);
            }
        }
    }

    @Override
    public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
    }

    private int getStatusCode(final AsyncEvent asyncEvent) {
        try {
            if (asyncEvent.getSuppliedResponse() instanceof HttpServletResponse) {
                return ((HttpServletResponse) asyncEvent.getSuppliedResponse()).getStatus();
            } else if (asyncEvent.getAsyncContext() != null && asyncEvent.getAsyncContext().getResponse() instanceof ServletResponseImpl) {
                return ((ServletResponseImpl) asyncEvent.getAsyncContext().getResponse()).getStatus();
            }
        } catch (Exception ignored) {
            // Expected exception: java.lang.IllegalStateException: [HTTP:101402]Cannot get Request or Response when the current state is completed or dispatched
        }
        return 0;
    }
}