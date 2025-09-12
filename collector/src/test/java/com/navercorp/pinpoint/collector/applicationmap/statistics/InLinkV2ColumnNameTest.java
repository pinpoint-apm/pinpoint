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

package com.navercorp.pinpoint.collector.applicationmap.statistics;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InLinkV2ColumnNameTest {

    @Test
    void testEquals() {

        ServiceType serviceType = ServiceType.TEST;
        short columnSlotNumber = 2;
        ColumnName name1 = InLinkV2ColumnName.histogram("callerApplicationName", serviceType, "callHost", columnSlotNumber);
        ColumnName name2 = InLinkV2ColumnName.histogram("callerApplicationName", serviceType, "callHost", columnSlotNumber);
        Assertions.assertEquals(name1, name2);
    }

    @Test
    public void testMakeColumnName() {
//        short serviceType, String applicationName, String destHost, short slotNumber
        final short slotNumber = 10;
        final byte[] columnNameBytes = InLinkV2ColumnName.makeColumnName(ServiceType.STAND_ALONE.getCode(), "applicationName", "dest", slotNumber);
        Buffer buffer = new FixedBuffer(columnNameBytes);
        Assertions.assertEquals(ServiceType.STAND_ALONE.getCode(), buffer.readShort());
        Assertions.assertEquals(10, buffer.readShort());
        Assertions.assertEquals("applicationName", buffer.read2PrefixedString());

        String dest = buffer.readPadStringAndRightTrim(buffer.remaining());
        Assertions.assertEquals("dest", dest);

    }
}