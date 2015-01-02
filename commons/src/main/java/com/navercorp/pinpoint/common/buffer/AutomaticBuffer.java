/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.buffer;

import com.navercorp.pinpoint.common.util.BytesUtils;

/**
 * Buffer that can be expanded automatically
 * @author emeroad
 */
public class AutomaticBuffer extends FixedBuffer {

    public AutomaticBuffer() {
        super(32);
    }

    public AutomaticBuffer(final int size) {
        super(size);
    }

    public AutomaticBuffer(final byte[] buffer) {
        super(buffer);
    }


    private void checkExpand(final int size) {
        int length = buffer.length;
        final int remain = length - offset;
        if (remain >= size) {
            return;
        }

        if (length == 0) {
            length = 1;
        }

        // after compute the buffer size, allocate it once for ado.
        final int expandedBufferSize = computeExpandedBufferSize(size, length, remain);
        // allocate buffer
        final byte[] expandedBuffer = new byte[expandedBufferSize];
        System.arraycopy(buffer, 0, expandedBuffer, 0, buffer.length);
        buffer = expandedBuffer;
    }

    private int computeExpandedBufferSize(final int size, int length, int remain) {
        int expandedBufferSize = 0;
        while (remain < size) {
            length <<= 2;
            expandedBufferSize = length;
            remain = expandedBufferSize - offset;
        }
        return expandedBufferSize;
    }

    @Override
    public void putPadBytes(byte[] bytes, int totalLength) {
        checkExpand(totalLength);
        super.putPadBytes(bytes, totalLength);
    }


    @Override
    public void putPrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            checkExpand(1);
            super.putSVar(NULL);
        } else {
            checkExpand(bytes.length + BytesUtils.VINT_MAX_SIZE);
            super.putSVar(bytes.length);
            super.put(bytes);
        }
    }

    @Override
    public void put2PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            checkExpand(BytesUtils.SHORT_BYTE_LENGTH);
            super.put((short)NULL);
        } else {
            if (bytes.length > Short.MAX_VALUE) {
                throw new IndexOutOfBoundsException("too large bytes length:" + bytes.length);
            }
            checkExpand(bytes.length + BytesUtils.SHORT_BYTE_LENGTH);
            super.put((short)bytes.length);
            super.put(bytes);
        }
    }

    @Override
    public void put4PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            checkExpand(BytesUtils.INT_BYTE_LENGTH);
            super.put(NULL);
        } else {
            checkExpand(bytes.length + BytesUtils.INT_BYTE_LENGTH);
            super.put(bytes.length);
            super.put(bytes);
        }
    }

    @Override
    public void putPadString(String string, int totalLength) {
        checkExpand(totalLength);
        super.putPadString(string, totalLength);
    }


    @Override
    public void putPrefixedString(final String string) {
        byte[] bytes = BytesUtils.toBytes(string);
        this.putPrefixedBytes(bytes);
    }

    @Override
    public void put2PrefixedString(final String string) {
        byte[] bytes = BytesUtils.toBytes(string);
        this.put2PrefixedBytes(bytes);
    }

    @Override
    public void put4PrefixedString(final String string) {
        byte[] bytes = BytesUtils.toBytes(string);
        this.put4PrefixedBytes(bytes);
    }

    @Override
    public void put(final byte v) {
        checkExpand(1);
        super.put(v);
    }

    @Override
    public void put(final boolean v) {
        checkExpand(1);
        super.put(v);
    }

    @Override
    public void put(final short v) {
        checkExpand(2);
        super.put(v);
    }

    @Override
    public void put(final int v) {
        checkExpand(4);
        super.put(v);
    }

    public void putVar(final int v) {
        checkExpand(BytesUtils.VLONG_MAX_SIZE);
        super.putVar(v);
    }

    public void putSVar(final int v) {
        checkExpand(BytesUtils.VINT_MAX_SIZE);
        super.putSVar(v);
    }

    public void putVar(final long v) {
        checkExpand(BytesUtils.VLONG_MAX_SIZE);
        super.putVar(v);
    }

    public void putSVar(final long v) {
        checkExpand(BytesUtils.VLONG_MAX_SIZE);
        super.putSVar(v);
    }

    @Override
    public void put(final long v) {
        checkExpand(8);
        super.put(v);
    }

    @Override
    public void put(final byte[] v) {
        if (v == null) {
            throw new NullPointerException("v must not be null");
        }
        checkExpand(v.length);
        super.put(v);
    }
}
