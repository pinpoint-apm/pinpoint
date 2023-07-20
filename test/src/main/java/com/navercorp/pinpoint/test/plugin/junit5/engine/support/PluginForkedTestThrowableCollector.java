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

package com.navercorp.pinpoint.test.plugin.junit5.engine.support;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import org.opentest4j.TestAbortedException;

import java.util.function.Predicate;

public class PluginForkedTestThrowableCollector extends ThrowableCollector {

    private TestExecutionResult result;

    private static Predicate<? super Throwable> createAbortedExecutionPredicate() {
        Predicate<Throwable> otaPredicate = TestAbortedException.class::isInstance;
        return otaPredicate;
    }

    public PluginForkedTestThrowableCollector() {
        super(createAbortedExecutionPredicate());
    }

    public void setTestExecutionResult(TestExecutionResult result) {
        this.result = result;
    }

    public TestExecutionResult toTestExecutionResult() {
        if (this.result != null) {
            return result;
        }

        return TestExecutionResult.aborted(new Throwable("unknown"));
    }
}
