/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.proxy;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author jaehong.kim
 */
public class ProxyHttpHeaderParserTest {

    @Test
    public void parseApacheHttpd() throws Exception {
        ProxyHttpHeaderParser parser = new ProxyHttpHeaderParser();
        final long currentTimeMillis = System.currentTimeMillis();
        String value = "t=" + currentTimeMillis + "999" + " D=12345 i=99 b=1";
        ProxyHttpHeader proxyHttpHeader = parser.parseApacheHttpd(value);
        assertTrue(proxyHttpHeader.isValid());
        assertEquals(currentTimeMillis, proxyHttpHeader.getReceivedTimeMillis());
        assertEquals(12345, proxyHttpHeader.getDurationTimeMicroseconds());
        assertEquals(99, proxyHttpHeader.getIdlePercent());
        assertEquals(1, proxyHttpHeader.getBusyPercent());
        assertEquals(AnnotationKey.PROXY_HTTP_HEADER, proxyHttpHeader.getAnnotationKey());
        LongIntIntByteByteStringValue tvalue = (LongIntIntByteByteStringValue) proxyHttpHeader.getAnnotationValue();
        assertEquals(currentTimeMillis, tvalue.getLongValue());
        assertEquals(12345, tvalue.getIntValue2());
        assertEquals(99, tvalue.getByteValue1());
        assertEquals(1, tvalue.getByteValue2());
    }

    @Test
    public void parseApacheHttpdOnlyReceivedTime() throws Exception {
        ProxyHttpHeaderParser parser = new ProxyHttpHeaderParser();
        final long currentTimeMillis = System.currentTimeMillis();
        String value = "t=" + currentTimeMillis + "999";
        ProxyHttpHeader proxyHttpHeader = parser.parseApacheHttpd(value);
        assertTrue(proxyHttpHeader.isValid());
        assertEquals(currentTimeMillis, proxyHttpHeader.getReceivedTimeMillis());
        assertEquals(-1, proxyHttpHeader.getDurationTimeMicroseconds());
        assertEquals(-1, proxyHttpHeader.getIdlePercent());
        assertEquals(-1, proxyHttpHeader.getBusyPercent());
        assertEquals(AnnotationKey.PROXY_HTTP_HEADER, proxyHttpHeader.getAnnotationKey());
        LongIntIntByteByteStringValue tvalue = (LongIntIntByteByteStringValue) proxyHttpHeader.getAnnotationValue();
        assertEquals(currentTimeMillis, tvalue.getLongValue());
        assertEquals(-1, tvalue.getIntValue2());
        assertEquals(-1, tvalue.getByteValue1());
        assertEquals(-1, tvalue.getByteValue2());
    }

    @Test
    public void parseApacheHttpdOnlyDurationTime() throws Exception {
        ProxyHttpHeaderParser parser = new ProxyHttpHeaderParser();
        final long currentTimeMillis = System.currentTimeMillis();
        String value = " D=12345";
        ProxyHttpHeader proxyHttpHeader = parser.parseApacheHttpd(value);
        assertFalse(proxyHttpHeader.isValid());
    }

    @Test
    public void parseApacheHttpdOnlyIdle() throws Exception {
        ProxyHttpHeaderParser parser = new ProxyHttpHeaderParser();
        String value = "i=99";
        ProxyHttpHeader proxyHttpHeader = parser.parseApacheHttpd(value);
        assertFalse(proxyHttpHeader.isValid());
    }

    @Test
    public void parseApacheHttpdOnlyBusy() throws Exception {
        ProxyHttpHeaderParser parser = new ProxyHttpHeaderParser();
        String value = "b=1";
        ProxyHttpHeader proxyHttpHeader = parser.parseApacheHttpd(value);
        assertFalse(proxyHttpHeader.isValid());
    }

    @Test
    public void parseApacheHttpdTooShotReceivedTime() throws Exception {
        ProxyHttpHeaderParser parser = new ProxyHttpHeaderParser();
        String value = "t=99" + " D=12345 i=99 b=1";
        ProxyHttpHeader proxyHttpHeader = parser.parseApacheHttpd(value);
        assertFalse(proxyHttpHeader.isValid());
    }

    @Test
    public void parseNginx() throws Exception {
        ProxyHttpHeaderParser parser = new ProxyHttpHeaderParser();
        String value = "Monday, 17-Jul-2017 05:57:29 GMT";
        ProxyHttpHeader proxyHttpHeader = parser.parseNginx(value);
        assertTrue(proxyHttpHeader.isValid());
        assertEquals(1500271049000L, proxyHttpHeader.getReceivedTimeMillis());
        assertEquals(-1, proxyHttpHeader.getDurationTimeMicroseconds());
        assertEquals(-1, proxyHttpHeader.getIdlePercent());
        assertEquals(-1, proxyHttpHeader.getBusyPercent());
        assertEquals(AnnotationKey.PROXY_HTTP_HEADER, proxyHttpHeader.getAnnotationKey());
        LongIntIntByteByteStringValue tvalue = (LongIntIntByteByteStringValue) proxyHttpHeader.getAnnotationValue();
        assertEquals(1500271049000L, tvalue.getLongValue());
        assertEquals(-1, tvalue.getIntValue2());
        assertEquals(-1, tvalue.getByteValue1());
        assertEquals(-1, tvalue.getByteValue2());
    }

    @Test
    public void parseNginxInvalidValue() throws Exception {
        ProxyHttpHeaderParser parser = new ProxyHttpHeaderParser();
        String value = "Monday, 2017-07-17 05:57:29 GMT";
        ProxyHttpHeader proxyHttpHeader = parser.parseNginx(value);
        assertFalse(proxyHttpHeader.isValid());
    }

    @Test
    public void parseTimestamp() throws Exception {
        ProxyHttpHeaderParser parser = new ProxyHttpHeaderParser();
        final long currentTimeMillis = System.currentTimeMillis();
        String value = String.valueOf(currentTimeMillis);
        ProxyHttpHeader proxyHttpHeader = parser.parseTimestamp(value);
        assertTrue(proxyHttpHeader.isValid());
        assertEquals(currentTimeMillis, proxyHttpHeader.getReceivedTimeMillis());
        assertEquals(-1, proxyHttpHeader.getDurationTimeMicroseconds());
        assertEquals(-1, proxyHttpHeader.getIdlePercent());
        assertEquals(-1, proxyHttpHeader.getBusyPercent());
        assertEquals(AnnotationKey.PROXY_HTTP_HEADER, proxyHttpHeader.getAnnotationKey());
        LongIntIntByteByteStringValue tvalue = (LongIntIntByteByteStringValue) proxyHttpHeader.getAnnotationValue();
        assertEquals(currentTimeMillis, tvalue.getLongValue());
        assertEquals(-1, tvalue.getIntValue2());
        assertEquals(-1, tvalue.getByteValue1());
        assertEquals(-1, tvalue.getByteValue2());
    }

    @Test
    public void parseTimestampInvalidValue() throws Exception {
        ProxyHttpHeaderParser parser = new ProxyHttpHeaderParser();
        final long currentTimeMillis = System.currentTimeMillis();
        String value = String.valueOf(Long.MAX_VALUE) + String.valueOf(Long.MAX_VALUE);
        ProxyHttpHeader proxyHttpHeader = parser.parseTimestamp(value);
        assertFalse(proxyHttpHeader.isValid());
    }

    @Test
    public void parseUnknown() throws Exception {
        ProxyHttpHeaderParser parser = new ProxyHttpHeaderParser();
        String value = "x=" + System.currentTimeMillis() + "999";
        ProxyHttpHeader proxyHttpHeader = parser.parse(value);
        assertFalse(proxyHttpHeader.isValid());
        assertEquals(0, proxyHttpHeader.getReceivedTimeMillis());
        assertEquals(-1, proxyHttpHeader.getDurationTimeMicroseconds());
        assertEquals(-1, proxyHttpHeader.getIdlePercent());
        assertEquals(-1, proxyHttpHeader.getBusyPercent());
        assertEquals(AnnotationKey.PROXY_HTTP_HEADER, proxyHttpHeader.getAnnotationKey());
        LongIntIntByteByteStringValue tvalue = (LongIntIntByteByteStringValue) proxyHttpHeader.getAnnotationValue();
        assertEquals(0, tvalue.getLongValue());
        assertEquals(-1, tvalue.getIntValue2());
        assertEquals(-1, tvalue.getByteValue1());
        assertEquals(-1, tvalue.getByteValue2());
    }
}