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

import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author WonChul Heo(heowc)
 */
public class PluginTestLoggerTest {

    @Test
    public void testIsXXXEnabled() {
        final PluginTestLogger logger = PluginTestLogger.getLogger(PluginTestLoggerTest.class.getName());
        assertThat(logger.isDebugEnabled(), is(true));
        assertThat(logger.isInfoEnabled(), is(true));
        assertThat(logger.isWarnEnabled(), is(true));
    }

    @Test
    public void test() {
        PrintStream out = mock(PrintStream.class);
        PrintStream err = mock(PrintStream.class);
        PluginTestLogger logger = new PluginTestLogger(PluginTestLoggerTest.class.getName(), out, err);

        logger.debug("hello");
        logger.info("naver");
        logger.warn("pinpoint");
        logger.warn("test", new RuntimeException());

        verify(out, times(2)).println(any(String.class));
        verify(err, times(2)).println(any(String.class));
    }
}
