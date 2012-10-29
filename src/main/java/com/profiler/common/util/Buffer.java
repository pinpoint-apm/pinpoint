package com.profiler.common.util;

import java.nio.charset.Charset;

/**
 *
 */
public class Buffer {

    public static final int BOOLEAN_FALSE = 0;
    public static final int BOOLEAN_TRUE = 1;

    public static byte[] EMPTY = new byte[0];

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private byte[] buffer;
    private int offset;

    public Buffer(int size) {
        this.buffer = new byte[size];
        this.offset = 0;
    }

    public Buffer(byte[] buffer) {
        if (buffer == null) {
            throw new NullPointerException("buffer must not be null");
        }
        this.buffer = buffer;
        this.offset = 0;
    }

    public Buffer(byte[] buffer, int offset) {
        if (buffer == null) {
            throw new NullPointerException("buffer must not be null");
        }
        this.buffer = buffer;
        this.offset = offset;
    }


    public void putPrefixedBytes(byte[] bytes) {
        if (bytes == null) {
            put(0);
        } else {
            put(bytes.length);
            put(bytes);
        }
    }

    public byte readByte() {
        return this.buffer[offset++];
    }

    public boolean readBoolean() {
        byte b = readByte();
        if (b == BOOLEAN_FALSE) {
            return true;
        } else {
            return false;
        }
    }

    public int readInt() {
        int i = BytesUtils.bytesToInt(buffer, offset);
        this.offset = this.offset + 4;
        return i;
    }

    public long readLong() {
        long l = BytesUtils.bytesToLong(buffer, offset);
        this.offset = this.offset + 8;
        return l;
    }

    public byte[] readPrefixedBytes() {
        int size = readInt();
        if (size == 0) {
            return EMPTY;
        }
        return readBytes(size);
    }

    private byte[] readBytes(int size) {
        byte[] b = new byte[size];
        System.arraycopy(buffer, offset, b, 0, size);
        this.offset = offset + size;
        return b;
    }

    public String readPrefixedString() {
        int size = readInt();
        if (size == 0) {
            return "";
        }
        return readString(size);
    }

    private String readString(int size) {
        String s = new String(buffer, offset, size, UTF8);
        this.offset = offset + size;
        return s;
    }


    public void put(byte v) {
        this.buffer[offset++] = v;
    }

    public void put(boolean v) {
        if (v) {
            this.buffer[offset++] = BOOLEAN_TRUE;
        } else {
            this.buffer[offset++] = BOOLEAN_FALSE;
        }
    }

    public void put(int v) {
        BytesUtils.writeInt(v, buffer, offset);
        this.offset = offset + 4;
    }

    public void put(long v) {
        BytesUtils.writeLong(v, buffer, offset);
        this.offset = offset + 8;
    }

    public void put(byte[] v) {
        if (v == null) {
            throw new NullPointerException("v must not be null");
        }
        System.arraycopy(v, 0, buffer, offset, v.length);
        this.offset = offset + v.length;
    }


    public byte[] getBuffer() {
        return this.buffer;
    }

    public int getOffset() {
        return offset;
    }


}
