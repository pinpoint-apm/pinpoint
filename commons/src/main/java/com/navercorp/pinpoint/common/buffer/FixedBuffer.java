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
import java.nio.ByteBuffer;

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
        putBytes(bytes);
        final int padSize = totalLength - bytes.length;
        if (padSize > 0) {
            putPad(padSize);
        }
    }

    private void putPad(int padSize) {
        for (int i = 0; i < padSize; i++) {
            putByte((byte)0);
        }
    }


    @Override
    public void putPrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            putSVInt(NULL);
        } else {
            putSVInt(bytes.length);
            putBytes(bytes);
        }
    }

    @Override
    public void put2PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            putShort((short)NULL);
        } else {
            if (bytes.length > Short.MAX_VALUE) {
                throw new IndexOutOfBoundsException("too large bytes length:" + bytes.length);
            }
            putShort((short)bytes.length);
            putBytes(bytes);
        }
    }

    @Override
    public void put4PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            putInt(NULL);
        } else {
            putInt(bytes.length);
            putBytes(bytes);
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
            putShort((short)NULL);
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
            putInt(NULL);
            return;
        }
        put4PrefixedBytes(bytes);
    }

    @Override
    public void putByte(final byte v) {
        this.buffer[offset++] = v;
    }


    @Override
    public void putBoolean(final boolean v) {
        if (v) {
            this.buffer[offset++] = BOOLEAN_TRUE;
        } else {
            this.buffer[offset++] = BOOLEAN_FALSE;
        }
    }


    @Override
    public void putInt(final int v) {
        this.offset = BytesUtils.writeInt(v, buffer, offset);
    }


    @Override
    public void putVInt(int v) {
        if (v >= 0) {
            putVar32(v);
        } else {
            putVar64((long) v);
        }
    }


    @Override
    public void putSVInt(int v) {
        this.offset = BytesUtils.writeSVar32(v, buffer, offset);
    }


    private void putVar32(int v) {
        this.offset = BytesUtils.writeVar32(v, buffer, offset);
    }

    @Override
    public void putShort(final short v) {
        this.offset = BytesUtils.writeShort(v, buffer, offset);
    }


    @Override
    public void putLong(final long v) {
        this.offset = BytesUtils.writeLong(v, buffer, offset);
    }


    @Override
    public void putVLong(long v) {
        putVar64(v);
    }


    @Override
    public void putSVLong(long v) {
        putVar64(BytesUtils.longToZigZag(v));
    }

    private void putVar64(long v) {
        this.offset = BytesUtils.writeVar64(v, buffer, offset);
    }

    @Override
    public void putDouble(double v) {
        putLong(Double.doubleToRawLongBits(v));
    }


    @Override
    public void putVDouble(double v) {
        putVLong(Double.doubleToRawLongBits(v));
    }

    @Override
    public void putSVDouble(double v) {
        putSVLong(Double.doubleToRawLongBits(v));
    }


    @Override
    public void putBytes(final byte[] v) {
        if (v == null) {
            throw new NullPointerException("v must not be null");
        }
        System.arraycopy(v, 0, buffer, offset, v.length);
        this.offset = offset + v.length;
    }


    @Override
    public byte getByte(int index) {
        return this.buffer[offset];
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
    public int readVInt() {
        // borrowing the protocol buffer's concept of variable-length encoding
        // copy https://github.com/google/protobuf 2.6.1
        // CodedInputStream.java -> int readRawVarint32()

        // See implementation notes for readRawVarint64
        fastpath: {
            int pos = this.offset;
            final int bufferSize = this.buffer.length;
            if (bufferSize == pos) {
                break fastpath;
            }

            final byte[] buffer = this.buffer;
            int x;
            if ((x = buffer[pos++]) >= 0) {
                this.offset = pos;
                return x;
            } else if (bufferSize - pos < 9) {
                break fastpath;
            } else if ((x ^= (buffer[pos++] << 7)) < 0) {
                x ^= (~0 << 7);
            } else if ((x ^= (buffer[pos++] << 14)) >= 0) {
                x ^= (~0 << 7) ^ (~0 << 14);
            } else if ((x ^= (buffer[pos++] << 21)) < 0) {
                x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21);
            } else {
                int y = buffer[pos++];
                x ^= y << 28;
                x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21) ^ (~0 << 28);
                if (y < 0 &&
                        buffer[pos++] < 0 &&
                        buffer[pos++] < 0 &&
                        buffer[pos++] < 0 &&
                        buffer[pos++] < 0 &&
                        buffer[pos++] < 0) {
                    break fastpath;  // Will throw malformedVarint()
                }
            }
            this.offset = pos;
            return x;
        }
        return (int) readVar64SlowPath();
    }


    /** Variant of readRawVarint64 for when uncomfortably close to the limit. */
    /* Visible for testing */
    long readVar64SlowPath() {
        int copyOffset = this.offset;
        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            final byte b = this.buffer[copyOffset++];
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                this.offset = copyOffset;
                return result;
            }
        }
        throw new IllegalArgumentException("invalid varLong. start offset:" +  this.offset + " readOffset:" + offset);
    }

    @Override
    public int readSVInt() {
        return BytesUtils.zigzagToInt(readVInt());
    }

    @Deprecated
    public int readSVarInt() {
        return readSVInt();
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
    public long readVLong() {
        // borrowing the protocol buffer's concept of variable-length encoding
        // copy https://github.com/google/protobuf 2.6.1
        // CodedInputStream.java -> long readRawVarint64() throws IOException

        // Implementation notes:
        //
        // Optimized for one-byte values, expected to be common.
        // The particular code below was selected from various candidates
        // empirically, by winning VarintBenchmark.
        //
        // Sign extension of (signed) Java bytes is usually a nuisance, but
        // we exploit it here to more easily obtain the sign of bytes read.
        // Instead of cleaning up the sign extension bits by masking eagerly,
        // we delay until we find the final (positive) byte, when we clear all
        // accumulated bits with one xor.  We depend on javac to constant fold.
        fastpath: {
            int pos = offset;
            int bufferSize = this.buffer.length;
            if (bufferSize == pos) {
                break fastpath;
            }

            final byte[] buffer = this.buffer;
            long x;
            int y;
            if ((y = buffer[pos++]) >= 0) {
                this.offset = pos;
                return y;
            } else if (bufferSize - pos < 9) {
                break fastpath;
            } else if ((x = y ^ (buffer[pos++] << 7)) < 0L) {
                x ^= (~0L << 7);
            } else if ((x ^= (buffer[pos++] << 14)) >= 0L) {
                x ^= (~0L << 7) ^ (~0L << 14);
            } else if ((x ^= (buffer[pos++] << 21)) < 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21);
            } else if ((x ^= ((long) buffer[pos++] << 28)) >= 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28);
            } else if ((x ^= ((long) buffer[pos++] << 35)) < 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35);
            } else if ((x ^= ((long) buffer[pos++] << 42)) >= 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35) ^ (~0L << 42);
            } else if ((x ^= ((long) buffer[pos++] << 49)) < 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35) ^ (~0L << 42)
                        ^ (~0L << 49);
            } else {
                x ^= ((long) buffer[pos++] << 56);
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35) ^ (~0L << 42)
                        ^ (~0L << 49) ^ (~0L << 56);
                if (x < 0L) {
                    if (buffer[pos++] < 0L) {
                        break fastpath;  // Will throw malformedVarint()
                    }
                }
            }
            this.offset = pos;
            return x;
        }
        return readVar64SlowPath();
    }


    @Override
    public long readSVLong() {
        return BytesUtils.zigzagToLong(readVLong());
    }

    @Override
    public double readDouble() {
        return Double.longBitsToDouble(this.readLong());
    }

    @Override
    public double readVDouble() {
        return Double.longBitsToDouble(this.readVLong());
    }

    @Override
    public double readSVDouble() {
        return Double.longBitsToDouble(this.readSVLong());
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
        final int size = readSVInt();
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
        final int size = readSVInt();
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
            return new String(buffer, offset, size, UTF8_CHARSET);
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

    @Override
    public ByteBuffer wrapByteBuffer() {
        return ByteBuffer.wrap(this.buffer, 0, offset);
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
    public int remaining() {
        return buffer.length - offset;
    }

    @Override
    public boolean hasRemaining() {
        return offset < buffer.length;
    }
}
