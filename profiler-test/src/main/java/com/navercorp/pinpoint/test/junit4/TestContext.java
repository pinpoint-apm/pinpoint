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

package com.navercorp.pinpoint.test.junit4;

import java.io.Closeable;
import java.io.IOException;

import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.test.MockApplicationContextFactory;
import com.navercorp.pinpoint.test.classloader.TestClassLoader;
import com.navercorp.pinpoint.test.classloader.TestClassLoaderFactory;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.logging.PLoggerBinder;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.profiler.logging.Slf4jLoggerBinder;

/**
 * @author hyungil.jeong
 * @author emeroad
 */
public class TestContext implements Closeable {

    private static final String BASE_TEST_CLASS_NAME = "com.navercorp.pinpoint.test.junit4.BasePinpointTest";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PLoggerBinder loggerBinder = new Slf4jLoggerBinder();
    private final TestClassLoader classLoader;
    private final DefaultApplicationContext mockApplicationContext;

    private final Class<?> baseTestClass;


    public TestContext(TestClassWrapper testClassWrapper) {
        this.mockApplicationContext = createMockApplicationContext(testClassWrapper.getConfigPath());
        this.mockApplicationContext.start();
        this.classLoader = TestClassLoaderFactory.createTestClassLoader(mockApplicationContext);
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

    public TestClass createTestClass(Class<?> testClass) {
        try {
            final Class<?> testClazz = classLoader.loadClass(testClass.getName());
            return new TestClass(testClazz);
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
        PLoggerFactory.unregister(loggerBinder);
    }
}
