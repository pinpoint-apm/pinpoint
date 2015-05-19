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

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class ApplicationMapStatisticsUtilsTest {

    @Test
    public void makeRowKey() {
        String applicationName = "TESTAPP";
        short serviceType = 123;
        long time = System.currentTimeMillis();

        byte[] bytes = ApplicationMapStatisticsUtils.makeRowKey(applicationName, serviceType, time);

        Assert.assertEquals(applicationName, ApplicationMapStatisticsUtils.getApplicationNameFromRowKey(bytes));
        Assert.assertEquals(serviceType, ApplicationMapStatisticsUtils.getApplicationTypeFromRowKey(bytes));
    }

    @Test
    public void testMakeColumnName() throws Exception {
        final byte[] columnNameBytes = ApplicationMapStatisticsUtils.makeColumnName("test", (short) 10);
        short slotNumber = BytesUtils.bytesToShort(columnNameBytes,0);
        Assert.assertEquals(slotNumber, 10);

        String columnName = BytesUtils.toString(columnNameBytes, BytesUtils.SHORT_BYTE_LENGTH, columnNameBytes.length - BytesUtils.SHORT_BYTE_LENGTH);
        Assert.assertEquals(columnName, "test");

    }

    @Test
    public void testMakeColumnName2() {
//        short serviceType, String applicationName, String destHost, short slotNumber
        final short slotNumber = 10;
        final byte[] columnNameBytes = ApplicationMapStatisticsUtils.makeColumnName(ServiceType.STAND_ALONE.getCode(), "applicationName", "dest", slotNumber);
        Buffer buffer = new FixedBuffer(columnNameBytes);
        Assert.assertEquals(ServiceType.STAND_ALONE.getCode(), buffer.readShort());
        Assert.assertEquals(10, buffer.readShort());
        Assert.assertEquals("applicationName", buffer.read2PrefixedString());

        int offset = buffer.getOffset();
        byte[] interBuffer = buffer.getInternalBuffer();
        Assert.assertEquals(BytesUtils.toString(interBuffer, offset, interBuffer.length - offset), "dest");

    }
}

