/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.test.classloader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TestClassList {

    private List<String> testClassList = new ArrayList<String>();

    public TestClassList() {
        add("com.navercorp.pinpoint.bootstrap.");
        add("com.navercorp.pinpoint.common.");
        add("com.navercorp.pinpoint.thrift.");
        add("com.navercorp.pinpoint.profiler.context.");

        add("com.navercorp.pinpoint.test.MockApplicationContext");
        add("com.navercorp.pinpoint.test.TBaseRecorder");
        add("com.navercorp.pinpoint.test.TBaseRecorderAdaptor");
        add("com.navercorp.pinpoint.test.ListenableDataSender");
        add("com.navercorp.pinpoint.test.ListenableDataSender$Listener");
        add("com.navercorp.pinpoint.test.ResettableServerMetaDataHolder");
        add("com.navercorp.pinpoint.test.junit4.TestContext");

        add("com.navercorp.pinpoint.test.junit4.IsRootSpan");
        add("org.apache.thrift.TBase");
        add("junit.");
        add("org.hamcrest.");
        add("org.junit.");
    }

    private void add(String className) {
        this.testClassList.add(className);
    }

    public List<String> getTestClassList() {
        return testClassList;
    }
}
