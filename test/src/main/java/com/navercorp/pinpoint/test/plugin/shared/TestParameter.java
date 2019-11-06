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

/**
 * @author Woonduk Kang(emeroad)
 */
public class TestParameter {
    private final String testId;
    private final String testMavenDependencies;

    public TestParameter(String testId, String testMavenDependencies) {
        this.testId = testId;
        this.testMavenDependencies = testMavenDependencies;
    }

    public String getTestId() {
        return testId;
    }

    public String getMavenDependencies() {
        return testMavenDependencies;
    }

    @Override
    public String toString() {
        return "TestParameter{" +
                "testId='" + testId + '\'' +
                ", testMavenDependencies='" + testMavenDependencies + '\'' +
                '}';
    }
}
