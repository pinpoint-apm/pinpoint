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

package com.navercorp.pinpoint.plugin.hystrix.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;

/**
 * Interceptor for <code>R getFallbackOrThrowException(HystrixEventType, FailureType, String, Exception);</code>
 *
 * @author HyunGil Jeong
 */
public class HystrixCommandGetFallbackOrThrowExceptionArgs4Interceptor extends HystrixCommandGetFallbackOrThrowExceptionInterceptor {

    public HystrixCommandGetFallbackOrThrowExceptionArgs4Interceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected Attributes getAttributes(Object[] args) {
        return new Args4Attributes(args);
    }

    private static class Args4Attributes implements Attributes {

        private final Object failureType;
        private final Object message;
        private final Object exception;

        private Args4Attributes(Object[] args) {
            if (args == null || args.length != 4) {
                failureType = null;
                message = null;
                exception = null;
            } else {
                failureType = args[1];
                message = args[2];
                exception = args[3];
            }
        }

        @Override
        public Object getFailureType() {
            return failureType;
        }

        @Override
        public Object getMessage() {
            return message;
        }

        @Override
        public Object getException() {
            return exception;
        }
    }
}
