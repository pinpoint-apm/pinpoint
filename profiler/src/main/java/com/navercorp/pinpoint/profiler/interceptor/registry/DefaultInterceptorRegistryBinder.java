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

package com.navercorp.pinpoint.profiler.interceptor.registry;

import com.navercorp.pinpoint.bootstrap.interceptor.registry.DefaultInterceptorRegistryAdaptor;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistry;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistryAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class DefaultInterceptorRegistryBinder implements InterceptorRegistryBinder {

    public final static int DEFAULT_MAX = 8192;

    private static final AtomicInteger LOCK_NUMBER = new AtomicInteger();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String lock = "DefaultRegistry-" + LOCK_NUMBER.getAndIncrement();
    private final InterceptorRegistryAdaptor interceptorRegistryAdaptor;

    public DefaultInterceptorRegistryBinder() {
        this(DEFAULT_MAX);
    }

    public DefaultInterceptorRegistryBinder(int maxRegistrySize) {
        this.interceptorRegistryAdaptor = new DefaultInterceptorRegistryAdaptor(maxRegistrySize);
    }

    @Override
    public void bind() {
        logger.info("bind:{}", lock);
        InterceptorRegistry.bind(interceptorRegistryAdaptor, lock);
    }

    @Override
    public void unbind() {
        logger.info("unbind:{}", lock);
        InterceptorRegistry.unbind(lock);
    }

    public InterceptorRegistryAdaptor getInterceptorRegistryAdaptor() {
        return interceptorRegistryAdaptor;
    }

    @Override
    public String getInterceptorRegistryClassName() {
        return InterceptorRegistry.class.getName();
    }
}
