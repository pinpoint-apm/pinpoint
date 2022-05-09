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

package com.navercorp.pinpoint.agent.plugin.proxy.user;

import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserRequestParserTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void parse() {
        UserRequestParser parser = new UserRequestParser();
        String value = "t=1625212448369 D=123";
        ProxyRequestHeader proxyHttpHeader = parser.parseHeader("HEADER_NAME", value);
        assertTrue(proxyHttpHeader.isValid());
        assertEquals(1625212448369L, proxyHttpHeader.getReceivedTimeMillis());
        assertEquals(123L, proxyHttpHeader.getDurationTimeMicroseconds());
        assertEquals("HEADER_NAME", proxyHttpHeader.getApp());
        assertEquals(-1, proxyHttpHeader.getIdlePercent());
        assertEquals(-1, proxyHttpHeader.getBusyPercent());
    }

    @Test
    public void parseOnlyReceivedTime() {
        UserRequestParser parser = new UserRequestParser();
        String value = "t=1625212448369";
        ProxyRequestHeader proxyHttpHeader = parser.parseHeader("HEADER_NAME", value);
        assertTrue(proxyHttpHeader.isValid());
        assertEquals(1625212448369L, proxyHttpHeader.getReceivedTimeMillis());
        assertEquals(-1, proxyHttpHeader.getDurationTimeMicroseconds());
        assertEquals(-1, proxyHttpHeader.getIdlePercent());
        assertEquals(-1, proxyHttpHeader.getBusyPercent());
    }

    @Test
    public void parseNotFoundReceived() {
        UserRequestParser parser = new UserRequestParser();
        String value = "D=123";
        ProxyRequestHeader proxyHttpHeader = parser.parse(value);
        assertFalse(proxyHttpHeader.isValid());
        logger.info(proxyHttpHeader);
    }

    @Test
    public void parseInvalidReceived() {
        UserRequestParser parser = new UserRequestParser();
        String value = "t=1625212448.369";
        ProxyRequestHeader proxyHttpHeader = parser.parse(value);
        assertFalse(proxyHttpHeader.isValid());
        logger.info(proxyHttpHeader);
    }
}