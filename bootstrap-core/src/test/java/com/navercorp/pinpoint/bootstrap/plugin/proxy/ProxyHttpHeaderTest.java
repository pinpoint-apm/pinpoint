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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class ProxyHttpHeaderTest {

    @Test
    public void operations() throws Exception {
        ProxyHttpHeader header = new ProxyHttpHeader();

        assertFalse(header.isValid());

        final long currentTimeMillis = System.currentTimeMillis();
        header.setName(1);
        header.setApp("testapp");
        header.setBusyPercent((byte) 99);
        header.setIdlePercent((byte) 1);
        header.setDurationTimeMicroseconds(12345);
        header.setReceivedTimeMillis(currentTimeMillis);
        header.setValid(true);

        assertEquals(1, header.getName());
        assertEquals("testapp", header.getApp());
        assertEquals(99, header.getBusyPercent());
        assertEquals(1, header.getIdlePercent());
        assertEquals(12345, header.getDurationTimeMicroseconds());
        assertEquals(currentTimeMillis, header.getReceivedTimeMillis());
        assertTrue(header.isValid());
    }
}