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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    private final String testClazzName;
    private final ClassLoader testClassLoader;

    private final Properties properties = new Properties();

    private final CountDownLatch beforeCompleteLatch = new CountDownLatch(1);
    private final CountDownLatch afterStartLatch = new CountDownLatch(1);

    public ExecuteSharedThread(String testClazzName, ClassLoader testClassLoader) {
        this.testClazzName = Objects.requireNonNull(testClazzName, "testClazzName");
        this.testClassLoader = Objects.requireNonNull(testClassLoader, "testClassLoader");

        thread = new Thread(new ExecuteSharedThreadRunnable());
        thread.setName(testClazzName + "-Shared-Thread");
        thread.setContextClassLoader(testClassLoader);
        thread.setDaemon(true);
    }

    void startBefore() {
        thread.start();
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

        @Override
        public void run() {
            final Class<?> testClazz;
            try {
                testClazz = loadClass();

                logger.debug("Execute testClazz:{} cl:{}", testClazz.getName(), testClazz.getClassLoader());

                runBeforeSharedClass(testClazz);
                Map<String, Object> result = getProperties(testClazz);
                if (result.size() > 0) {
                    properties.putAll(result);
                }
            } finally {
                setBeforeCompleted();
            }

            awaitAfterStart();
            runAfterSharedClass(testClazz);
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

    private void runBeforeSharedClass(final Class<?> testClazz) {
        try {
            MethodFilter beforeSharedMethodFilter = MethodUtils.createBeforeSharedMethodFilter();
            List<Method> beforeSharedMethods = MethodUtils.getMethod(testClazz, beforeSharedMethodFilter);
            MethodUtils.invokeStaticAndNoParametersMethod(beforeSharedMethods);
        } catch (Exception e) {
            logger.error(e, "execute beforeSharedClass failed. testClazz:{}", testClazzName);
        }
    }

    private Map<String, Object> getProperties(final Class<?> testClazz) {
        try {
            return MethodUtils.invokeGetMethod(testClazz);
        } catch (Exception e) {
            logger.error(e, "invokeGetMethod execute failed. message:{}", e.getMessage());
        }

        return Collections.emptyMap();
    }

    private void runAfterSharedClass(final Class<?> testClazz) {
        try {
            MethodFilter afterSharedMethodFilter = MethodUtils.createAfterSharedMethodFilter();
            List<Method> afterSharedMethods = MethodUtils.getMethod(testClazz, afterSharedMethodFilter);
            MethodUtils.invokeStaticAndNoParametersMethod(afterSharedMethods);
        } catch (Exception e) {
            logger.error(e, "execute afterSharedClass failed. testClazz:{}", testClazzName);
        }

    }


}
