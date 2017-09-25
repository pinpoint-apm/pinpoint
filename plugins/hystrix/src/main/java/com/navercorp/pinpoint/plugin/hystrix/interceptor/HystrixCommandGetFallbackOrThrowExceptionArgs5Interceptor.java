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
 * Interceptor for <code>R getFallbackOrThrowException(AbstractCommand, HystrixEventType, FailureType, String, Exception);</code>
 *
 * @author HyunGil Jeong
 */
public class HystrixCommandGetFallbackOrThrowExceptionArgs5Interceptor extends HystrixCommandGetFallbackOrThrowExceptionInterceptor {

    public HystrixCommandGetFallbackOrThrowExceptionArgs5Interceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected Attributes getAttributes(Object[] args) {
        return new Args5Attributes(args);
    }

    private static class Args5Attributes implements Attributes {

        private final Object failureType;
        private final Object message;
        private final Object exception;

        private Args5Attributes(Object[] args) {
            if (args == null || args.length != 5) {
                failureType = null;
                message = null;
                exception = null;
            } else {
                failureType = args[2];
                message = args[3];
                exception = args[4];
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
