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


    protected void checkExpand(final int size) {
        final int remain = remaining();
        if (remain >= size) {
            return;
        }
        int length = buffer.length;
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

    protected int computeExpandedBufferSize(final int size, int length, int remain) {
        int expandedBufferSize = 0;
        while (remain < size) {
            length <<= 1;
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
            super.putSVInt(NULL);
        } else {
            checkExpand(bytes.length + BytesUtils.VINT_MAX_SIZE);
            super.putSVInt(bytes.length);
            super.putBytes(bytes);
        }
    }

    @Override
    public void put2PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            checkExpand(BytesUtils.SHORT_BYTE_LENGTH);
            super.putShort((short)NULL);
        } else {
            if (bytes.length > Short.MAX_VALUE) {
                throw new IndexOutOfBoundsException("too large bytes length:" + bytes.length);
            }
            checkExpand(bytes.length + BytesUtils.SHORT_BYTE_LENGTH);
            super.putShort((short)bytes.length);
            super.putBytes(bytes);
        }
    }

    @Override
    public void put4PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            checkExpand(BytesUtils.INT_BYTE_LENGTH);
            super.putInt(NULL);
        } else {
            checkExpand(bytes.length + BytesUtils.INT_BYTE_LENGTH);
            super.putInt(bytes.length);
            super.putBytes(bytes);
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
    public void putByte(final byte v) {
        checkExpand(1);
        super.putByte(v);
    }


    @Override
    public void putBoolean(final boolean v) {
        checkExpand(1);
        super.putBoolean(v);
    }



    @Override
    public void putShort(final short v) {
        checkExpand(2);
        super.putShort(v);
    }

    @Override
    public void putInt(final int v) {
        checkExpand(4);
        super.putInt(v);
    }


    @Override
    public void putVInt(final int v) {
        checkExpand(BytesUtils.VLONG_MAX_SIZE);
        super.putVInt(v);
    }


    @Override
    public void putSVInt(final int v) {
        checkExpand(BytesUtils.VINT_MAX_SIZE);
        super.putSVInt(v);
    }



    @Override
    public void putVLong(final long v) {
        checkExpand(BytesUtils.VLONG_MAX_SIZE);
        super.putVLong(v);
    }


    @Override
    public void putSVLong(final long v) {
        checkExpand(BytesUtils.VLONG_MAX_SIZE);
        super.putSVLong(v);
    }


    @Override
    public void putLong(final long v) {
        checkExpand(8);
        super.putLong(v);
    }




    @Override
    public void putBytes(final byte[] v) {
        if (v == null) {
            throw new NullPointerException("v must not be null");
        }
        checkExpand(v.length);
        super.putBytes(v);
    }

}
