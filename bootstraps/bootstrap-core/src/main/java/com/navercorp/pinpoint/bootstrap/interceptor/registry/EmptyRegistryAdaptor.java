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

package com.navercorp.pinpoint.bootstrap.interceptor.registry;

import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.LoggingInterceptor;

/**
 * @author emeroad
 */
public final class EmptyRegistryAdaptor implements InterceptorRegistryAdaptor {

    public static final InterceptorRegistryAdaptor EMPTY = new EmptyRegistryAdaptor();

    private static final LoggingInterceptor LOGGING_INTERCEPTOR = new LoggingInterceptor("com.navercorp.pinpoint.profiler.interceptor.EMPTY");

    public EmptyRegistryAdaptor() {
    }


    @Override
    public int addInterceptor(Interceptor interceptor) {
        return -1;
    }


    public Interceptor getInterceptor(int key) {
        return LOGGING_INTERCEPTOR;
    }
}
