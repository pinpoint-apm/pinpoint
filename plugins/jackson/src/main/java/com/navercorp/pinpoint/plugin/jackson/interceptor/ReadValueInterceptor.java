/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jackson.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.ContentLength;
import com.navercorp.pinpoint.plugin.jackson.JacksonConstants;

import java.io.File;

/**
 * @see JacksonPlugin#intercept_ObjectMapper(com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext)
 * @author Sungkook Kim
 */
public class ReadValueInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final ContentLength contentLength;

    public ReadValueInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
        this.contentLength = newContentLength();
    }

    private ContentLength newContentLength() {
        ContentLength.Builder builder = ContentLength.newBuilder();
        builder.addContentType(String.class);
        builder.addContentType(byte[].class);
        return builder.build();
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(JacksonConstants.SERVICE_TYPE);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);

        Object arg = ArrayUtils.get(args, 0);
        if (arg instanceof File) {
            // long length
            long length = ((File) arg).length();
            recorder.recordAttribute(JacksonConstants.ANNOTATION_KEY_LENGTH_VALUE, length);
        } else {
            int length = contentLength.getLength(arg);
            if (length != ContentLength.NOT_EXIST) {
                recorder.recordAttribute(JacksonConstants.ANNOTATION_KEY_LENGTH_VALUE, length);
            }
        }
    }

}