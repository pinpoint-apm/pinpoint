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

import java.lang.reflect.InvocationTargetException;

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
     * Gets attribute.
     *
     * @param object     the object
     * @param methodName the method name
     * @return the attribute
     */
    protected <T> T getAttribute(Object object, String methodName) {

        T result = null;
        try {
            result = (T) object.getClass().getMethod(methodName).invoke(object);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Record attributes.
     *
     * @param recorder       the recorder
     * @param loggingMessage the logging message
     */
    protected void recordAttributes(SpanEventRecorder recorder, Object loggingMessage) {

        String id = getAttribute(loggingMessage, "getId");

        if (id != null) {
            recorder.recordAttribute(CxfPluginConstants.CXF_LOG_ID, id);
        }

        StringBuilder address = getAttribute(loggingMessage, "getAddress");

        if (address != null && address.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_ADDRESS, address.toString());
        }

        StringBuilder responseCode = getAttribute(loggingMessage, "getResponseCode");

        if (responseCode != null && responseCode.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_RESPONSE_CODE, responseCode.toString());
        }

        StringBuilder encoding = getAttribute(loggingMessage, "getEncoding");

        if (encoding != null && encoding.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_ENCODING, encoding.toString());
        }

        StringBuilder httpMethod = getAttribute(loggingMessage, "getHttpMethod");

        if (httpMethod != null && httpMethod.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_HTTP_METHOD, httpMethod.toString());
        }

        StringBuilder contentType = getAttribute(loggingMessage, "getContentType");

        if (contentType != null && contentType.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_CONTENT_TYPE, contentType.toString());
        }

        StringBuilder header = getAttribute(loggingMessage, "getHeader");

        if (header != null && header.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_HEADERS, header.toString());
        }

        StringBuilder message = getAttribute(loggingMessage, "getMessage");

        if (message != null && message.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_MESSAGES, message.toString());
        }

        StringBuilder payload = getAttribute(loggingMessage, "getPayload");

        if (payload != null && payload.length() > 0) {
            recorder.recordAttribute(CxfPluginConstants.CXF_PAYLOAD, payload.toString());
        }

    }

}