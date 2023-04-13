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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRequestParserTest {

    @Test
    public void parse() {
        UserRequestParser parser = new UserRequestParser();
        String value = "t=1625212448369 D=123";
        ProxyRequestHeader proxyHttpHeader = parser.parseHeader("HEADER_NAME", value);
        assertThat(proxyHttpHeader.isValid()).isTrue();
        assertThat(1625212448369L).isEqualTo(proxyHttpHeader.getReceivedTimeMillis());
        assertThat(123L).isEqualTo(proxyHttpHeader.getDurationTimeMicroseconds());
        assertThat("HEADER_NAME").isEqualTo(proxyHttpHeader.getApp());
        assertThat(-1).isEqualTo(proxyHttpHeader.getIdlePercent());
        assertThat(-1).isEqualTo(proxyHttpHeader.getBusyPercent());
    }

    @Test
    public void parseOnlyReceivedTime() {
        UserRequestParser parser = new UserRequestParser();
        String value = "t=1625212448369";
        ProxyRequestHeader proxyHttpHeader = parser.parseHeader("HEADER_NAME", value);
        assertThat(proxyHttpHeader.isValid()).isTrue();
        assertThat(1625212448369L).isEqualTo(proxyHttpHeader.getReceivedTimeMillis());
        assertThat(-1).isEqualTo(proxyHttpHeader.getDurationTimeMicroseconds());
        assertThat(-1).isEqualTo(proxyHttpHeader.getIdlePercent());
        assertThat(-1).isEqualTo(proxyHttpHeader.getBusyPercent());
    }

    @Test
    public void parseNotFoundReceived() {
        UserRequestParser parser = new UserRequestParser();
        String value = "D=123";
        ProxyRequestHeader proxyHttpHeader = parser.parseHeader("HEADER_NAME", value);
        assertThat(proxyHttpHeader.isValid()).isFalse();
    }

    @Test
    public void parseReceivedSeconds() {
        UserRequestParser parser = new UserRequestParser();
        String value = "t=1625212448.369";
        ProxyRequestHeader proxyHttpHeader = parser.parseHeader("HEADER_NAME", value);
        assertThat(1625212448369L).isEqualTo(proxyHttpHeader.getReceivedTimeMillis());
        assertThat(-1).isEqualTo(proxyHttpHeader.getDurationTimeMicroseconds());
        assertThat(-1).isEqualTo(proxyHttpHeader.getIdlePercent());
        assertThat(-1).isEqualTo(proxyHttpHeader.getBusyPercent());
    }

    @Test
    public void parseInvalidReceived() {
        UserRequestParser parser = new UserRequestParser();
        String value = "t=1625212448:369";
        ProxyRequestHeader proxyHttpHeader = parser.parseHeader("HEADER_NAME", value);
        assertThat(proxyHttpHeader.isValid()).isFalse();
    }
}