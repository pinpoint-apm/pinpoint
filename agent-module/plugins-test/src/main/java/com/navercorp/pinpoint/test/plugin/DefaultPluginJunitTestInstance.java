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

import com.navercorp.pinpoint.bootstrap.logging.PLoggerBinder;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.logging.Log4j2Binder;
import com.navercorp.pinpoint.profiler.test.MockApplicationContextFactory;
import com.navercorp.pinpoint.profiler.test.classloader.TestClassLoader;
import com.navercorp.pinpoint.profiler.test.classloader.TestClassLoaderFactory;
import com.navercorp.pinpoint.profiler.test.junit5.TestClassWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class DefaultPluginJunitTestInstance implements PluginJunitTestInstance {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final PLoggerBinder loggerBinder = new Log4j2Binder(LogManager.getContext());
    private final TestClassLoader classLoader;
    private final DefaultApplicationContext mockApplicationContext;

    private final TestClassWrapper testClassWrapper;

    public DefaultPluginJunitTestInstance(TestClassWrapper testClassWrapper) {
        this.testClassWrapper = testClassWrapper;
        this.mockApplicationContext = createMockApplicationContext(testClassWrapper.getConfigPath());
        this.mockApplicationContext.start();
        this.classLoader = TestClassLoaderFactory.createTestClassLoader(mockApplicationContext, null, null);
        this.classLoader.initialize();
    }

    private DefaultApplicationContext createMockApplicationContext(String configPath) {
        logger.trace("agent create");
        MockApplicationContextFactory factory = new MockApplicationContextFactory();
        return factory.build(configPath);
    }

    @Override
    public DefaultApplicationContext getDefaultApplicationContext() {
        return mockApplicationContext;
    }

    @Override
    public Class<?> getTestClass() {
        try {
            final Class<?> testClazz = classLoader.loadClass(testClassWrapper.getTestClass().getName());
            return testClazz;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public void close() throws IOException {
        if (mockApplicationContext != null) {
            mockApplicationContext.close();
        }
        PLoggerFactory.unregister(loggerBinder);
    }
}
