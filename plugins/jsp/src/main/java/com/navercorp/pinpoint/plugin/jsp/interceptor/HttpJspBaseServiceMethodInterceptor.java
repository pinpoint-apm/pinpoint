/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jsp.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.jsp.JspConstants;

import java.util.regex.Pattern;

/**
 * @author jaehong.kim
 */
public class HttpJspBaseServiceMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    private static final String JSP_PREFIX = "org.apache.jsp.";
    private static final int JSP_PREFIX_LENGTH = JSP_PREFIX.length();
    private static final String WEB_INF = "WEB-INF";
    private static final String WEB_INF_ENCODE = "WEB_002dINF";
    private static final String JSP = ".jsp";
    private static final String JSP_ENCODE = "_jsp";

    public HttpJspBaseServiceMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        if (target != null) {
            final String jspPath = parseJspName(target.getClass().getName());
            if (jspPath != null && !jspPath.isEmpty()) {
                recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, jspPath);
            }
        }
    }

    String parseJspName(final String className) {
        if (className == null || className.isEmpty()) {
            return className;
        }

        // org.apache.jsp.
        String path = parsePath(className);
        // WEB-INF
        path = decode(path, WEB_INF_ENCODE, WEB_INF, false);
        // .jsp
        path = decode(path, JSP_ENCODE, JSP, true);
        return path;
    }

    private String parsePath(final String text) {
        if (text.startsWith(JSP_PREFIX)) {
            return text.substring(JSP_PREFIX_LENGTH).replace('.', '/');
        }

        int lastPeriodIndex = text.lastIndexOf('.');
        if (lastPeriodIndex != -1 && text.length() > lastPeriodIndex) {
            return text.substring(lastPeriodIndex + 1);
        }

        return text;
    }

    private String decode(final String text, final String search, final String replacement, boolean lastIndex) {
        int beginIndex;
        if (lastIndex) {
            beginIndex = text.lastIndexOf(search);
        } else {
            beginIndex = text.indexOf(search);
        }

        if (beginIndex == -1) {
            return text;
        }
        int endIndex = beginIndex + search.length();
        return replace(text, beginIndex, endIndex, replacement);
    }

    private String replace(final String text, final int beginIndex, final int endIndex, final String replacement) {
        final StringBuilder sb = new StringBuilder();
        if (beginIndex > 0) {
            sb.append(text.substring(0, beginIndex));
        }
        sb.append(replacement);
        if (text.length() > endIndex) {
            sb.append(text.substring(endIndex));
        }

        return sb.toString();
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordException(throwable);
        recorder.recordServiceType(JspConstants.SERVICE_TYPE);
        recorder.recordApi(getMethodDescriptor());
    }
}