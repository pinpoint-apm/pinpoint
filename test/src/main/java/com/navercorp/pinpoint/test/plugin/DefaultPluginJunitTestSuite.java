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

import com.navercorp.pinpoint.profiler.test.junit5.TestClassWrapper;

/**
 * We have referred OrderedThreadPoolExecutor ParentRunner of JUnit.
 *
 * @author Jongho Moon
 * @author Taejin Koo
 */
public class DefaultPluginJunitTestSuite {

    private PluginJunitTestInstance testInstance;

    public DefaultPluginJunitTestSuite(Class<?> testClass) {
        TestClassWrapper testClassWrapper = new TestClassWrapper(testClass);
        this.testInstance = new DefaultPluginJunitTestInstance(testClassWrapper);
    }

    public PluginJunitTestInstance getTestInstance() {
        return this.testInstance;
    }
}
