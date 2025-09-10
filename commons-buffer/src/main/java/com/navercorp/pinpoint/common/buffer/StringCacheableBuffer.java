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

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class StringCacheableBuffer extends OffsetFixedBuffer {

    private final StringAllocator stringAllocator;

    public StringCacheableBuffer(byte[] buffer, StringAllocator stringAllocator) {
        super(buffer, 0, buffer.length);
        this.stringAllocator = Objects.requireNonNull(stringAllocator, "stringAllocator");
    }

    public StringCacheableBuffer(byte[] buffer, int offset, int length, StringAllocator stringAllocator) {
        super(buffer, offset, length);
        this.stringAllocator = Objects.requireNonNull(stringAllocator, "stringAllocator");
    }

    protected String readString(final int size) {
        Objects.checkFromIndexSize(offset, size, endOffset);

        String newValue = stringAllocator.allocate(buffer, offset, size, Buffer.UTF8_CHARSET);
        this.offset = offset + size;
        return newValue;
    }

}
