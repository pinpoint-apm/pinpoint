/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.buffer;

import com.navercorp.pinpoint.common.util.BytesUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ByteArrayUtilsTest {

    @Test
    void bytesToLong() {
        byte[] bytes = new byte[ByteArrayUtils.LONG_BYTE_LENGTH];
        long value = 999;

        // Test Functional
        ByteArrayUtils.writeLong(value, bytes, 0);
        long result = ByteArrayUtils.bytesToLong(bytes, 0);
        assertEquals(value, result);

        // Test Compatibility
        BytesUtils.writeLong(value, bytes, 0);
        result = BytesUtils.bytesToLong(bytes, 0);
        assertEquals(value, result);
        result = ByteArrayUtils.bytesToLong(bytes, 0);
        assertEquals(value, result);
    }

    @Test
    void writeLong() {
        byte[] bytes1 = new byte[BytesUtils.LONG_BYTE_LENGTH];
        long value = 999;

        // Test Compatibility
        BytesUtils.writeLong(value, bytes1, 0);
        byte[] bytes2 = new byte[BytesUtils.LONG_BYTE_LENGTH];
        ByteArrayUtils.writeLong(value, bytes2, 0);
        Assertions.assertArrayEquals(bytes1, bytes2);
    }

    @Test
    void bytesToInt() {
        byte[] bytes = new byte[BytesUtils.INT_BYTE_LENGTH];
        int value = 999;

        // Test Functional
        ByteArrayUtils.writeInt(value, bytes, 0);
        int result = ByteArrayUtils.bytesToInt(bytes, 0);
        assertEquals(value, result);

        // Test Compatibility
        BytesUtils.writeInt(value, bytes, 0);
        result = BytesUtils.bytesToInt(bytes, 0);
        assertEquals(value, result);
        result = ByteArrayUtils.bytesToInt(bytes, 0);
        assertEquals(value, result);
    }

    @Test
    void writeInt() {
        byte[] bytes1 = new byte[BytesUtils.INT_BYTE_LENGTH];
        int value = 999;

        // Test Compatibility
        BytesUtils.writeInt(value, bytes1, 0);
        byte[] bytes2 = new byte[BytesUtils.INT_BYTE_LENGTH];
        ByteArrayUtils.writeInt(value, bytes2, 0);
        assertArrayEquals(bytes1, bytes2);
    }

    @Test
    void bytesToShort() {
        byte[] bytes = new byte[BytesUtils.SHORT_BYTE_LENGTH];
        short value = 999;

        // Test Functional
        ByteArrayUtils.writeShort(value, bytes, 0);
        short result = ByteArrayUtils.bytesToShort(bytes, 0);
        assertEquals(value, result);

        // Test Compatibility
        BytesUtils.writeShort(value, bytes, 0);
        result = BytesUtils.bytesToShort(bytes, 0);
        assertEquals(value, result);
        result = ByteArrayUtils.bytesToShort(bytes, 0);
        assertEquals(value, result);
    }

    @Test
    void writeShort() {
        byte[] bytes1 = new byte[BytesUtils.SHORT_BYTE_LENGTH];
        short value = 999;

        // Test Compatibility
        BytesUtils.writeShort(value, bytes1, 0);
        byte[] bytes2 = new byte[BytesUtils.SHORT_BYTE_LENGTH];
        ByteArrayUtils.writeShort(value, bytes2, 0);
        assertArrayEquals(bytes1, bytes2);
    }
}