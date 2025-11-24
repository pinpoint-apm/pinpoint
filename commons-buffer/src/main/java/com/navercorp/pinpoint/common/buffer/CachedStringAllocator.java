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

import com.navercorp.pinpoint.common.cache.Cache;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;

public class CachedStringAllocator implements StringAllocator {
    private final Cache<ByteBuffer, String> cache;

    public CachedStringAllocator(Cache<ByteBuffer, String> cache) {
        this.cache = Objects.requireNonNull(cache, "cache");
    }

    @Override
    public String allocate(byte[] bytes, int offset, int length, Charset charset) {
        final ByteBuffer wrapByteBuffer = ByteBuffer.wrap(bytes, offset, length);

        final String hit = cache.get(wrapByteBuffer);
        if (hit != null) {
            return hit;
        }

        final String newString = StringAllocator.DEFAULT_ALLOCATOR.allocate(bytes, offset, length, charset);
        cache.put(wrapByteBuffer, newString);
        return newString;
    }
}

