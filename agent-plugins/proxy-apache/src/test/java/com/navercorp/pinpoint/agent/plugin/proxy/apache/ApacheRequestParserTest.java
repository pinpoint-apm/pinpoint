/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.agent.plugin.proxy.apache;

import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestHeader;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class ApacheRequestParserTest {
    @Test
    public void parseApacheHttpd() throws Exception {
        ApacheRequestParser parser = new ApacheRequestParser();
        final long currentTimeMillis = System.currentTimeMillis();
        String value = "t=" + currentTimeMillis + "999" + " D=12345 i=99 b=1";
        ProxyRequestHeader proxyHttpHeader = parser.parse(value);
        assertTrue(proxyHttpHeader.isValid());
        assertEquals(currentTimeMillis, proxyHttpHeader.getReceivedTimeMillis());
        assertEquals(12345, proxyHttpHeader.getDurationTimeMicroseconds());
        assertEquals(99, proxyHttpHeader.getIdlePercent());
        assertEquals(1, proxyHttpHeader.getBusyPercent());
    }

    @Test
    public void parseApacheHttpdOnlyReceivedTime() throws Exception {
        ApacheRequestParser parser = new ApacheRequestParser();
        final long currentTimeMillis = System.currentTimeMillis();
        String value = "t=" + currentTimeMillis + "999";
        ProxyRequestHeader proxyHttpHeader = parser.parse(value);
        assertTrue(proxyHttpHeader.isValid());
        assertEquals(currentTimeMillis, proxyHttpHeader.getReceivedTimeMillis());
        assertEquals(-1, proxyHttpHeader.getDurationTimeMicroseconds());
        assertEquals(-1, proxyHttpHeader.getIdlePercent());
        assertEquals(-1, proxyHttpHeader.getBusyPercent());
    }

    @Test
    public void parseApacheHttpdOnlyDurationTime() throws Exception {
        ApacheRequestParser parser = new ApacheRequestParser();
        final long currentTimeMillis = System.currentTimeMillis();
        String value = " D=12345";
        ProxyRequestHeader proxyHttpHeader = parser.parse(value);
        assertFalse(proxyHttpHeader.isValid());
    }

    @Test
    public void parseApacheHttpdOnlyIdle() throws Exception {
        ApacheRequestParser parser = new ApacheRequestParser();
        String value = "i=99";
        ProxyRequestHeader proxyHttpHeader = parser.parse(value);
        assertFalse(proxyHttpHeader.isValid());
    }

    @Test
    public void parseApacheHttpdOnlyBusy() throws Exception {
        ApacheRequestParser parser = new ApacheRequestParser();
        String value = "b=1";
        ProxyRequestHeader proxyHttpHeader = parser.parse(value);
        assertFalse(proxyHttpHeader.isValid());
    }

    @Test
    public void parseApacheHttpdTooShotReceivedTime() throws Exception {
        ApacheRequestParser parser = new ApacheRequestParser();
        String value = "t=99" + " D=12345 i=99 b=1";
        ProxyRequestHeader proxyHttpHeader = parser.parse(value);
        assertFalse(proxyHttpHeader.isValid());
    }
}