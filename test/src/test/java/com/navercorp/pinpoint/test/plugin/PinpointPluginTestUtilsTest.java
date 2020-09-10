/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author WonChul Heo(heowc)
 */
public class PinpointPluginTestUtilsTest {

    @Test
    public void testGetTestDescribe() throws NoSuchMethodException {
        assertThat(PinpointPluginTestUtils.getTestDescribe(null), is("Method null"));
        final Method toString = Object.class.getMethod("toString");
        assertThat(PinpointPluginTestUtils.getTestDescribe(toString), is("Method toString(java.lang.Object)"));
        final Method equals = Object.class.getMethod("equals", Object.class);
        assertThat(PinpointPluginTestUtils.getTestDescribe(equals), is("Method equals(java.lang.Object)"));
        final Method hashCode = Object.class.getMethod("hashCode");
        assertThat(PinpointPluginTestUtils.getTestDescribe(hashCode), is("Method hashCode(java.lang.Object)"));
    }
}