/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.test.plugin.shared.ThreadFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DefaultPluginTestInstance implements PluginTestInstance {
    private String id;
    private ClassLoader classLoader;
    private Class<?> testClass;
    private PluginTestVerifier pluginTestVerifier;
    private InterceptorRegistryBinder interceptorRegistryBinder;

    private ExecutorService executorService;

    public DefaultPluginTestInstance(String id, ClassLoader classLoader, Class<?> testClass, PluginTestVerifier pluginTestVerifier, InterceptorRegistryBinder interceptorRegistryBinder) {
        this.id = id;
        this.classLoader = classLoader;
        this.testClass = testClass;
        this.pluginTestVerifier = pluginTestVerifier;
        this.interceptorRegistryBinder = interceptorRegistryBinder;

        final String threadName = id + "-Thread";
        final ThreadFactory threadFactory = new ThreadFactory(threadName, this.classLoader);
        this.executorService = Executors.newSingleThreadExecutor(threadFactory);
    }

    @Override
    public String getTestId() {
        return this.id;
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    @Override
    public Class<?> getTestClass() {
        return this.testClass;
    }

    @Override
    public PluginTestVerifier getPluginVerifier() {
        return this.pluginTestVerifier;
    }

    public InterceptorRegistryBinder getInterceptorRegistryBinder() {
        return interceptorRegistryBinder;
    }

    public <T> T execute(final Callable<T> callable, boolean verify) {
        Callable<T> task = new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    interceptorRegistryBinder.bind();
                    if (verify) {
                        PluginTestVerifierHolder.setInstance(pluginTestVerifier);
                        pluginTestVerifier.initialize(true);
                    }
                    return callable.call();
                } finally {
                    interceptorRegistryBinder.unbind();
                    if (verify) {
                        pluginTestVerifier.cleanUp(true);
                        PluginTestVerifierHolder.setInstance(null);
                    }
                }
            }
        };

        Future<T> future = this.executorService.submit(task);
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
