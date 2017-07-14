/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.hystrix.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.hystrix.HystrixPluginConstants;

import java.lang.reflect.Method;

/**
 * @author HyunGil Jeong
 */
public class FallbackObservableCallInterceptor extends HystrixObservableCallInterceptor {

    public FallbackObservableCallInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        super.doInAfterTrace(recorder, target, args, result, throwable);
        Object cause = getCause(target);
        if (cause != null) {
            recorder.recordAttribute(HystrixPluginConstants.HYSTRIX_FALLBACK_CAUSE_ANNOTATION_KEY, cause.toString());
        }
    }

    @Override
    protected String getExecutionType() {
        return HystrixPluginConstants.EXECUTION_TYPE_FALLBACK;
    }

    private Object getCause(Object target) {
        Object enclosingInstance = getEnclosingInstance(target);
        if (enclosingInstance == null) {
            return null;
        }
        Method getExecutionExceptionMethod;
        try {
            final Class<?> clazz = enclosingInstance.getClass();
            getExecutionExceptionMethod = clazz.getMethod(HystrixPluginConstants.HYSTRIX_COMMAND_GET_EXECUTION_EXCEPTION_METHOD_NAME);
        } catch (NoSuchMethodException e) {
            // may throw this depending on Hystrix version (it only exists after 1.4.22) - just swalllow
            return null;
        }
        if (getExecutionExceptionMethod != null) {
            try {
                return getExecutionExceptionMethod.invoke(enclosingInstance);
            } catch (Exception e) {
                // simply warn about the exception
                logger.warn(e.getMessage(), e);
                return null;
            }
        } else {
            return null;
        }
    }
}
