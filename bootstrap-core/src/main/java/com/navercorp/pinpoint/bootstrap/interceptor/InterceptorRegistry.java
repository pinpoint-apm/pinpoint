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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author emeroad
 */
public final class InterceptorRegistry {

    private static final LoggingInterceptor LOGGING_INTERCEPTOR = new LoggingInterceptor("com.navercorp.pinpoint.profiler.interceptor.LOGGING_INTERCEPTOR");

    public static final InterceptorRegistry REGISTRY = new InterceptorRegistry();

    private final static int DEFAULT_MAX = 4096;
    private final int registrySize;

    private final AtomicInteger id = new AtomicInteger(0);

    private final WeakAtomicReferenceArray<StaticAroundInterceptor> staticIndex;
    private final WeakAtomicReferenceArray<SimpleAroundInterceptor> simpleIndex;

//    private final ConcurrentMap<String, Integer> nameIndex = new ConcurrentHashMap<String, Integer>();

    public InterceptorRegistry() {
        this(DEFAULT_MAX);
    }

    InterceptorRegistry(int maxRegistrySize) {
        if (maxRegistrySize < 0) {
            throw new IllegalArgumentException("negative maxRegistrySize:" + maxRegistrySize);
        }
        this.registrySize = maxRegistrySize;
        this.staticIndex = new WeakAtomicReferenceArray<StaticAroundInterceptor>(maxRegistrySize, StaticAroundInterceptor.class);
        this.simpleIndex = new WeakAtomicReferenceArray<SimpleAroundInterceptor>(maxRegistrySize, SimpleAroundInterceptor.class);
    }


    public int addStaticInterceptor(StaticAroundInterceptor interceptor) {
        if (interceptor == null) {
            return -1;
        }
        return addInterceptor(interceptor, staticIndex);
    }

    private <T extends Interceptor> int addInterceptor(T interceptor, WeakAtomicReferenceArray<T> index) {
        final int newId = nextId();
        if (newId >= registrySize) {
            throw new IndexOutOfBoundsException("size=" + index.length() + " id=" + id);
        }
        index.set(newId, interceptor);
        return newId;
    }

    private int nextId() {
        return id.getAndIncrement();
    }

    int addSimpleInterceptor0(SimpleAroundInterceptor interceptor) {
        if (interceptor == null) {
            return -1;
        }
        final int newId = nextId();
        if (newId >= registrySize) {
            throw new IndexOutOfBoundsException("size=" + staticIndex.length() + " id=" + id);
        }

        this.simpleIndex.set(newId, interceptor);
        return newId;
    }

    public StaticAroundInterceptor getInterceptor0(int key) {
        final StaticAroundInterceptor interceptor = staticIndex.get(key);
        if (interceptor == null) {
            // return LOGGING_INTERCEPTOR upon wrong logic
            return LOGGING_INTERCEPTOR;
        }
        return interceptor;
    }

    public Interceptor findInterceptor0(int key) {
        final SimpleAroundInterceptor simpleInterceptor = this.simpleIndex.get(key);
        if (simpleInterceptor != null) {
            return simpleInterceptor;
        }
        final StaticAroundInterceptor staticAroundInterceptor = this.staticIndex.get(key);
        if (staticAroundInterceptor != null) {
            return staticAroundInterceptor;
        }
        Logger logger = Logger.getLogger(InterceptorRegistry.class.getName());
        if (logger.isLoggable(Level.WARNING)) {
            logger.warning("interceptor not found. id:" + key);
        }
        return LOGGING_INTERCEPTOR;
    }

    SimpleAroundInterceptor getSimpleInterceptor0(int key) {
        final SimpleAroundInterceptor interceptor = simpleIndex.get(key);
        if (interceptor == null) {
            // return LOGGING_INTERCEPTOR upon wrong logic
            return LOGGING_INTERCEPTOR;
        }
        return interceptor;
    }


    public static int addInterceptor(StaticAroundInterceptor interceptor) {
        return REGISTRY.addStaticInterceptor(interceptor);
    }

    public static StaticAroundInterceptor getInterceptor(int key) {
        return REGISTRY.getInterceptor0(key);
    }

    public static Interceptor findInterceptor(int key) {
        return REGISTRY.findInterceptor0(key);
    }

    public static int addSimpleInterceptor(SimpleAroundInterceptor interceptor) {
        return REGISTRY.addSimpleInterceptor0(interceptor);
    }


    public static SimpleAroundInterceptor getSimpleInterceptor(int key) {
        return REGISTRY.getSimpleInterceptor0(key);
    }

}
