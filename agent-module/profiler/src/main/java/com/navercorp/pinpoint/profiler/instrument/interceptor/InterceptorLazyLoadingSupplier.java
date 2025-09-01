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

package com.navercorp.pinpoint.profiler.instrument.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.EmptyInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.profiler.instrument.ScopeInfo;
import com.navercorp.pinpoint.profiler.interceptor.factory.InterceptorFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class InterceptorLazyLoadingSupplier implements Supplier<Interceptor> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final InterceptorFactory factory;
    private final Class<?> interceptorClass;
    private final Object[] providedArguments;
    private final ScopeInfo scopeInfo;
    private final MethodDescriptor methodDescriptor;

    public InterceptorLazyLoadingSupplier(InterceptorFactory factory, Class<?> interceptorClass, Object[] providedArguments, ScopeInfo scopeInfo, MethodDescriptor methodDescriptor) {
        this.factory = factory;
        this.interceptorClass = interceptorClass;
        this.providedArguments = providedArguments;
        this.scopeInfo = scopeInfo;
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public Interceptor get() {
        Interceptor interceptor = null;
        try {
            interceptor = factory.newInterceptor(interceptorClass, providedArguments, scopeInfo, methodDescriptor);
        } catch (Throwable t) {
            if (methodDescriptor != null) {
                logger.warn("Failed to new interceptor, interceptor={}, method={}", interceptorClass.getName(), methodDescriptor, t);
            } else {
                logger.warn("Failed to new interceptor, interceptor={}", interceptorClass.getName(), t);
            }
        }

        if (interceptor == null) {
            // defense
            return EmptyInterceptor.empty();
        }
        return interceptor;

    }
}
