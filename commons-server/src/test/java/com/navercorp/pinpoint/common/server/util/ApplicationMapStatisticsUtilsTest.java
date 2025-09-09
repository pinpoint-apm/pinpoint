/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApplicationMapStatisticsUtilsTest {


    @Test
    public void testMakeColumnName() {
        final byte[] columnNameBytes = ApplicationMapStatisticsUtils.makeColumnName("test", (short) 10);
        short slotNumber = ByteArrayUtils.bytesToShort(columnNameBytes, 0);
        Assertions.assertEquals(10, slotNumber);

        String columnName = BytesUtils.toString(columnNameBytes, BytesUtils.SHORT_BYTE_LENGTH, columnNameBytes.length - BytesUtils.SHORT_BYTE_LENGTH);
        Assertions.assertEquals("test", columnName);

    }

    @Test
    public void testMakeColumnName2() {
//        short serviceType, String applicationName, String destHost, short slotNumber
        final short slotNumber = 10;
        final byte[] columnNameBytes = ApplicationMapStatisticsUtils.makeColumnName(ServiceType.STAND_ALONE.getCode(), "applicationName", "dest", slotNumber);
        Buffer buffer = new FixedBuffer(columnNameBytes);
        Assertions.assertEquals(ServiceType.STAND_ALONE.getCode(), buffer.readShort());
        Assertions.assertEquals(10, buffer.readShort());
        Assertions.assertEquals("applicationName", buffer.read2PrefixedString());

        int offset = buffer.getOffset();
        byte[] interBuffer = buffer.getInternalBuffer();
        Assertions.assertEquals("dest", BytesUtils.toString(interBuffer, offset, interBuffer.length - offset));

    }
}

