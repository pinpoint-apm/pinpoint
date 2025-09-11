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

/**
 * @author emeroad
 */
public class OffsetFixedBuffer extends FixedBuffer {

    protected int startOffset;
    protected int endOffset;


    public OffsetFixedBuffer(final byte[] buffer) {
        this(buffer, 0, buffer.length);
    }


    public OffsetFixedBuffer(final byte[] buffer, final int startOffset, final int length) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        if (startOffset < 0) {
            throw new IndexOutOfBoundsException("negative startOffset:" + startOffset);
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("negative length:" + length);
        }
        if (startOffset > buffer.length) {
            throw new IndexOutOfBoundsException("startOffset:" + startOffset + " > buffer.length:" + buffer.length);
        }
        final int endOffset = startOffset + length;
        if (endOffset > buffer.length) {
            throw new IndexOutOfBoundsException("too large length buffer.length:" + buffer.length + " endOffset:" + endOffset);
        }
        this.buffer = buffer;
        this.offset = startOffset;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    @Override
    public int getStartOffset() {
        return startOffset;
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }
}
