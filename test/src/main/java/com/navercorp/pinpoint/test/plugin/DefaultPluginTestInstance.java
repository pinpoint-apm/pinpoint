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

import com.navercorp.pinpoint.test.plugin.classloader.PluginTestClassLoader;
import com.navercorp.pinpoint.test.plugin.shared.ThreadFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DefaultPluginTestInstance implements PluginTestInstance {

    private String id;
    private PluginTestClassLoader classLoader;
    private Class<?> testClass;
    private boolean manageTraceObject;
    private PluginTestInstanceCallback callback;
    private ExecutorService executorService;

    public DefaultPluginTestInstance(String id, PluginTestClassLoader classLoader, Class<?> testClass, boolean manageTraceObject, PluginTestInstanceCallback callback) {
        this.id = id;
        this.classLoader = classLoader;
        this.testClass = testClass;
        this.manageTraceObject = manageTraceObject;
        this.callback = callback;

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

    public <T> T execute(final Callable<T> callable, boolean verify) {
        Callable<T> task = new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    callback.before(verify, manageTraceObject);
                    return callable.call();
                } finally {
                    callback.after(verify, manageTraceObject);
                }
            }
        };

        Future<T> future = this.executorService.submit(task);
        try {
            return future.get(30l, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {
        if (this.callback != null) {
            this.callback.clear();
            this.callback = null;
        }
        if (this.classLoader != null) {
            this.classLoader.clear();
            this.classLoader = null;
        }
        if (this.executorService != null) {
            this.executorService.shutdownNow();
        }
    }
}
