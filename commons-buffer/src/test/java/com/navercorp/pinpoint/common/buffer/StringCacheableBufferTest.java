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

import com.navercorp.pinpoint.common.util.LRUCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringCacheableBufferTest {

    @Test
    public void stringCache() {

        Buffer writer = new AutomaticBuffer();
        writer.putPrefixedString("abc");
        writer.putPrefixedString("123");
        writer.putPrefixedString("abc");


        StringAllocator allocator = new CachedStringAllocator(new LRUCache<>(2));

        Buffer buffer = new StringCacheableBuffer(writer.getBuffer(), allocator);
        String s1 = buffer.readPrefixedString();
        String s2 = buffer.readPrefixedString();
        String s3 = buffer.readPrefixedString();

        Assertions.assertSame(s1, s3);
    }

    @Test
    public void boundaryCheck() {

        Buffer writer = new AutomaticBuffer();
        writer.putPrefixedString("abc");
        // prefix index
        writer.putSVInt(10);


        StringAllocator allocator = new CachedStringAllocator(new LRUCache<>(2));

        Buffer buffer = new StringCacheableBuffer(writer.getBuffer(), allocator);
        String s1 = buffer.readPrefixedString();
        Assertions.assertEquals("abc", s1);

        Assertions.assertThrows(IndexOutOfBoundsException.class, buffer::readPrefixedString);
    }

    @Test
    public void boundaryCheck_prefix() {
        int offset = 4;
        Buffer writer = new AutomaticBuffer();
        writer.setOffset(offset);
        writer.putPrefixedString("abc");
        // prefix index
        writer.putSVInt(10);

        StringAllocator allocator = new CachedStringAllocator(new LRUCache<>(2));

        byte[] bytes2 = writer.getBuffer();

        Buffer buffer = new StringCacheableBuffer(bytes2, offset, bytes2.length - offset, allocator);
        String s1 = buffer.readPrefixedString();
        Assertions.assertEquals("abc", s1);

        Assertions.assertThrows(IndexOutOfBoundsException.class, buffer::readPrefixedString);
    }
}