/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;

import java.util.Objects;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public abstract class AbstractSpanEventInterceptorForPlugin {
    protected final PluginLogger logger = PluginLogManager.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected final TraceContext traceContext;

    protected AbstractSpanEventInterceptorForPlugin(TraceContext traceContext) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
    }

    protected Trace currentTrace() {
        return traceContext.currentTraceObject();
    }

    protected void logBeforeInterceptor(Object target, Object[] args) {
        logger.beforeInterceptor(target, args);
    }

    protected void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        logger.afterInterceptor(target, args, result, throwable);
    }

    protected TraceContext getTraceContext() {
        return traceContext;
    }
}