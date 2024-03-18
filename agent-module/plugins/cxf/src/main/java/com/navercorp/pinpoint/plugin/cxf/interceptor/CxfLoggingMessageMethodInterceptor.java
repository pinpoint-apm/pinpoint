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

package com.navercorp.pinpoint.plugin.cxf.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.cxf.CxfPluginConstants;
import org.apache.cxf.interceptor.LoggingMessage;

/**
 * The type Cxf logging message method interceptor.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/09/30
 */
public abstract class CxfLoggingMessageMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    /**
     * Instantiates a new Cxf logging message method interceptor.
     *
     * @param traceContext the trace context
     * @param descriptor   the descriptor
     */
    protected CxfLoggingMessageMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    /**
     * Record attributes.
     *
     * @param recorder       the recorder
     * @param loggingMessage the logging message
     */
    protected void recordAttributes(SpanEventRecorder recorder, LoggingMessage loggingMessage) {

        StringBuilder address = loggingMessage.getAddress();
        if (address.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_ADDRESS, address.toString());
        }

        StringBuilder responseCode = loggingMessage.getResponseCode();
        if (responseCode.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_RESPONSE_CODE, responseCode.toString());
        }

        StringBuilder encoding = loggingMessage.getEncoding();
        if (encoding.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_ENCODING, encoding.toString());
        }

        StringBuilder httpMethod = loggingMessage.getHttpMethod();
        if (httpMethod.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_HTTP_METHOD, httpMethod.toString());
        }

        StringBuilder contentType = loggingMessage.getContentType();
        if (contentType.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_CONTENT_TYPE, contentType.toString());
        }

        StringBuilder header = loggingMessage.getHeader();
        if (header.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_HEADERS, header.toString());
        }

        StringBuilder message = loggingMessage.getMessage();
        if (message.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_MESSAGES, message.toString());
        }

        StringBuilder payload = loggingMessage.getPayload();
        if (payload.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_PAYLOAD, payload.toString());
        }

    }

}