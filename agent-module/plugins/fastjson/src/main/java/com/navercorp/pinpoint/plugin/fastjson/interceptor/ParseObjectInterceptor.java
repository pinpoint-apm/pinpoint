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
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.ContentLength;
import com.navercorp.pinpoint.plugin.fastjson.FastjsonConstants;

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
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final ContentLength contentLength;

    /**
     * Instantiates a new Parse object interceptor.
     *
     * @param traceContext the trace context
     * @param descriptor   the descriptor
     */
    public ParseObjectInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.contentLength = newContentLength();
    }

    private ContentLength newContentLength() {
        ContentLength.Builder builder = ContentLength.newBuilder();
        builder.addContentType(String.class);
        builder.addContentType(byte[].class);
        builder.addContentType(char[].class);
        builder.addContentType(InputStream.class);
        return builder.build();
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
            int length = contentLength.getLength(arg);
            if (length != ContentLength.NOT_EXIST) {
                recorder.recordAttribute(FastjsonConstants.ANNOTATION_KEY_JSON_LENGTH, length);
            }

        } finally {
            trace.traceBlockEnd();
        }

    }
}
