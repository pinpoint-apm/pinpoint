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

package com.navercorp.pinpoint.test.plugin.junit5.descriptor;

import com.navercorp.pinpoint.test.junit5.TestContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.UniqueId;

public class PluginJunitTestClassTestDescriptor extends ClassTestDescriptor {

    private TestContext testContext;

    public PluginJunitTestClassTestDescriptor(UniqueId uniqueId, Class<?> testClass, JupiterConfiguration configuration, TestContext testContext) {
        super(uniqueId, testClass, configuration);
        this.testContext = testContext;
    }


    @Override
    public JupiterEngineExecutionContext before(JupiterEngineExecutionContext context) {
        final Thread thread = Thread.currentThread();
        final ClassLoader originalClassLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(testContext.getClassLoader());
            return super.before(context);
        } finally {
            thread.setContextClassLoader(originalClassLoader);
        }
    }

    @Override
    public void after(JupiterEngineExecutionContext context) {
        final Thread thread = Thread.currentThread();
        final ClassLoader originalClassLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(testContext.getClassLoader());
            super.after(context);
        } finally {
            thread.setContextClassLoader(originalClassLoader);
        }
    }
}
