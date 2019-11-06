/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;

import java.lang.reflect.Method;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultInterceptorDefinition implements InterceptorDefinition {
    private final Class<? extends Interceptor> baseInterceptorClazz;
    private final Class<? extends Interceptor> interceptorClazz;
    private final InterceptorType interceptorType;
    private final CaptureType captureType;
    private final Method beforeMethod;
    private final Method afterMethod;

    public DefaultInterceptorDefinition(Class<? extends Interceptor> baseInterceptorClazz, Class<? extends Interceptor> interceptorClazz, InterceptorType interceptorType, CaptureType captureType, Method beforeMethod, Method afterMethod) {
        if (baseInterceptorClazz == null) {
            throw new NullPointerException("baseInterceptorClazz");
        }
        if (interceptorClazz == null) {
            throw new NullPointerException("interceptorClazz");
        }
        if (interceptorType == null) {
            throw new NullPointerException("interceptorType");
        }
        if (captureType == null) {
            throw new NullPointerException("captureType");
        }
        this.baseInterceptorClazz = baseInterceptorClazz;
        this.interceptorClazz = interceptorClazz;
        this.interceptorType = interceptorType;
        this.captureType = captureType;
        this.beforeMethod = beforeMethod;
        this.afterMethod = afterMethod;
    }

    @Override
    public Class<? extends Interceptor> getInterceptorBaseClass() {
        return baseInterceptorClazz;
    }


    @Override
    public Class<? extends Interceptor> getInterceptorClass() {
        return interceptorClazz;
    }

    @Override
    public InterceptorType getInterceptorType() {
        return interceptorType;
    }


    @Override
    public CaptureType getCaptureType() {
        return captureType;
    }

    @Override
    public Method getBeforeMethod() {
        return beforeMethod;
    }

    @Override
    public Method getAfterMethod() {
        return afterMethod;
    }


}
