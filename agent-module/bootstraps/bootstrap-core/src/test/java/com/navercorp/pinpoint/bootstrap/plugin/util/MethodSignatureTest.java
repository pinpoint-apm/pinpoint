/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MethodSignatureTest {

    @Test
    void toStringWithNoParameters() {
        MethodSignature methodSignature = new MethodSignature("test", new String[]{});
        Assertions.assertEquals("test()", methodSignature.toString());
    }

    @Test
    void toStringWithManyParameters() {
        MethodSignature methodSignature = new MethodSignature("test", new String[]{"java.lang.String", "int", "double", "float"});
        Assertions.assertEquals("test(java.lang.String, int, double, float)", methodSignature.toString());
    }
}