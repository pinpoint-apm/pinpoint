/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.junit4;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TestClassWrapper {

    public static final String DEFAULT_CONFIG_PATH = "pinpoint.config";
    
    private final Class<?> testClass;

    public TestClassWrapper(Class<?> testClass) {
        this.testClass = testClass;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public String getConfigPath() {
        final JunitAgentConfigPath annotation = testClass.getAnnotation(JunitAgentConfigPath.class);
        if (annotation == null) {
            return DEFAULT_CONFIG_PATH;
        }
        return annotation.value();
    }
}
