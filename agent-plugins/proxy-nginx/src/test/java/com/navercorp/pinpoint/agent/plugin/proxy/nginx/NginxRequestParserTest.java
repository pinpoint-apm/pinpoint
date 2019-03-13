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

package com.navercorp.pinpoint.agent.plugin.proxy.nginx;

import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestHeader;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class NginxRequestParserTest {

    @Test
    public void parseNginx() throws Exception {
        NginxRequestParser parser = new NginxRequestParser();
        String value = "t=1504248328.423 D=0.123";
        ProxyRequestHeader proxyHttpHeader = parser.parse(value);
        assertTrue(proxyHttpHeader.isValid());
        assertEquals(1504248328423L, proxyHttpHeader.getReceivedTimeMillis());
        assertEquals(123000L, proxyHttpHeader.getDurationTimeMicroseconds());
        assertEquals(-1, proxyHttpHeader.getIdlePercent());
        assertEquals(-1, proxyHttpHeader.getBusyPercent());
    }

    @Test
    public void parseNginxMsec() throws Exception {
        NginxRequestParser parser = new NginxRequestParser();
        String value = "t=1504248328.423";
        ProxyRequestHeader proxyHttpHeader = parser.parse(value);
        assertTrue(proxyHttpHeader.isValid());
        assertEquals(1504248328423L, proxyHttpHeader.getReceivedTimeMillis());
        assertEquals(-1, proxyHttpHeader.getDurationTimeMicroseconds());
        assertEquals(-1, proxyHttpHeader.getIdlePercent());
        assertEquals(-1, proxyHttpHeader.getBusyPercent());
    }
}