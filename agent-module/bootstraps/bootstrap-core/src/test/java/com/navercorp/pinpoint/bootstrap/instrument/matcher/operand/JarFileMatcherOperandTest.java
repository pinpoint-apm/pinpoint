/*
 * Copyright 2021 NAVER Corp.
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
 */

package com.navercorp.pinpoint.bootstrap.instrument.matcher.operand;

import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class JarFileMatcherOperandTest {

    @Test
    public void matchAntStyle() throws Exception {
        final URL codeSourceLoaction = new URL("file::/local/pinpoint/libs/test-core-3.15.jar");
        JarFileMatcherOperand operand = new JarFileMatcherOperand("antstyle:test-core-3.??");
        assertTrue(operand.match(codeSourceLoaction));
    }

    @Test
    public void matchAntStyleFail() throws Exception {
        final URL codeSourceLoaction = new URL("file::/local/pinpoint/libs/test-core-4.1.jar");
        JarFileMatcherOperand operand = new JarFileMatcherOperand("antstyle:test-core-3.??");
        assertFalse(operand.match(codeSourceLoaction));
    }

    @Test
    public void matchRegex() throws Exception {
        final URL codeSourceLoaction = new URL("file::/local/pinpoint/libs/test-util-3.15.jar");

        JarFileMatcherOperand operand = new JarFileMatcherOperand("regex:test-util-3\\D?\\d{2}");
        assertTrue(operand.match(codeSourceLoaction));
    }

    @Test
    public void matchRegexFail() throws Exception {
        final URL codeSourceLoaction = new URL("file::/local/pinpoint/libs/test-util-4.15.jar");

        JarFileMatcherOperand operand = new JarFileMatcherOperand("regex:test-util-3\\D?\\d{2}");
        assertFalse(operand.match(codeSourceLoaction));
    }
}