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
import com.navercorp.pinpoint.test.plugin.shared.TestThreadFactory;
import com.navercorp.pinpoint.test.plugin.util.CallExecutable;
import com.navercorp.pinpoint.test.plugin.util.RunExecutable;
import org.junit.platform.commons.JUnitException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DefaultPluginTestInstance implements PluginTestInstance {

    private final String id;
    private PluginTestClassLoader classLoader;
    private final Class<?> testClass;
    private final boolean manageTraceObject;
    private PluginTestInstanceCallback callback;
    private final ExecutorService executorService;

    public DefaultPluginTestInstance(String id, PluginTestClassLoader classLoader, Class<?> testClass, boolean manageTraceObject, PluginTestInstanceCallback callback) {
        this.id = id;
        this.classLoader = classLoader;
        this.testClass = testClass;
        this.manageTraceObject = manageTraceObject;
        this.callback = callback;

        final String threadName = id + "-Thread";
        final ThreadFactory testThreadFactory = new TestThreadFactory(threadName, this.classLoader);
        this.executorService = Executors.newSingleThreadExecutor(testThreadFactory);
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

    public <T> T call(final CallExecutable<T> callable, boolean verify) {
        Callable<T> task = new Callable<T>() {
            @Override
            public T call() {
                try {
                    callback.before(verify, manageTraceObject);
                    return callable.call();
                } finally {
                    callback.after(verify, manageTraceObject);
                }
            }
        };

        Future<T> future = this.executorService.submit(task);
        return await(future);
    }

    public void run(final RunExecutable runnable, boolean verify) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    callback.before(verify, manageTraceObject);
                    runnable.run();
                } finally {
                    callback.after(verify, manageTraceObject);
                }
            }
        };

        Future<?> future = this.executorService.submit(task);
        await(future);
    }

    private <T> T await(Future<T> future) {
        try {
            // Wait for docker image to be ready
            return future.get(300L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JUnitException(this.id + " failed", e);
        } catch (ExecutionException e) {
            throw new JUnitException(this.id + " failed", e);
        } catch (TimeoutException e) {
            // testcase interrupt
            future.cancel(true);
            throw new JUnitException(this.id + " failed", e);
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
            this.executorService.shutdown();
            try {
                if (!this.executorService.awaitTermination(10L, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService did not terminate in the specified time");
                    this.executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
