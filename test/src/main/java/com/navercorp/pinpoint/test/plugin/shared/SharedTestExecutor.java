/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import com.navercorp.pinpoint.test.plugin.util.TestLogger;

import org.tinylog.TaggedLogger;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Taejin Koo
 */
public class SharedTestExecutor {

    private static final TaggedLogger logger = TestLogger.getLogger();

    private final ExecutorService executor;
    private final String testClazzName;
    private final ClassLoader testClassLoader;

    private volatile SharedTestLifeCycleWrapper sharedTestLifeCycleWrapper;

    public SharedTestExecutor(String testClazzName, ClassLoader testClassLoader) {
        this.testClazzName = Objects.requireNonNull(testClazzName, "testClazzName");
        this.testClassLoader = Objects.requireNonNull(testClassLoader, "testClassLoader");

        ThreadFactory threadFactory = new ThreadFactory(testClazzName + "-Shared-Executor", testClassLoader);
        this.executor = Executors.newSingleThreadExecutor(threadFactory);
    }

    public void startBefore(long timeout, TimeUnit unit) {
        Future<?> future = this.executor.submit(this::beforeAll);
        awaitFuture("startBefore", future, timeout, unit);
    }

    private <V> V awaitFuture(String action, Future<V> future, long timeout, TimeUnit unit) {
        try {
            return future.get(timeout, unit);
        } catch (ExecutionException | InterruptedException e) {
            logger.warn("{} execution error {}", action, testClazzName, e);
            throw new IllegalStateException(action + " execution error "  + testClazzName, e);
        } catch (TimeoutException e) {
            future.cancel(true);
            logger.warn("{} timeout {}", action,  testClazzName);
            throw new IllegalStateException(action + " timeout " + testClazzName);
        }
    }

    public SharedTestLifeCycleWrapper getSharedClassWrapper() {
        return sharedTestLifeCycleWrapper;
    }


    public void startAfter(long timeout, TimeUnit unit) {
        Future<?> future = this.executor.submit(this::afterAll);
        awaitFuture("startAfter", future, timeout, unit);
    }


    private void beforeAll() {
        Class<?> testClazz = loadClass();

        logger.debug("Execute testClazz:{} cl:{}", testClazz.getName(), testClazz.getClassLoader());

        sharedTestLifeCycleWrapper = SharedTestLifeCycleWrapper.newSharedTestLifeCycleWrapper(testClazz);
        if (sharedTestLifeCycleWrapper != null) {
            sharedTestLifeCycleWrapper.beforeAll();
        }
    }

    private void afterAll() {
        if (sharedTestLifeCycleWrapper != null) {
            sharedTestLifeCycleWrapper.afterAll();
        }
    }

    private Class<?> loadClass() {
        try {
            return testClassLoader.loadClass(testClazzName);
        } catch (ClassNotFoundException e) {
            logger.error(e, "testClazz:{} not found", testClazzName);
            throw new RuntimeException(e);
        }
    }
}
