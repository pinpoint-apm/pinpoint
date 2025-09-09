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

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResponseColumnNameTest {

    @Test
    void testEquals() {

        short columnSlotNumber = 2;

        ResponseColumnName name1 = new ResponseColumnName("agentId", columnSlotNumber);
        ResponseColumnName name2 = new ResponseColumnName("agentId", columnSlotNumber);
        Assertions.assertEquals(name1, name2);
    }

    @Test
    public void testMakeColumnName() {
        final byte[] columnNameBytes = ResponseColumnName.makeColumnName("test", (short) 10);
        short slotNumber = ByteArrayUtils.bytesToShort(columnNameBytes, 0);
        Assertions.assertEquals(10, slotNumber);

        String columnName = BytesUtils.toString(columnNameBytes, BytesUtils.SHORT_BYTE_LENGTH, columnNameBytes.length - BytesUtils.SHORT_BYTE_LENGTH);
        Assertions.assertEquals("test", columnName);

    }
}