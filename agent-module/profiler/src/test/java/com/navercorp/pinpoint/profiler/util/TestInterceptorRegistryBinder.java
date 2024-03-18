/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.bootstrap.interceptor.registry.DefaultInterceptorRegistryAdaptor;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistry;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistryAdaptor;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;

/**
 * // TODO duplicate class : com.navercorp.pinpoint.test.TestInterceptorRegistryBinder
 * @author emeroad
 */
public class TestInterceptorRegistryBinder implements InterceptorRegistryBinder {
    private static final InterceptorRegistryAdaptor interceptorRegistryAdaptor = new DefaultInterceptorRegistryAdaptor();
    private static final Object lock = new Object();

    @Override
    public void bind() {
        try {
            InterceptorRegistry.bind(interceptorRegistryAdaptor, lock);
        } catch (IllegalStateException e) {
            // ignore
        }
    }

    @Override
    public void unbind() {
        try {
            InterceptorRegistry.unbind(lock);
        } catch (IllegalStateException e) {
            // ignore
        }
    }

    public InterceptorRegistryAdaptor getInterceptorRegistryAdaptor() {
        return interceptorRegistryAdaptor;
    }

    @Override
    public String getInterceptorRegistryClassName() {
        return InterceptorRegistry.class.getName();
    }
}
