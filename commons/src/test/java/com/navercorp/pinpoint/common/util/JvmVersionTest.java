/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;

import static org.junit.Assert.*;
import static com.navercorp.pinpoint.common.util.JvmVersion.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * @author hyungil.jeong
 */
public class JvmVersionTest {

    @Test
    public void testOnOrAfter() {
        // JDK 5
        assertTrue(JAVA_5.onOrAfter(JAVA_5));
        assertFalse(JAVA_5.onOrAfter(JAVA_6));
        assertFalse(JAVA_5.onOrAfter(JAVA_7));
        assertFalse(JAVA_5.onOrAfter(JAVA_8));
        assertFalse(JAVA_5.onOrAfter(JAVA_9));
        assertFalse(JAVA_5.onOrAfter(JAVA_10));
        assertFalse(JAVA_5.onOrAfter(UNSUPPORTED));
        // JDK 6
        assertTrue(JAVA_6.onOrAfter(JAVA_5));
        assertTrue(JAVA_6.onOrAfter(JAVA_6));
        assertFalse(JAVA_6.onOrAfter(JAVA_7));
        assertFalse(JAVA_6.onOrAfter(JAVA_8));
        assertFalse(JAVA_6.onOrAfter(JAVA_9));
        assertFalse(JAVA_6.onOrAfter(JAVA_10));
        assertFalse(JAVA_6.onOrAfter(UNSUPPORTED));
        // JDK 7
        assertTrue(JAVA_7.onOrAfter(JAVA_5));
        assertTrue(JAVA_7.onOrAfter(JAVA_6));
        assertTrue(JAVA_7.onOrAfter(JAVA_7));
        assertFalse(JAVA_7.onOrAfter(JAVA_8));
        assertFalse(JAVA_7.onOrAfter(JAVA_9));
        assertFalse(JAVA_7.onOrAfter(JAVA_10));
        assertFalse(JAVA_7.onOrAfter(UNSUPPORTED));
        // JDK 8
        assertTrue(JAVA_8.onOrAfter(JAVA_5));
        assertTrue(JAVA_8.onOrAfter(JAVA_6));
        assertTrue(JAVA_8.onOrAfter(JAVA_7));
        assertTrue(JAVA_8.onOrAfter(JAVA_8));
        assertFalse(JAVA_8.onOrAfter(JAVA_9));
        assertFalse(JAVA_8.onOrAfter(JAVA_10));
        assertFalse(JAVA_8.onOrAfter(UNSUPPORTED));
        // JDK 9
        assertTrue(JAVA_9.onOrAfter(JAVA_5));
        assertTrue(JAVA_9.onOrAfter(JAVA_6));
        assertTrue(JAVA_9.onOrAfter(JAVA_7));
        assertTrue(JAVA_9.onOrAfter(JAVA_8));
        assertTrue(JAVA_9.onOrAfter(JAVA_9));
        assertFalse(JAVA_9.onOrAfter(JAVA_10));
        assertFalse(JAVA_9.onOrAfter(UNSUPPORTED));

        assertTrue(JAVA_10.onOrAfter(JAVA_5));
        assertTrue(JAVA_10.onOrAfter(JAVA_6));
        assertTrue(JAVA_10.onOrAfter(JAVA_7));
        assertTrue(JAVA_10.onOrAfter(JAVA_8));
        assertTrue(JAVA_10.onOrAfter(JAVA_9));
        assertTrue(JAVA_10.onOrAfter(JAVA_10));
        assertFalse(JAVA_10.onOrAfter(JAVA_RECENT));
        assertFalse(JAVA_10.onOrAfter(UNSUPPORTED));

        assertTrue(JAVA_RECENT.onOrAfter(JAVA_11));

        // Unsupported
        assertFalse(UNSUPPORTED.onOrAfter(JAVA_5));
        assertFalse(UNSUPPORTED.onOrAfter(JAVA_6));
        assertFalse(UNSUPPORTED.onOrAfter(JAVA_7));
        assertFalse(UNSUPPORTED.onOrAfter(JAVA_8));
        assertFalse(UNSUPPORTED.onOrAfter(JAVA_9));
        assertFalse(UNSUPPORTED.onOrAfter(JAVA_10));
        assertFalse(UNSUPPORTED.onOrAfter(UNSUPPORTED));
    }

    @Test
    public void testGetFromDoubleVersion() {
        // JDK 5
        final JvmVersion java_5 = JvmVersion.getFromVersion(1.5f);
        assertSame(JAVA_5, java_5);
        // JDK 6
        final JvmVersion java_6 = JvmVersion.getFromVersion(1.6f);
        assertSame(JAVA_6, java_6);
        // JDK 7
        final JvmVersion java_7 = JvmVersion.getFromVersion(1.7f);
        assertSame(JAVA_7, java_7);
        // JDK 8
        final JvmVersion java_8 = JvmVersion.getFromVersion(1.8f);
        assertSame(JAVA_8, java_8);
        // JDK 9
        final JvmVersion java_9 = JvmVersion.getFromVersion(9f);
        assertSame(JAVA_9, java_9);
        // JDK 10
        final JvmVersion java_10 = JvmVersion.getFromVersion(10f);
        assertSame(JAVA_10, java_10);
    }

    @Test
    public void testGetFromDoubleVersion_exceptional_case() {
        // Unsupported
        final JvmVersion java_unsupported = JvmVersion.getFromVersion(0.9f);
        assertSame(UNSUPPORTED, java_unsupported);

        // new version
        final JvmVersion java20 = JvmVersion.getFromVersion(20.f);
        assertSame(JAVA_RECENT, java20);
    }

    @Test
    public void testGetFromStringVersion() {
        // JDK 5
        final JvmVersion java_5 = JvmVersion.getFromVersion("1.5");
        assertSame(JAVA_5, java_5);
        // JDK 6
        final JvmVersion java_6 = JvmVersion.getFromVersion("1.6");
        assertSame(JAVA_6, java_6);
        // JDK 7
        final JvmVersion java_7 = JvmVersion.getFromVersion("1.7");
        assertSame(JAVA_7, java_7);
        // JDK 8
        final JvmVersion java_8 = JvmVersion.getFromVersion("1.8");
        assertSame(JAVA_8, java_8);
        // JDK 9
        final JvmVersion java_9 = JvmVersion.getFromVersion("9");
        assertSame(JAVA_9, java_9);
        // JDK 10
        final JvmVersion java_10 = JvmVersion.getFromVersion("10");
        assertSame(JAVA_10, java_10);
        // Unsupported
        final JvmVersion java_unsupported = JvmVersion.getFromVersion("abc");
        assertSame(UNSUPPORTED, java_unsupported);
    }

    @Test
    public void testGetFromClassVersion() {
        // JDK 5
        final JvmVersion java_5 = JvmVersion.getFromClassVersion(49);
        assertSame(JAVA_5, java_5);
        // JDK 6
        final JvmVersion java_6 = JvmVersion.getFromClassVersion(50);
        assertSame(JAVA_6, java_6);
        // JDK 7
        final JvmVersion java_7 = JvmVersion.getFromClassVersion(51);
        assertSame(JAVA_7, java_7);
        // JDK 8
        final JvmVersion java_8 = JvmVersion.getFromClassVersion(52);
        assertSame(JAVA_8, java_8);
        // JDK 9
        final JvmVersion java_9 = JvmVersion.getFromClassVersion(53);
        assertSame(JAVA_9, java_9);
        // JDK 10
        final JvmVersion java_10 = JvmVersion.getFromClassVersion(54);
        assertSame(JAVA_10, java_10);
        // Unsupported
        final JvmVersion java_unsupported = JvmVersion.getFromClassVersion(-1);
        assertSame(UNSUPPORTED, java_unsupported);
    }
}
