/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

public class ExceptionHandleBlockApiIdAwareAroundInterceptor implements BlockApiIdAwareAroundInterceptor {

    private final BlockApiIdAwareAroundInterceptor delegate;
    private final ExceptionHandler exceptionHandler;

    public ExceptionHandleBlockApiIdAwareAroundInterceptor(BlockApiIdAwareAroundInterceptor delegate, ExceptionHandler exceptionHandler) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler");
    }

    @Override
    public TraceBlock before(Object target, int apiId, Object[] args) {
        try {
            return delegate.before(target, apiId, args);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }

        return null;
    }

    @Override
    public void after(TraceBlock block, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        try {
            delegate.after(block, target, apiId, args, result, throwable);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }
}