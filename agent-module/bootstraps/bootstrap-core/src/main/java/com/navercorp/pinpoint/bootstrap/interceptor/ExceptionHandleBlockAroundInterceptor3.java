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

public class ExceptionHandleBlockAroundInterceptor3 implements BlockAroundInterceptor3 {

    private final BlockAroundInterceptor3 delegate;
    private final ExceptionHandler exceptionHandler;

    public ExceptionHandleBlockAroundInterceptor3(BlockAroundInterceptor3 delegate, ExceptionHandler exceptionHandler) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler");
    }

    @Override
    public TraceBlock before(Object target, Object arg0, Object arg1, Object arg2) {
        try {
            return delegate.before(target, arg0, arg1, arg2);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }

        return null;
    }

    @Override
    public void after(TraceBlock block, Object target, Object arg0, Object arg1, Object arg2, Object result, Throwable throwable) {
        try {
            delegate.after(block, target, arg0, arg1, arg2, result, throwable);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }
}