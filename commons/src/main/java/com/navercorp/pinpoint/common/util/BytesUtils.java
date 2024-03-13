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

package com.navercorp.pinpoint.common.util;


import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author emeroad
 */
public final class BytesUtils {
    public static final int SHORT_BYTE_LENGTH = 2;
    public static final int INT_BYTE_LENGTH = 4;
    public static final int LONG_BYTE_LENGTH = 8;
    public static final int UUID_BYTE_LENGTH = 16;
    public static final int LONG_LONG_BYTE_LENGTH = 16;

    public static final int VLONG_MAX_SIZE = 10;
    public static final int VINT_MAX_SIZE = 5;

    private static final byte[] EMPTY_BYTES = new byte[0];

    private static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    private static final String UTF8 = StandardCharsets.UTF_8.name();

    private BytesUtils() {
    }

    public static byte[] stringLongLongToBytes(final String string, final int maxStringSize, final long value1, final long value2) {
        if (string == null) {
            throw new NullPointerException("string");
        }
        if (maxStringSize < 0) {
            throw new StringIndexOutOfBoundsException(maxStringSize);
        }
        final byte[] stringBytes = toBytes(string);
        if (stringBytes.length > maxStringSize) {
            throw new StringIndexOutOfBoundsException("string is max " + stringBytes.length + ", string='" + string + "'");
        }
        final byte[] buffer = new byte[LONG_LONG_BYTE_LENGTH + maxStringSize];
        writeBytes(buffer, 0, stringBytes);
        writeLong(value1, buffer, maxStringSize);
        writeLong(value2, buffer, maxStringSize + LONG_BYTE_LENGTH);
        return buffer;
    }

    public static int writeBytes(final byte[] buffer, int bufferOffset, final byte[] srcBytes) {
        if (srcBytes == null) {
            throw new NullPointerException("srcBytes");
        }
        return writeBytes(buffer, bufferOffset, srcBytes, 0, srcBytes.length);
    }

    public static int writeBytes(final byte[] buffer, final int bufferOffset, final byte[] srcBytes, final int srcOffset, final int srcLength) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        if (srcBytes == null) {
            throw new NullPointerException("srcBytes");
        }
        if (bufferOffset < 0) {
            throw new ArrayIndexOutOfBoundsException(bufferOffset);
        }
        if (srcOffset < 0) {
            throw new ArrayIndexOutOfBoundsException(srcOffset);
        }
        System.arraycopy(srcBytes, srcOffset, buffer, bufferOffset, srcLength);
        return bufferOffset + srcLength;
    }

    public static long bytesToLong(final byte[] buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, LONG_BYTE_LENGTH);

        final long rv = (((long) buf[offset] & 0xff) << 56)
                | (((long) buf[offset + 1] & 0xff) << 48)
                | (((long) buf[offset + 2] & 0xff) << 40)
                | (((long) buf[offset + 3] & 0xff) << 32)
                | (((long) buf[offset + 4] & 0xff) << 24)
                | (((long) buf[offset + 5] & 0xff) << 16)
                | (((long) buf[offset + 6] & 0xff) << 8)
                | (((long) buf[offset + 7] & 0xff));
        return rv;
    }

    public static int bytesToInt(final byte[] buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, INT_BYTE_LENGTH);

        final int v = ((buf[offset] & 0xff) << 24)
                | ((buf[offset + 1] & 0xff) << 16)
                | ((buf[offset + 2] & 0xff) << 8)
                | ((buf[offset + 3] & 0xff));

        return v;
    }

    public static short bytesToShort(final byte[] buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, SHORT_BYTE_LENGTH);

        final short v = (short) (((buf[offset] & 0xff) << 8) | ((buf[offset + 1] & 0xff)));

        return v;
    }

    public static UUID bytesToUUID(final byte[] buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, UUID_BYTE_LENGTH);

        long msb = bytesToLong(buf, offset);
        long lsb = bytesToLong(buf, offset + 8);
        return new UUID(msb, lsb);
    }


     public static int bytesToSVar32(final byte[] buffer, final int offset) {
        return zigzagToInt(bytesToVar32(buffer, offset));
    }

    public static int bytesToVar32(final byte[] buffer, final int offset) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        checkBounds(buffer, offset);

        // borrowing the protocol buffer's concept of variable-length encoding
        // copy https://github.com/google/protobuf 2.6.1
        // CodedInputStream.java -> int readRawVarint32()

        // See implementation notes for readRawVarint64
        fastpath: {
            int pos = offset;
            final int bufferSize = buffer.length;
            if (bufferSize == pos) {
                break fastpath;
            }

            int x;
            if ((x = buffer[pos++]) >= 0) {
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
                        buffer[pos] < 0) {
                    break fastpath;  // Will throw malformedVarint()
                }
            }

            return x;
        }
        return (int) readVar64SlowPath(buffer, offset);
    }

    public static long bytesToSVar64(final byte[] buffer, final int offset) {
        return zigzagToLong(bytesToVar64(buffer, offset));
    }

    public static long bytesToVar64(final byte[] buffer, final int offset) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        checkBounds(buffer, offset);
        // borrowing the protocol buffer's concept of variable-length encoding
        // copy https://github.com/google/protobuf 2.6.1
        // CodedInputStream.java -> int readRawVarint32()

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
            int bufferSize = buffer.length;
            if (bufferSize == pos) {
                break fastpath;
            }

            long x;
            int y;
            if ((y = buffer[pos++]) >= 0) {
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
                    if (buffer[pos] < 0L) {
                        break fastpath;  // Will throw malformedVarint()
                    }
                }
            }
            return x;
        }
        return readVar64SlowPath(buffer, offset);
    }

    /** Variant of readRawVarint64 for when uncomfortably close to the limit. */
    /* Visible for testing */
    static long readVar64SlowPath(final byte[] buffer, int offset) {

        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            final byte b = buffer[offset++];
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        throw new ArrayIndexOutOfBoundsException("invalid varLong. start offset:" +  offset + " readOffset:" + offset);
    }

    public static short bytesToShort(final byte byte1, final byte byte2) {
        return (short) (((byte1 & 0xff) << 8) | ((byte2 & 0xff)));
    }


    public static int writeLong(final long value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, LONG_BYTE_LENGTH);

        buf[offset++] = (byte) (value >> 56);
        buf[offset++] = (byte) (value >> 48);
        buf[offset++] = (byte) (value >> 40);
        buf[offset++] = (byte) (value >> 32);
        buf[offset++] = (byte) (value >> 24);
        buf[offset++] = (byte) (value >> 16);
        buf[offset++] = (byte) (value >> 8);
        buf[offset++] = (byte) (value);
        return offset;
    }


    public static int writeShort(final short value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, SHORT_BYTE_LENGTH);

        buf[offset++] = (byte) (value >> 8);
        buf[offset++] = (byte) (value);
        return offset;
    }

    public static int writeInt(final int value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, INT_BYTE_LENGTH);

        buf[offset++] = (byte) (value >> 24);
        buf[offset++] = (byte) (value >> 16);
        buf[offset++] = (byte) (value >> 8);
        buf[offset++] = (byte) (value);
        return offset;
    }

    public static int writeSVar32(final int value, final byte[] buf, final int offset) {
        return writeVar32(intToZigZag(value), buf, offset);
    }

    public static int writeVar32(int value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset);
        while (true) {
            if ((value & ~0x7F) == 0) {
                buf[offset++] = (byte)value;
                return offset;
            } else {
                buf[offset++] = (byte)((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    public static int shortToUnsignedShort(short value) {
        return value & 0xffff;
    }

    public static byte[] intToSVar32(int value) {
        return intToVar32(intToZigZag(value));
    }

    public static byte[] intToVar32(int value) {
        final int bufferSize = BytesUtils.computeVar32Size(value);
        final byte[] buffer = new byte[bufferSize];
        writeVar32(value, buffer, 0);
        return buffer;
    }

    /**
     * copy google protocol buffer
     * https://github.com/google/protobuf/blob/master/java/src/main/java/com/google/protobuf/CodedOutputStream.java
     */
    public static int computeVar32Size(final int value) {
        if ((value & (0xffffffff <<  7)) == 0) return 1;
        if ((value & (0xffffffff << 14)) == 0) return 2;
        if ((value & (0xffffffff << 21)) == 0) return 3;
        if ((value & (0xffffffff << 28)) == 0) return 4;
        return 5;
    }


    public static int writeSVar64(final int value, final byte[] buf, final int offset) {
        return writeVar64(longToZigZag(value), buf, offset);
    }

    /**
     * copy google protocol buffer
     * https://github.com/google/protobuf/blob/master/java/src/main/java/com/google/protobuf/CodedOutputStream.java
     */
    public static int writeVar64(long value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset);

        while (true) {
            if ((value & ~0x7FL) == 0) {
                buf[offset++] = (byte)value;
                return offset;
            } else {
                buf[offset++] = (byte)(((int)value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    static void checkBounds(byte[] bytes, final int offset) {
        checkBounds(bytes, offset, bytes.length - offset);
    }

    static void checkBounds(byte[] bytes, int offset, int length) {
        if (length < 0) {
            throw new ArrayIndexOutOfBoundsException(length);
        }
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException(offset);
        }
        if (offset > bytes.length - length) {
            throw new ArrayIndexOutOfBoundsException(offset + length);
        }
    }

    public static byte[] longToSVar64(long value) {
        return longToVar64(longToZigZag(value));
    }

    public static byte[] longToVar64(long value) {
        final int bufferSize = BytesUtils.computeVar64Size(value);
        final byte[] buffer = new byte[bufferSize];
        writeVar64(value, buffer, 0);
        return buffer;
    }

    /**
     * copy google protocol buffer
     * https://github.com/google/protobuf/blob/master/java/src/main/java/com/google/protobuf/CodedOutputStream.java
     */
    public static int computeVar64Size(final long value) {
        if ((value & (0xffffffffffffffffL <<  7)) == 0) return 1;
        if ((value & (0xffffffffffffffffL << 14)) == 0) return 2;
        if ((value & (0xffffffffffffffffL << 21)) == 0) return 3;
        if ((value & (0xffffffffffffffffL << 28)) == 0) return 4;
        if ((value & (0xffffffffffffffffL << 35)) == 0) return 5;
        if ((value & (0xffffffffffffffffL << 42)) == 0) return 6;
        if ((value & (0xffffffffffffffffL << 49)) == 0) return 7;
        if ((value & (0xffffffffffffffffL << 56)) == 0) return 8;
        if ((value & (0xffffffffffffffffL << 63)) == 0) return 9;
        return 10;
    }

    public static byte[] add(final String prefix, final long postfix) {
        if (prefix == null) {
            throw new NullPointerException("prefix");
        }
        byte[] agentByte = toBytes(prefix);
        return add(agentByte, postfix);
    }

    public static byte[] add(final byte[] prefix, final long postfix) {
        if (prefix == null) {
            throw new NullPointerException("prefix");
        }
        byte[] buf = new byte[prefix.length + LONG_BYTE_LENGTH];
        System.arraycopy(prefix, 0, buf, 0, prefix.length);
        writeLong(postfix, buf, prefix.length);
        return buf;
    }

    public static byte[] add(final byte[] prefix, final short postfix) {
        if (prefix == null) {
            throw new NullPointerException("prefix");
        }
        byte[] buf = new byte[prefix.length + SHORT_BYTE_LENGTH];
        System.arraycopy(prefix, 0, buf, 0, prefix.length);
        writeShort(postfix, buf, prefix.length);
        return buf;
    }
    
    public static byte[] add(final byte[] prefix, final int postfix) {
        if (prefix == null) {
            throw new NullPointerException("prefix");
        }
        byte[] buf = new byte[prefix.length + INT_BYTE_LENGTH];
        System.arraycopy(prefix, 0, buf, 0, prefix.length);
        writeInt(postfix, buf, prefix.length);
        return buf;
    }

    public static byte[] add(final int prefix, final short postFix) {
        byte[] buf = new byte[INT_BYTE_LENGTH + SHORT_BYTE_LENGTH];
        writeInt(prefix, buf, 0);
        writeShort(postFix, buf, INT_BYTE_LENGTH);
        return buf;
    }


    public static byte[] add(final long prefix, final short postFix) {
        byte[] buf = new byte[LONG_BYTE_LENGTH + SHORT_BYTE_LENGTH];
        writeLong(prefix, buf, 0);
        writeShort(postFix, buf, LONG_BYTE_LENGTH);
        return buf;
    }


    public static byte[] toBytes(final String value) {
        if (value == null) {
            return null;
        }
        try {
            return value.getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            return value.getBytes(UTF8_CHARSET);
        }
    }

    public static byte[] toFixedLengthBytes(final String str, final int length) {
        if (length < 0) {
            throw new ArrayIndexOutOfBoundsException(length);
        }
        final byte[] b1 = toBytes(str);
        if (b1 == null) {
            return new byte[length];
        }

        if (b1.length > length) {
            throw new StringIndexOutOfBoundsException("String is longer then target length of bytes.");
        }
        byte[] b = new byte[length];
        System.arraycopy(b1, 0, b, 0, b1.length);

        return b;
    }


    public static int intToZigZag(final int n) {
        return (n << 1) ^ (n >> 31);
    }

    public static int zigzagToInt(final int n) {
        return (n >>> 1) ^ -(n & 1);
    }


    public static long longToZigZag(final long n) {
        return (n << 1) ^ (n >> 63);
    }

    public static long zigzagToLong(final long n) {
        return (n >>> 1) ^ -(n & 1);
    }

    public static byte[] concat(final byte[]... arrays) {
        if (arrays == null) {
            throw new NullPointerException("arrays");
        }
        final int totalLength = getTotalLength(arrays);

        byte[] result = new byte[totalLength];

        int currentIndex = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }

        return result;
    }

    private static int getTotalLength(byte[][] arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }
        return totalLength;
    }


    public static byte[] concat(final byte[] b1, final byte[] b2) {
        if (b1 == null) {
            throw new NullPointerException("b1");
        }
        if (b2 == null) {
            throw new NullPointerException("b2");
        }
        final byte[] result = new byte[b1.length + b2.length];

        System.arraycopy(b1, 0, result, 0, b1.length);
        System.arraycopy(b2, 0, result, b1.length, b2.length);

        return result;
    }

    public static String safeTrim(final String string) {
        if (string == null) {
            return null;
        }
        return string.trim();
    }

    public static String toString(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return toString(bytes, 0, bytes.length);
    }

    public static String toString(final byte[] bytes, final int offset, final int length) {
        if (bytes == null) {
            return null;
        }
        checkBounds(bytes, offset, length);
        if (length == 0) {
            return "";
        }
        try {
            return new String(bytes, offset, length, UTF8);
        } catch (UnsupportedEncodingException e) {
            return new String(bytes, offset, length, UTF8_CHARSET);
        }
    }

    public static String toStringAndRightTrim(final byte[] bytes, final int offset, final int length) {
        final int rTrimIndex = rightTrimIndex(bytes, offset, length);
        if (rTrimIndex == -1) {
            return null;
        }
        return toString(bytes, offset, rTrimIndex - offset);
    }

    static int rightTrimIndex(final byte[] string, final int offset, final int length) {
        if (string == null) {
            return -1;
        }
        checkBounds(string, offset, length);

        int index = offset + length - 1;
        while (index >= offset && string[index] <= ' ') {
            index--;
        }
        return index + 1;
    }

    public static String rightTrim(final String string) {
        if (string == null) {
            return null;
        }
        if (string.isEmpty()) {
            return "";
        }
        final int length = string.length();
        int index = length - 1;
        while (index >= 0 && string.charAt(index) <= ' ') {
            index--;
        }
        index++;

        if (index == length) {
            return string;
        } else {
            return string.substring(0, index);
        }
    }
}
