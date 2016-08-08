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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author emeroad
 */
public interface Buffer {

    int BOOLEAN_FALSE = 0;
    int BOOLEAN_TRUE = 1;

    byte[] EMPTY = new byte[0];

    String UTF8 = "UTF-8";

    Charset UTF8_CHARSET = Charset.forName(UTF8);

    void putPadBytes(byte[] bytes, int totalLength);

    void putPrefixedBytes(byte[] bytes);

    void put2PrefixedBytes(byte[] bytes);

    void put4PrefixedBytes(byte[] bytes);

    void putPadString(String string, int totalLength);

    void putPrefixedString(String string);

    void put2PrefixedString(String string);

    void put4PrefixedString(String string);

    void putByte(byte v);

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#putByte(byte)}
     */
    @Deprecated
    void put(byte v);

    void putBoolean(boolean v);

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#putBoolean(boolean)}
     */
    @Deprecated
    void put(boolean v);

    void putInt(int v);

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#putInt(int)}
     */
    @Deprecated
    void put(int v);

    /**
     * put value using the variable-length encoding especially for constants
     * the size using variable-length encoding is bigger than using fixed-length int when v is negative.
     * if there are a lot of negative value in a buffer, it's very inefficient.
     * instead use putSVar in that case.
     * putVar compared to putSVar has a little benefit to use a less cpu due to no zigzag operation.
     * it has more benefit to use putSVar whenever v is negative.
     * consume 1~10 bytes ( integer's max value consumes 5 bytes, integer's min value consumes 10 bytes)
     * @param v
     */
    void putVInt(int v);

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#putVInt(int)}
     */
    @Deprecated
    void putVar(int v);

    /**
     * put value using variable-length encoding
     * useful for same distribution of constants and negatives value
     * consume 1~5 bytes ( integer's max value consumes 5 bytes, integer's min value consumes 5 bytes)

     * @param v
     */
    void putSVInt(int v);

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#putSVInt(int)}
     */
    @Deprecated
    void putSVar(int v);

    void putShort(short v);

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#putShort(short)}
     */
    @Deprecated
    void put(short v);

    void putLong(long v);

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#putLong(long)}
     */
    @Deprecated
    void put(long v);

    /**
     * put value using the variable-length encoding especially for constants
     * the size using variable-length encoding is bigger than using fixed-length int when v is negative.
     * if there are a lot of negative value in a buffer, it's very inefficient.
     * instead use putSVar in that case.
     * @param v
     */
    void putVLong(long v);

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#putVLong(long)}
     */
    @Deprecated
    void putVar(long v);

    /**
     * put value using variable-length encoding
     * useful for same distribution of constants and negatives value
     * @param v
     */
    void putSVLong(long v);

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#putSVLong(long)}
     */
    @Deprecated
    void putSVar(long v);

    void putDouble(double v);

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#putDouble(double)}
     */
    @Deprecated
    void put(double v);

    /**
     * put value using the variable-length encoding especially for constants
     * the size using variable-length encoding is bigger than using fixed-length int when v is negative.
     * if there are a lot of negative value in a buffer, it's very inefficient.
     * instead use putSVar in that case.
     * @param v
     */
    void putVDouble(double v);

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#putVDouble(double)}
     */
    @Deprecated
    void putVar(double v);

    /**
     * put value using variable-length encoding
     * useful for same distribution of constants and negatives value
     * @param v
     */
    void putSVDouble(double v);

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#putSVDouble(double)}
     */
    @Deprecated
    void putSVar(double v);

    void putBytes(byte[] v);

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#putBytes(byte[])}
     */
    @Deprecated
    void put(byte[] v);

    byte getByte(int index);

    byte readByte();

    int readUnsignedByte();

    boolean readBoolean();

    int readInt();

    int readVInt();

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#readVInt()}
     */
    @Deprecated
    int readVarInt();


    int readSVInt();

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#readSVInt()}
     */
    @Deprecated
    int readSVarInt();


    short readShort();

    long readLong();

    long readVLong();

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#readVLong()}
     */
    @Deprecated
    long readVarLong();

    long readSVLong();

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#readSVLong()}
     */
    @Deprecated
    long readSVarLong();

    double readDouble();

    double readVDouble();

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#readVDouble()}
     */
    @Deprecated
    double readVarDouble();

    double readSVDouble();

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#readSVDouble()}
     */
    @Deprecated
    double readSVarDouble();

    byte[] readPadBytes(int totalLength);

    String readPadString(int totalLength);

    String readPadStringAndRightTrim(int totalLength);

    byte[] readPrefixedBytes();

    byte[] read2PrefixedBytes();

    byte[] read4PrefixedBytes();

    String readPrefixedString();

    String read2PrefixedString();

    String read4PrefixedString();

    byte[] getBuffer();

    byte[] copyBuffer();

    byte[] getInternalBuffer();

    ByteBuffer wrapByteBuffer();

    void setOffset(int offset);

    int getOffset();

    /**
     * @deprecated Since 1.6.0. Use {@link Buffer#remaining()}
     */
    @Deprecated
    int limit();

    int remaining();

    boolean hasRemaining();

}
