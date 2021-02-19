/*
 * Copyright 2016 NAVER Corp.
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

/**
 * @author jaehong.kim
 */
public class ExceptionHandleAroundInterceptor2 implements AroundInterceptor2 {

    private final AroundInterceptor2 delegate;
    private final ExceptionHandler exceptionHandler;

    public ExceptionHandleAroundInterceptor2(AroundInterceptor2 delegate, ExceptionHandler exceptionHandler) {
        if (delegate == null) {
            throw new NullPointerException("delegate");
        }
        if (exceptionHandler == null) {
            throw new NullPointerException("exceptionHandler");
        }

        this.delegate = delegate;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void before(Object target, Object arg0, Object arg1) {
        try {
            this.delegate.before(target, arg0, arg1);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public void after(Object target, Object arg0, Object arg1, Object result, Throwable throwable) {
        try {
            this.delegate.after(target, arg0, arg1, result, throwable);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }
}