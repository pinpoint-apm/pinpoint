/*
 * *
 *  * Copyright 2014 NAVER Corp.
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.navercorp.pinpoint.profiler.instrument.interceptor;

import java.lang.reflect.Method;

/**
 * @author Woonduk Kang(emeroad)
 */
public class InterceptorDefinition {
    private final Class<?> interceptorClazz;
    private final InterceptorType interceptorType;
    private final CaptureType captureType;
    private final Method beforeMethod;
    private final Method afterMethod;

    public InterceptorDefinition(Class<?> interceptorClazz, InterceptorType interceptorType, CaptureType captureType, Method beforeMethod, Method afterMethod) {
        if (interceptorClazz == null) {
            throw new NullPointerException("interceptorClazz must not be null");
        }
        if (interceptorType == null) {
            throw new NullPointerException("interceptorType must not be null");
        }
        if (captureType == null) {
            throw new NullPointerException("captureType must not be null");
        }
        this.interceptorClazz = interceptorClazz;
        this.interceptorType = interceptorType;
        this.captureType = captureType;
        this.beforeMethod = beforeMethod;
        this.afterMethod = afterMethod;
    }

    public Class<?> getInterceptorClazz() {
        return interceptorClazz;
    }

    public InterceptorType getInterceptorType() {
        return interceptorType;
    }


    public CaptureType getCaptureType() {
        return captureType;
    }

    public Method getBeforeMethod() {
        return beforeMethod;
    }

    public Method getAfterMethod() {
        return afterMethod;
    }


}
