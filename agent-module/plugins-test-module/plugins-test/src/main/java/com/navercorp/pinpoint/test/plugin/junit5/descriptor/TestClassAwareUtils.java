/*
 * Copyright 2026 NAVER Corp.
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

import org.junit.jupiter.engine.descriptor.TestClassAware;
import org.junit.platform.engine.TestDescriptor;

import java.util.Objects;

public final class TestClassAwareUtils {

    private TestClassAwareUtils() {
    }

    /**
     * Reads the test class of a descriptor the builders expect to be {@link TestClassAware}.
     * A jupiter upgrade can silently change the descriptor hierarchy, so a mismatch fails
     * with the offending descriptor type instead of a bare {@link ClassCastException}.
     */
    public static Class<?> getTestClass(TestDescriptor testDescriptor) {
        Objects.requireNonNull(testDescriptor, "testDescriptor");
        if (testDescriptor instanceof TestClassAware) {
            return ((TestClassAware) testDescriptor).getTestClass();
        }
        throw new IllegalArgumentException("not a TestClassAware descriptor. type:" + testDescriptor.getClass().getName()
                + ", uniqueId:" + testDescriptor.getUniqueId());
    }
}
