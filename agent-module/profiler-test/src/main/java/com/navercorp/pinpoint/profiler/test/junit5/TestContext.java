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

package com.navercorp.pinpoint.profiler.test.junit5;

import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLoggerBinder;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.logging.Log4j2Binder;
import com.navercorp.pinpoint.profiler.test.MockApplicationContextFactory;
import com.navercorp.pinpoint.profiler.test.classloader.TestClassLoader;
import com.navercorp.pinpoint.profiler.test.classloader.TestClassLoaderFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

/**
 * @author hyungil.jeong
 * @author emeroad
 */
public class TestContext implements Closeable {

    private static final String BASE_TEST_CLASS_NAME = "com.navercorp.pinpoint.profiler.test.junit5.BasePinpointTest";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final PluginLoggerBinder loggerBinder = new Log4j2Binder(LogManager.getContext());
    private final TestClassLoader classLoader;
    private final DefaultApplicationContext mockApplicationContext;
    private final TestClassWrapper testClassWrapper;
    private final Class<?> baseTestClass;


    public TestContext(TestClassWrapper testClassWrapper) {
        this.testClassWrapper = testClassWrapper;
        this.mockApplicationContext = createMockApplicationContext(testClassWrapper.getConfigPath());
        this.mockApplicationContext.start();
        this.classLoader = TestClassLoaderFactory.createTestClassLoader(mockApplicationContext, new URL[0], null);
        this.classLoader.initialize();
        try {
            this.baseTestClass = classLoader.loadClass(BASE_TEST_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    private DefaultApplicationContext createMockApplicationContext(String configPath) {
        logger.trace("agent create");
        MockApplicationContextFactory factory = new MockApplicationContextFactory();
        return factory.build(configPath);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public DefaultApplicationContext getDefaultApplicationContext() {
        return mockApplicationContext;
    }

    public Class<?> createTestClass() {
        try {
            return classLoader.loadClass(testClassWrapper.getTestClass().getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public Class<?> getBaseTestClass() {
        return this.baseTestClass;
    }

    @Override
    public void close() throws IOException {
        if (mockApplicationContext != null) {
            mockApplicationContext.close();
        }
        PluginLogManager.unregister(loggerBinder);
    }
}
