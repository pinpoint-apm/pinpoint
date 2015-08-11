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

package com.navercorp.pinpoint.bootstrap.interceptor;

/**
 * @author emeroad
 */
public final class InterceptorRegistry {

    private static final Locker LOCK = new DefaultLocker();

    private static InterceptorRegistryAdaptor REGISTRY;

    public static void bind(final InterceptorRegistryAdaptor interceptorRegistryAdaptor, final Object lock) {
        if (interceptorRegistryAdaptor == null) {
            throw new NullPointerException("interceptorRegistryAdaptor must not be null");
        }
        
        System.out.println("try to set interceptorRegistryAdator: " + interceptorRegistryAdaptor);
        
        if (LOCK.lock(lock)) {
            REGISTRY = interceptorRegistryAdaptor;
            System.out.println("set interceptorRegistryAdator: " + interceptorRegistryAdaptor);
        } else {
            throw new IllegalStateException("bind failed.");
        }
    }

    public static void unbind(final Object lock) {
        System.out.println("try to unset interceptorRegistryAdator: " + REGISTRY);
        
        if (LOCK.unlock(lock)) {
            System.out.println("unset interceptorRegistryAdator: " + REGISTRY);
            REGISTRY = null;
        } else {
            throw new IllegalStateException("unbind failed.");
        }
    }



    public static StaticAroundInterceptor getStaticInterceptor(int key) {
        return REGISTRY.getStaticInterceptor(key);
    }

    public static Interceptor findInterceptor(int key) {
        return REGISTRY.findInterceptor(key);
    }


    public static SimpleAroundInterceptor getSimpleInterceptor(int key) {
        return REGISTRY.getSimpleInterceptor(key);
    }

}
