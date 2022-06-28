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
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class ExecuteSharedThread {

    private static final TaggedLogger logger = TestLogger.getLogger();

    private final Thread thread;
    private final ExecuteSharedThreadRunnable runnable;
    private final String testClazzName;
    private final ClassLoader testClassLoader;

    private final Properties properties = new Properties();

    private final CountDownLatch beforeCompleteLatch = new CountDownLatch(1);
    private final CountDownLatch afterStartLatch = new CountDownLatch(1);

    public ExecuteSharedThread(String testClazzName, ClassLoader testClassLoader) {
        this.testClazzName = Objects.requireNonNull(testClazzName, "testClazzName");
        this.testClassLoader = Objects.requireNonNull(testClassLoader, "testClassLoader");

        this.runnable = new ExecuteSharedThreadRunnable();

        thread = new Thread(runnable);
        thread.setName(testClazzName + "-Shared-Thread");
        thread.setContextClassLoader(testClassLoader);
        thread.setDaemon(true);
    }

    void startBefore() {
        thread.start();
    }

    public SharedTestLifeCycleWrapper getSharedClassWrapper() {
        return runnable.sharedTestLifeCycleWrapper;
    }

    public Throwable getRunnableError() {
        return runnable.throwable;
    }

    boolean awaitBeforeCompleted(long timeout, TimeUnit unit) {
        if (!thread.isAlive()) {
            throw new IllegalStateException("Thread is not alive.");
        }

        try {
            return beforeCompleteLatch.await(timeout, unit);
        } catch (InterruptedException e) {
            logger.warn("awaitBeforeCompleted() interrupted. message:{}", e.getMessage());
        }
        return false;
    }

    private void setBeforeCompleted() {
        beforeCompleteLatch.countDown();
    }

    void startAfter() {
        if (!thread.isAlive()) {
            throw new IllegalStateException("Thread is not alive.");
        }

        long count = afterStartLatch.getCount();
        if (count == 0) {
            logger.info("startAfter() already executed.");
        } else {
            afterStart();
        }
    }

    private void afterStart() {
        afterStartLatch.countDown();
    }

    private boolean awaitAfterStart() {
        try {
            afterStartLatch.await();
            return true;
        } catch (InterruptedException e) {
            logger.error(e, "awaitAfterStart interrupted. message:{}", e.getMessage());
        }
        return false;
    }

    Properties getProperties() {
        if (beforeCompleteLatch.getCount() == 0) {
            return properties;
        } else {
            throw new IllegalStateException("SharedBeforeClass is not completed.");
        }
    }

    boolean join(long millis) {
        try {
            thread.join(millis);
            return true;
        } catch (InterruptedException e) {
            logger.warn("join() interrupted. message:{}", e.getMessage());
        }
        return false;
    }

    private class ExecuteSharedThreadRunnable implements Runnable {
        private volatile SharedTestLifeCycleWrapper sharedTestLifeCycleWrapper;
        private volatile Throwable throwable;
        @Override
        public void run() {
            try {
                Class<?> testClazz = loadClass();

                logger.debug("Execute testClazz:{} cl:{}", testClazz.getName(), testClazz.getClassLoader());

                sharedTestLifeCycleWrapper = SharedTestLifeCycleWrapper.newVersionTestLifeCycleWrapper(testClazz);
                if (sharedTestLifeCycleWrapper != null) {
                    sharedTestLifeCycleWrapper.beforeAll();
                }
            } catch (Throwable th) {
                logger.warn("{} testclass error", testClazzName, th);
                throwable = th;
            } finally {
                setBeforeCompleted();
            }

            awaitAfterStart();
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

}
