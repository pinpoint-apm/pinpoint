/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.fastjson.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.fastjson.FastjsonConstants;

import java.io.IOException;
import java.io.InputStream;

/**
 * The type Parse object interceptor.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/07/17
 */
public class ParseObjectInterceptor implements AroundInterceptor {

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    /**
     * Instantiates a new Parse object interceptor.
     *
     * @param traceContext the trace context
     * @param descriptor   the descriptor
     */
    public ParseObjectInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
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
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordServiceType(FastjsonConstants.SERVICE_TYPE);
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);

            Object arg = ArrayUtils.get(args, 0);
            if (arg != null) {
                if (arg instanceof String) {
                    recorder.recordAttribute(FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH, ((String) arg).length());
                } else if (arg instanceof byte[]) {
                    recorder.recordAttribute(FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH, ((byte[]) arg).length);
                } else if (arg instanceof char[]) {
                    recorder.recordAttribute(FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH, ((char[]) arg).length);
                } else if (arg instanceof InputStream) {
                    recorder.recordAttribute(FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH, ((InputStream) arg).available());
                }
            }
        } catch (IOException ignore) {
            // ignore
        } finally {
            trace.traceBlockEnd();
        }

    }
}
