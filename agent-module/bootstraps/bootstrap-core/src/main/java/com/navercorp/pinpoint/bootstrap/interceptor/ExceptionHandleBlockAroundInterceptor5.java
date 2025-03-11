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

public class ExceptionHandleBlockAroundInterceptor5 implements BlockAroundInterceptor5 {

    private final BlockAroundInterceptor5 delegate;
    private final ExceptionHandler exceptionHandler;

    public ExceptionHandleBlockAroundInterceptor5(BlockAroundInterceptor5 delegate, ExceptionHandler exceptionHandler) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler");
    }

    @Override
    public TraceBlock before(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
        try {
            return delegate.before(target, arg0, arg1, arg2, arg3, arg4);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }

        return null;
    }

    @Override
    public void after(TraceBlock block, Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object result, Throwable throwable) {
        try {
            delegate.after(block, target, arg0, arg1, arg2, arg3, arg4, result, throwable);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }
}