/**
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
package com.navercorp.pinpoint.plugin.gson.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.gson.GsonPlugin;
import com.navercorp.pinpoint.plugin.gson.filter.GsonMethodNames;

import java.lang.reflect.Type;

/**
 * Gson method interceptor
 *
 * @author ChaYoung You
 */
public class GsonMethodInterceptor implements SimpleAroundInterceptor {
    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    public GsonMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (logger.isDebugEnabled()) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        trace.traceBlockBegin();
        trace.markBeforeTime();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            trace.recordServiceType(GsonPlugin.GSON_SERVICE_TYPE);
            trace.recordApi(descriptor);
            trace.recordException(throwable);
            if (descriptor.getMethodName().equals(GsonMethodNames.FROM_JSON)) {
                if (args.length >= 1 && args[0] instanceof String) {
                    trace.recordAttribute(GsonPlugin.GSON_ANNOTATION_KEY_JSON_LENGTH, ((String) args[0]).length());
                }
            } else if (descriptor.getMethodName().equals(GsonMethodNames.TO_JSON)) {
                if (args.length == 1 || args.length == 2 && args[1] instanceof Type) {
                    trace.recordAttribute(GsonPlugin.GSON_ANNOTATION_KEY_JSON_LENGTH, ((String) result).length());
                }
            }
            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }
}
