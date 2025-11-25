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

package com.navercorp.pinpoint.common.buffer;

import com.navercorp.pinpoint.common.util.BytesUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CachedStringAllocatorTest {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @Test
    void allocate() {
        // Arrange
        CachedStringAllocator allocator = new CachedStringAllocator(10);
        byte[] bytes = BytesUtils.toBytes("test");

        // Act
        String result1 = allocator.allocate(bytes, 0, bytes.length, UTF8);
        String result2 = allocator.allocate(bytes, 0, bytes.length, UTF8);

        // Assert
        assertEquals(result1, result2);
        assertSame(result1, result2, "Cached instance should be returned.");
    }

    @Test
    void allocate_createsNewString() {
        // Arrange
        CachedStringAllocator allocator = new CachedStringAllocator(10);
        byte[] bytes1 = BytesUtils.toBytes("test1");
        byte[] bytes2 = BytesUtils.toBytes("test2");

        // Act
        String result1 = allocator.allocate(bytes1, 0, bytes1.length, UTF8);
        String result2 = allocator.allocate(bytes2, 0, bytes2.length, UTF8);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotSame(result1, result2, "Different strings should not be the same instance.");
        assertEquals("test1", result1);
        assertEquals("test2", result2);
    }

    @Test
    void allocate_handlesNullCharset() {
        // Arrange
        CachedStringAllocator allocator = new CachedStringAllocator(10);
        byte[] bytes = BytesUtils.toBytes("test");

        // Act & Assert
        assertThrows(NullPointerException.class, () -> allocator.allocate(bytes, 0, bytes.length, null));
    }

}