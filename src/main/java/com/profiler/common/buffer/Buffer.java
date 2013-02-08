package com.profiler.common.buffer;

import java.nio.charset.Charset;

/**
 *
 */
public interface Buffer {

    public static final int BOOLEAN_FALSE = 0;
    public static final int BOOLEAN_TRUE = 1;

    public static final byte[] EMPTY = new byte[0];

    public static final String UTF8 = "UTF-8";

    void put1PrefixedBytes(byte[] bytes);

    void put2PrefixedBytes(byte[] bytes);

    void putPrefixedBytes(byte[] bytes);

    void putPrefixedString(String string);

    void putNullTerminatedBytes(byte[] bytes);


    void put(byte v);

    void put(boolean v);

    void put(int v);

    void put(short v);

    void put(long v);

    void put(byte[] v);


//    void put(String string);


    byte readByte();

    int readUnsignedByte();

    boolean readBoolean();

    int readInt();

    short readShort();

    long readLong();

    byte[] readPrefixedBytes();

    byte[] read1PrefixedBytes();

    byte[] read2PrefixedBytes();

    String readPrefixedString();

    String read1PrefixedString();

    String read1UnsignedPrefixedString();

    String read2PrefixedString();

    String readNullTerminatedString();


    byte[] getBuffer();

    byte[] getInternalBuffer();

    void setOffset(int offset);

    int getOffset();

    int limit();
}
