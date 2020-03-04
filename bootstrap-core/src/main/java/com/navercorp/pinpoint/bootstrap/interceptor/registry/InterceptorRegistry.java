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


/**
 * @author emeroad
 */
public final class InterceptorRegistry {

    private static final Locker LOCK = new DefaultLocker();

    private static InterceptorRegistryAdaptor REGISTRY = EmptyRegistryAdaptor.EMPTY;

    public static void bind(final InterceptorRegistryAdaptor interceptorRegistryAdaptor, final Object lock) {
        if (interceptorRegistryAdaptor == null) {
            throw new NullPointerException("interceptorRegistryAdaptor");
        }
        
        if (LOCK.lock(lock)) {
            REGISTRY = interceptorRegistryAdaptor;
        } else {
            throw new IllegalStateException("bind failed. lock=" + lock + " current=" + LOCK.getLock());
        }
    }

    public static void unbind(final Object lock) {
        if (LOCK.unlock(lock)) {
            REGISTRY = EmptyRegistryAdaptor.EMPTY;
        } else {
            throw new IllegalStateException("unbind failed. lock=" + lock + " current=" + LOCK.getLock());
        }
    }

    public static Interceptor getInterceptor(int key) {
        return REGISTRY.getInterceptor(key);
    }
}
