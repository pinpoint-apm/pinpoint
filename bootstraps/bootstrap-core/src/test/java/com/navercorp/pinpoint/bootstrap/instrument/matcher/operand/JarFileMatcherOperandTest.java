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

import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class JarFileMatcherOperandTest {

    @Test
    public void matchAntStyle() throws Exception {
        List<String> patternList = Arrays.asList("antstyle:test-core-3.??", "regex:test-util-*");
        final URL codeSourceLoaction = new URL("file::/local/pinpoint/libs/test-core-3.15.jar");

        JarFileMatcherOperand operand = new JarFileMatcherOperand(patternList);
        assertTrue(operand.match(codeSourceLoaction));
    }

    @Test
    public void matchAntStyleFail() throws Exception {
        List<String> patternList = Arrays.asList("antstyle:test-core-3.??", "regex:test-util-*");
        final URL codeSourceLoaction = new URL("file::/local/pinpoint/libs/test-core-4.1.jar");

        JarFileMatcherOperand operand = new JarFileMatcherOperand(patternList);
        assertFalse(operand.match(codeSourceLoaction));
    }

    @Test
    public void matchRegex() throws Exception {
        List<String> patternList = Arrays.asList("regex:test-util-3\\D?\\d{2}");
        final URL codeSourceLoaction = new URL("file::/local/pinpoint/libs/test-util-3.15.jar");

        JarFileMatcherOperand operand = new JarFileMatcherOperand(patternList);
        assertTrue(operand.match(codeSourceLoaction));
    }

    @Test
    public void matchRegexFail() throws Exception {
        List<String> patternList = Arrays.asList("regex:test-util-3\\D?\\d{2}");
        final URL codeSourceLoaction = new URL("file::/local/pinpoint/libs/test-util-4.15.jar");

        JarFileMatcherOperand operand = new JarFileMatcherOperand(patternList);
        assertFalse(operand.match(codeSourceLoaction));
    }
}