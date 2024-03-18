/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.interceptor.factory;

import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandler;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ExceptionHandlerFactory {
    private final boolean exceptionGuard;
    private final ExceptionHandler exceptionHandler;

    public ExceptionHandlerFactory(boolean exceptionGuard) {
        this.exceptionGuard = exceptionGuard;
        this.exceptionHandler = newExceptionHandler(exceptionGuard);
    }

    private ExceptionHandler newExceptionHandler(boolean propagate) {
        if (propagate) {
            return new GuardExceptionHandler();
        } else {
            return new RethrowExceptionHandler();
        }
    }

    public boolean isHandleException() {
        return exceptionGuard;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }
}
