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

package com.navercorp.pinpoint.plugin.tomcat.jakarta;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.AsyncListenerInterceptorHelper;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class TomcatAsyncListenerAdaptor implements AsyncListener {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final AsyncListenerInterceptorHelper delegate;

    public TomcatAsyncListenerAdaptor(AsyncListenerInterceptorHelper delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public void onComplete(AsyncEvent asyncEvent) throws IOException {
        if (isDebug) {
            logger.debug("Complete asynchronous operation. event={}", asyncEvent);
        }

        if (asyncEvent == null) {
            logger.info("Invalid event. event is null");
            return;
        }

        try {
            final int statusCode = getStatusCode(asyncEvent);
            this.delegate.complete(asyncEvent.getThrowable(), statusCode);
        } catch (Throwable t) {
            logger.info("Failed to async event handle. event={}", asyncEvent, t);
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
            this.delegate.timeout(asyncEvent.getThrowable());
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
            logger.info("Invalid event. event is null");
            return;
        }

        try {
            this.delegate.error(asyncEvent.getThrowable());
        } catch (Throwable t) {
            logger.info("Failed to async event handle. event={}", asyncEvent, t);
        }
    }

    @Override
    public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
    }

    private int getStatusCode(final AsyncEvent asyncEvent) {
        try {
            final ServletResponse suppliedResponse = asyncEvent.getSuppliedResponse();
            if (suppliedResponse instanceof HttpServletResponse) {
                return ((HttpServletResponse) suppliedResponse).getStatus();
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}