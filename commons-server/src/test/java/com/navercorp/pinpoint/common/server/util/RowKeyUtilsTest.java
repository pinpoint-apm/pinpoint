/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RowKeyUtilsTest {

    @Test
    void concatFixedByteAndLongFuzzySlot() {
        byte[] agentId = "agentId".getBytes(StandardCharsets.UTF_8);
        int length = 24;
        long timestamp = 100;
        byte slot = 9;

        byte[] rowKey1 = RowKeyUtils.concatFixedByteAndLong(agentId, length, timestamp);
        byte[] rowKey2 = RowKeyUtils.concatFixedByteAndLongFuzzySlot(agentId, length, timestamp, slot);

        Assertions.assertArrayEquals(rowKey1, Arrays.copyOfRange(rowKey2, 0, rowKey2.length - 1));
        Assertions.assertEquals(slot, rowKey2[rowKey2.length - 1]);
    }

    @Test
    void concatFixedByteAndLongFuzzySlot_prefix0() {
        byte[] agentId = "agentId".getBytes(StandardCharsets.UTF_8);
        int length = 24;
        long timestamp = 100;
        byte slot = 9;

        byte[] rowKey1 = RowKeyUtils.concatFixedByteAndLongFuzzySlot(agentId, length, timestamp, slot);
        byte[] rowKey2 = RowKeyUtils.concatFixedByteAndLongFuzzySlot(0, agentId, length, timestamp, slot);

        Assertions.assertArrayEquals(rowKey1, rowKey2);
        Assertions.assertEquals(slot, rowKey2[rowKey2.length - 1]);
    }

    @Test
    void concatFixedByteAndLongFuzzySlot_prefix1() {
        byte[] agentId = "agentId".getBytes(StandardCharsets.UTF_8);
        int length = 24;
        long timestamp = 100;
        byte slot = 9;

        byte[] rowKey1 = RowKeyUtils.concatFixedByteAndLongFuzzySlot(agentId, length, timestamp, slot);
        byte[] rowKey2 = RowKeyUtils.concatFixedByteAndLongFuzzySlot(1, agentId, length, timestamp, slot);

        Assertions.assertArrayEquals(rowKey1, Arrays.copyOfRange(rowKey2, 1, rowKey2.length));
    }

    @Test
    public void testStringLongLongToBytes() {
        final int strLength = 24;
        byte[] bytes = RowKeyUtils.stringLongLongToBytes("123", strLength, 12345, 54321);

        assertEquals("123", BytesUtils.toStringAndRightTrim(bytes, 0, strLength));
        assertEquals(12345, ByteArrayUtils.bytesToLong(bytes, strLength));
        assertEquals(54321, ByteArrayUtils.bytesToLong(bytes, strLength + BytesUtils.LONG_BYTE_LENGTH));
    }

    @Test
    public void testStringLongLongToBytes_error() {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
            RowKeyUtils.stringLongLongToBytes("123", 2, 1, 2);
        });
    }

    @Test
    public void testStringLongLongToBytes2() {
        byte[] bytes = RowKeyUtils.stringLongLongToBytes("123", 10, 1, 2);
        String s = BytesUtils.toStringAndRightTrim(bytes, 0, 10);
        assertEquals("123", s);
        long l = ByteArrayUtils.bytesToLong(bytes, 10);
        assertEquals(1, l);
        long l2 = ByteArrayUtils.bytesToLong(bytes, 10 + BytesUtils.LONG_BYTE_LENGTH);
        assertEquals(2, l2);
    }

    @Test
    public void testStringLongLongToBytes_prefix0() {
        byte[] bytes = RowKeyUtils.stringLongLongToBytes("123", 10, 1, 2);
        byte[] prefixedBytes = RowKeyUtils.stringLongLongToBytes(0, "123", 10, 1, 2);

        assertArrayEquals(bytes, prefixedBytes);
    }

    @Test
    public void testStringLongLongToBytes_prefix2() {
        byte[] bytes = RowKeyUtils.stringLongLongToBytes("123", 10, 1, 2);
        byte[] prefixedBytes = RowKeyUtils.stringLongLongToBytes(2, "123", 10, 1, 2);

        assertArrayEquals(bytes, Arrays.copyOfRange(prefixedBytes, 2, prefixedBytes.length));
    }
}