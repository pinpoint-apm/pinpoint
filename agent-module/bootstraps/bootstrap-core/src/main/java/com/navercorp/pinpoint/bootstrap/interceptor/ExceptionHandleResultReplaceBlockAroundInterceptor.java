/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceBlock;

import java.util.Objects;

public class ExceptionHandleResultReplaceBlockAroundInterceptor implements ResultReplaceBlockAroundInterceptor {

    private final ResultReplaceBlockAroundInterceptor delegate;
    private final ExceptionHandler exceptionHandler;

    public ExceptionHandleResultReplaceBlockAroundInterceptor(ResultReplaceBlockAroundInterceptor delegate, ExceptionHandler exceptionHandler) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler");
    }

    @Override
    public TraceBlock before(Object target, Class<?> returnType, Object[] args) {
        try {
            return this.delegate.before(target, returnType, args);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }

        return null;
    }

    @Override
    public Object after(TraceBlock block, Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        try {
            return this.delegate.after(block, target, returnType, args, result, throwable);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            // keep the original return value when the delegate fails.
            return result;
        }
    }
}
