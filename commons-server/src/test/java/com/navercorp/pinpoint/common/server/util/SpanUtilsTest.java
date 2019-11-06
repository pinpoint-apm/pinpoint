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

package com.navercorp.pinpoint.common.server.util;

import java.util.Arrays;

import com.google.common.primitives.Longs;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.thrift.dto.TSpan;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class SpanUtilsTest {
    @Test
    public void testGetTraceIndexRowKeyWhiteSpace() {
        String applicationName = "test test";
        long time = System.currentTimeMillis();
        check(applicationName, time);
    }

    @Test
    public void testGetTraceIndexRowKey1() {
        String applicationName = "test";
        long time = System.currentTimeMillis();
        check(applicationName, time);
    }

    @Test
    public void testGetTraceIndexRowKey2() {
        String applicationName = "";
        for (int i = 0; i < PinpointConstants.APPLICATION_NAME_MAX_LEN; i++) {
            applicationName += "1";
        }

        long time = System.currentTimeMillis();
        check(applicationName, time);
    }

    @Test
    public void testGetTraceIndexRowKey3() {
        String applicationName = "";
        for (int i = 0; i < PinpointConstants.APPLICATION_NAME_MAX_LEN + 1; i++) {
            applicationName += "1";
        }

        long time = System.currentTimeMillis();
        try {
            check(applicationName, time);
            Assert.fail("error");
        } catch (IndexOutOfBoundsException ignore) {
        }
    }

    private void check(String applicationName, long l1) {
        TSpan span = new TSpan();
        span.setApplicationName(applicationName);
        span.setStartTime(l1);

        byte[] traceIndexRowKey = SpanUtils.getApplicationTraceIndexRowKey(span.getApplicationName(), span.getStartTime());

        String agentId = BytesUtils.toString(traceIndexRowKey, 0, PinpointConstants.APPLICATION_NAME_MAX_LEN).trim();
        Assert.assertEquals(applicationName, agentId);

        long time = Longs.fromByteArray(Arrays.copyOfRange(traceIndexRowKey, PinpointConstants.APPLICATION_NAME_MAX_LEN, PinpointConstants.APPLICATION_NAME_MAX_LEN + 8));
        time = TimeUtils.recoveryTimeMillis(time);
        Assert.assertEquals(time, l1);
    }
}
