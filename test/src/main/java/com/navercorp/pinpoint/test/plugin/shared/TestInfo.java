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

import java.io.File;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TestInfo {
    private final String testId;
    private final List<File> dependencyFileList;

    public TestInfo(String testId, List<File> dependencyFileList) {
        this.testId = testId;
        this.dependencyFileList = dependencyFileList;
    }

    public String getTestId() {
        return testId;
    }

    public List<File> getDependencyFileList() {
        return dependencyFileList;
    }
}
