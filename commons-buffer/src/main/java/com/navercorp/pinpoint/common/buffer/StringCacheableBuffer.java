/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Taejin Koo
 */
public class StringCacheableBuffer extends FixedBuffer {

    private final StringAllocator stringAllocator;

    public StringCacheableBuffer(byte[] buffer, StringAllocator stringAllocator) {
        super(buffer);
        this.stringAllocator = Assert.requireNonNull(stringAllocator, "stringAllocator");
    }

    protected String readString(final int size) {
        checkBounds(buffer, offset, size);

        String newValue = stringAllocator.allocate(buffer, offset, size, Buffer.UTF8_CHARSET);
        this.offset = offset + size;
        return newValue;
    }

    private void checkBounds(byte[] bytes, int offset, int size) {
        if (size < 0) {
            throw new StringIndexOutOfBoundsException(size);
        }
        if (offset < 0) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        if (bytes.length < offset + size) {
            throw new StringIndexOutOfBoundsException(offset + size);
        }
    }

}
