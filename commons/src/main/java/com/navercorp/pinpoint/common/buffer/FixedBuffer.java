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

import java.io.UnsupportedEncodingException;

/**
 * @author emeroad
 */
public class FixedBuffer implements Buffer {
    protected static final int NULL = -1;
    protected byte[] buffer;
    protected int offset;

    public FixedBuffer() {
        this(32);
    }

    public FixedBuffer(final int bufferSize) {
        if (bufferSize < 0) {
            throw new IndexOutOfBoundsException("negative bufferSize:" + bufferSize);
        }
        this.buffer = new byte[bufferSize];
        this.offset = 0;
    }

    public FixedBuffer(final byte[] buffer) {
        if (buffer == null) {
            throw new NullPointerException("buffer must not be null");
        }
        this.buffer = buffer;
        this.offset = 0;
    }

    @Override
    public void putPadBytes(byte[] bytes, int totalLength) {
        if (bytes == null) {
            bytes = EMPTY;
        }
        if (bytes.length > totalLength) {
            throw new IndexOutOfBoundsException("bytes too big:" + bytes.length + " totalLength:" + totalLength);
        }
        put(bytes);
        final int padSize = totalLength - bytes.length;
        if (padSize > 0) {
            putPad(padSize);
        }
    }

    private void putPad(int padSize) {
        for (int i = 0; i < padSize; i++) {
            put((byte)0);
        }
    }


    @Override
    public void putPrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            putSVar(NULL);
        } else {
            putSVar(bytes.length);
            put(bytes);
        }
    }

    @Override
    public void put2PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            put((short)NULL);
        } else {
            if (bytes.length > Short.MAX_VALUE) {
                throw new IndexOutOfBoundsException("too large bytes length:" + bytes.length);
            }
            put((short)bytes.length);
            put(bytes);
        }
    }

    @Override
    public void put4PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            put(NULL);
        } else {
            put(bytes.length);
            put(bytes);
        }
    }

    @Override
    public void putPadString(String string, int totalLength) {
        final byte[] bytes = BytesUtils.toBytes(string);
        putPadBytes(bytes, totalLength);
    }

    @Override
    public void putPrefixedString(final String string) {
        final byte[] bytes = BytesUtils.toBytes(string);
        putPrefixedBytes(bytes);
    }

    @Override
    public void put2PrefixedString(final String string) {
        final byte[] bytes = BytesUtils.toBytes(string);
        if (bytes == null) {
            put((short)NULL);
            return;
        }
        if (bytes.length > Short.MAX_VALUE) {
            throw new IndexOutOfBoundsException("too large String size:" + bytes.length);
        }
        put2PrefixedBytes(bytes);
    }

    @Override
    public void put4PrefixedString(final String string) {
        final byte[] bytes = BytesUtils.toBytes(string);
        if (bytes == null) {
            put(NULL);
            return;
        }
        put4PrefixedBytes(bytes);
    }

    @Override
    public void put(final byte v) {
        this.buffer[offset++] = v;
    }

    @Override
    public void put(final boolean v) {
        if (v) {
            this.buffer[offset++] = BOOLEAN_TRUE;
        } else {
            this.buffer[offset++] = BOOLEAN_FALSE;
        }
    }

    @Override
    public void put(final int v) {
        this.offset = BytesUtils.writeInt(v, buffer, offset);
    }

    public void putVar(int v) {
        if (v >= 0) {
            putVar32(v);
        } else {
            putVar64((long) v);
        }
    }

    public void putSVar(int v) {
        this.offset = BytesUtils.writeSVar32(v, buffer, offset);
    }

    private void putVar32(int v) {
        this.offset = BytesUtils.writeVar32(v, buffer, offset);
    }

    @Override
    public void put(final short v) {
        this.offset = BytesUtils.writeShort(v, buffer, offset);
    }

    @Override
    public void put(final long v) {
        this.offset = BytesUtils.writeLong(v, buffer, offset);
    }

    @Override
    public void putVar(long v) {
        putVar64(v);
    }

    @Override
    public void putSVar(long v) {
        putVar64(BytesUtils.longToZigZag(v));
    }

    private void putVar64(long v) {
        this.offset = BytesUtils.writeVar64(v, buffer, offset);
    }

    @Override
    public void put(double v) {
        put(Double.doubleToRawLongBits(v));
    }

    @Override
    public void putVar(double v) {
        putVar(Double.doubleToRawLongBits(v));
    }

    @Override
    public void putSVar(double v) {
        putSVar(Double.doubleToRawLongBits(v));
    }

    @Override
    public void put(final byte[] v) {
        if (v == null) {
            throw new NullPointerException("v must not be null");
        }
        System.arraycopy(v, 0, buffer, offset, v.length);
        this.offset = offset + v.length;
    }

    @Override
    public byte readByte() {
        return this.buffer[offset++];
    }

    @Override
    public int readUnsignedByte() {
        return readByte() & 0xff;
    }

    @Override
    public boolean readBoolean() {
        final byte b = readByte();
        return b == BOOLEAN_TRUE;
    }

    @Override
    public int readInt() {
        final int i = BytesUtils.bytesToInt(buffer, offset);
        this.offset = this.offset + 4;
        return i;
    }

    @Override
    public int readVarInt() {
        final byte[] buffer = this.buffer;
        int offset = this.offset;
        // borrowing the protocol buffer's concept of variable-length encoding
        int result;
        byte v = buffer[offset++];
        if (v >= 0) {
            this.offset = offset;
            return v;
        }
        result = v & 0x7f;
        if ((v = buffer[offset++]) >= 0) {
            result |= v << 7;
        } else {
            result |= (v & 0x7f) << 7;
            if ((v = buffer[offset++]) >= 0) {
                result |= v << 14;
            } else {
                result |= (v & 0x7f) << 14;
                if ((v = buffer[offset++]) >= 0) {
                    result |= v << 21;
                } else {
                    result |= (v & 0x7f) << 21;
                    result |= (v = buffer[offset++]) << 28;
                    if (v < 0) {
                        for (int i = 0; i < 5; i++) {
                            if (buffer[offset++] >= 0) {
                                this.offset = offset;
                                return result;
                            }
                        }
                        throw new IllegalArgumentException("invalid varInt. start offset:" +  this.offset + " readOffset:" + offset);
                    }
                }
            }
        }
        this.offset = offset;
        return result;
    }

    public int readSVarInt() {
        return BytesUtils.zigzagToInt(readVarInt());
    }

    @Override
    public short readShort() {
        final short i = BytesUtils.bytesToShort(buffer, offset);
        this.offset = this.offset + 2;
        return i;
    }

    public int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    @Override
    public long readLong() {
        final long l = BytesUtils.bytesToLong(buffer, offset);
        this.offset = this.offset + 8;
        return l;
    }

    @Override
    public long readVarLong() {
        final byte[] buffer = this.buffer;
        int offset = this.offset;

        int shift = 0;
        long result = 0;
        while (shift < 64) {
            final byte v = buffer[offset++];
            result |= (long)(v & 0x7F) << shift;
            if ((v & 0x80) == 0) {
                this.offset = offset;
                return result;
            }
            shift += 7;
        }
        throw new IllegalArgumentException("invalid varLong. start offset:" +  this.offset + " readOffset:" + offset);
    }

    @Override
    public long readSVarLong() {
        return BytesUtils.zigzagToLong(readVarLong());
    }

    @Override
    public double readDouble() {
        return Double.longBitsToDouble(this.readLong());
    }

    @Override
    public double readVarDouble() {
        return Double.longBitsToDouble(this.readVarLong());
    }

    @Override
    public double readSVarDouble() {
        return Double.longBitsToDouble(this.readSVarLong());
    }

    @Override
    public byte[] readPadBytes(int totalLength) {
        return readBytes(totalLength);
    }

    @Override
    public String readPadString(int totalLength) {
        return readString(totalLength);
    }

    @Override
    public String readPadStringAndRightTrim(int totalLength) {
        String string = BytesUtils.toStringAndRightTrim(buffer, offset, totalLength);
        this.offset = offset + totalLength;
        return string ;
    }


    @Override
    public byte[] readPrefixedBytes() {
        final int size = readSVarInt();
        if (size == NULL) {
            return null;
        }
        if (size == 0) {
            return EMPTY;
        }
        return readBytes(size);
    }

    @Override
    public byte[] read2PrefixedBytes() {
        final int size = readShort();
        if (size == NULL) {
            return null;
        }
        if (size == 0) {
            return EMPTY;
        }
        return readBytes(size);
    }

    @Override
    public byte[] read4PrefixedBytes() {
        final int size = readInt();
        if (size == NULL) {
            return null;
        }
        if (size == 0) {
            return EMPTY;
        }
        return readBytes(size);
    }


    private byte[] readBytes(int size) {
        final byte[] b = new byte[size];
        System.arraycopy(buffer, offset, b, 0, size);
        this.offset = offset + size;
        return b;
    }

    @Override
    public String readPrefixedString() {
        final int size = readSVarInt();
        if (size == NULL) {
            return null;
        }
        if (size == 0) {
            return "";
        }
        return readString(size);
    }

    @Override
    public String read2PrefixedString() {
        final int size = readShort();
        if (size == NULL) {
            return null;
        }
        if (size == 0) {
            return "";
        }
        return readString(size);
    }

    @Override
    public String read4PrefixedString() {
        final int size = readInt();
        if (size == NULL) {
            return null;
        }
        if (size == 0) {
            return "";
        }
        return readString(size);
    }


    private String readString(final int size) {
        final String s = newString(size);
        this.offset = offset + size;
        return s;
    }

    private String newString(final int size) {
        try {
            return new String(buffer, offset, size, UTF8);
        } catch (UnsupportedEncodingException ue) {
            throw new RuntimeException(ue.getMessage(), ue);
        }
    }

    /**
     * Be careful that if internal buffer's length is as same as offset,
     * then just return internal buffer without copying memory for improving performance.
     * @return
     */
    @Override
    public byte[] getBuffer() {
        if (offset == buffer.length) {
            return this.buffer;
        } else {
            return copyBuffer();
        }
    }

    @Override
    public byte[] copyBuffer() {
        final byte[] copy = new byte[offset];
        System.arraycopy(buffer, 0, copy, 0, offset);
        return copy;
    }

    /**
     * return internal buffer
     * @return
     */
    @Override
    public byte[] getInternalBuffer() {
        return this.buffer;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int limit() {
        return buffer.length - offset;
    }
}
