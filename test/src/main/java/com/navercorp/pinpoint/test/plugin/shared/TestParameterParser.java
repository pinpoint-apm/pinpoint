/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TestParameterParser {
    public TestParameterParser() {
    }

    public List<TestParameter> parse(String[] args) {
        if (args == null) {
            return Collections.emptyList();
        }

        final List<TestParameter> testParameters = new ArrayList<TestParameter>();
        for (String arg : args) {
            if (arg == null) {
                continue;
            }

            String[] testArguments = arg.split("=");
            if (testArguments.length != 2) {
                continue;
            }

            final String testId = testArguments[0];
            final String testMavenDependencies = testArguments[1];


            final TestParameter testParameter = new TestParameter(testId, testMavenDependencies);
            testParameters.add(testParameter);
        }
        return testParameters;
    }
}
