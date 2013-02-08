package com.profiler.common.buffer;

import com.profiler.common.util.BytesUtils;

import java.io.UnsupportedEncodingException;

/**
 *
 */
public class FixedBuffer implements Buffer {

    protected byte[] buffer;
    protected int offset;

    public FixedBuffer() {
        this(32);
    }

    public FixedBuffer(final int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size:" + size);
        }
        this.buffer = new byte[size];
        this.offset = 0;
    }

    public FixedBuffer(final byte[] buffer) {
        if (buffer == null) {
            throw new NullPointerException("buffer must not be null");
        }
        this.buffer = buffer;
        this.offset = 0;
    }

    public FixedBuffer(final byte[] buffer, final int offset) {
        if (buffer == null) {
            throw new NullPointerException("buffer must not be null");
        }
        this.buffer = buffer;
        this.offset = offset;
    }

    @Override
    public void put1PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            put(0);
        } else {
            int length = (byte) bytes.length;
            if (length > Byte.MAX_VALUE) {
                throw new IllegalArgumentException("too large bytes:" + bytes.length);
            }
            put((byte) length);
            put(bytes);
        }
    }

    @Override
    public void put2PrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            put(0);
        } else {
            int length = (byte) bytes.length;
            if (length > Short.MAX_VALUE) {
                throw new IllegalArgumentException("too large bytes:" + bytes.length);
            }
            put((short) length);
            put(bytes);
        }
    }

    @Override
    public void putPrefixedBytes(final byte[] bytes) {
        if (bytes == null) {
            put(0);
        } else {
            put(bytes.length);
            put(bytes);
        }
    }

    @Override
    public void putNullTerminatedBytes(final byte[] bytes) {
        if (bytes == null) {
            put(0);
        } else {
            put(bytes);
            put((byte) 0);
        }
    }

    @Override
    public void putPrefixedString(final String string) {
        byte[] bytes = BytesUtils.getBytes(string);
        putPrefixedBytes(bytes);
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
        BytesUtils.writeInt(v, buffer, offset);
        this.offset = offset + 4;
    }

    @Override
    public void put(final short v) {
        BytesUtils.writeShort(v, buffer, offset);
        this.offset = offset + 2;
    }

    @Override
    public void put(final long v) {
        BytesUtils.writeLong(v, buffer, offset);
        this.offset = offset + 8;
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
    public short readShort() {
        final short i = BytesUtils.bytesToShort(buffer, offset);
        this.offset = this.offset + 2;
        return i;
    }

    @Override
    public long readLong() {
        final long l = BytesUtils.bytesToLong(buffer, offset);
        this.offset = this.offset + 8;
        return l;
    }

    @Override
    public byte[] readPrefixedBytes() {
        final int size = readInt();
        if (size == 0) {
            return EMPTY;
        }
        return readBytes(size);
    }

    @Override
    public byte[] read1PrefixedBytes() {
        final int b = readByte();
        if (b == 0) {
            return EMPTY;
        }
        return readBytes(b);
    }

    @Override
    public byte[] read2PrefixedBytes() {
        final int b = readShort();
        if (b == 0) {
            return EMPTY;
        }
        return readBytes(b);
    }

    private byte[] readBytes(int size) {
        final byte[] b = new byte[size];
        System.arraycopy(buffer, offset, b, 0, size);
        this.offset = offset + size;
        return b;
    }

    @Override
    public String readPrefixedString() {
        final int size = readInt();
        if (size == 0) {
            return "";
        }
        return readString(size);
    }

    @Override
    public String read1PrefixedString() {
        final int size = readByte();
        if (size == 0) {
            return "";
        }
        return readString(size);
    }

    @Override
    public String read1UnsignedPrefixedString() {
        final int size = readUnsignedByte();
        if (size == 0) {
            return "";
        }
        return readString(size);
    }

    @Override
    public String read2PrefixedString() {
        final int size = readShort();
        if (size == 0) {
            return "";
        }
        return readString(size);
    }

    @Override
    public String readNullTerminatedString() {
        final int size = findNull();
        if (size == 0) {
            return "";
        } else if (size == -1) {
            throw new IllegalArgumentException("null not found");
        }
        return readString(size);
    }

    private int findNull() {
        for (int i = offset; i < buffer.length; i++) {
            final byte b = this.buffer[i];
            if (b == 0) {
                return i - offset;
            }
        }
        return -1;
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


    @Override
    public byte[] getBuffer() {
        if (offset == buffer.length) {
            return this.buffer;
        } else {
            final byte[] copy = new byte[offset];
            System.arraycopy(buffer, 0, copy, 0, offset);
            return copy;
        }
    }


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
