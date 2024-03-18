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

package com.navercorp.pinpoint.agent.plugin.proxy.app;

import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestHeader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author jaehong.kim
 */
public class AppRequestParserTest {

    @Test
    public void parseApp() {
        AppRequestParser parser = new AppRequestParser();
        final long currentTimeMillis = System.currentTimeMillis();
        String value = "t=" + currentTimeMillis;
        ProxyRequestHeader proxyHttpHeader = parser.parseHeader("UNKNOWN", value);
        assertTrue(proxyHttpHeader.isValid());
        assertEquals(currentTimeMillis, proxyHttpHeader.getReceivedTimeMillis());
        assertEquals(-1, proxyHttpHeader.getDurationTimeMicroseconds());
        assertEquals(-1, proxyHttpHeader.getIdlePercent());
        assertEquals(-1, proxyHttpHeader.getBusyPercent());
    }

    @Test
    public void parseAppInvalid() throws Exception {
        AppRequestParser parser = new AppRequestParser();
        final long currentTimeMillis = System.currentTimeMillis();
        String value = "t=" + currentTimeMillis + "app=jndi:xxx";
        ProxyRequestHeader proxyHttpHeader = parser.parseHeader("UNKNOWN", value);
        assertFalse(proxyHttpHeader.isValid());
    }
}