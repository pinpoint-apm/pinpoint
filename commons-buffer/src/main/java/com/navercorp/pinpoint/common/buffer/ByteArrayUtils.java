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

import com.navercorp.pinpoint.common.util.BytesUtils;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Arrays;

public final class ByteArrayUtils {
    public static final int SHORT_BYTE_LENGTH = 2;
    public static final int INT_BYTE_LENGTH = 4;
    public static final int LONG_BYTE_LENGTH = 8;

    public static final VarHandle BYTE_ARRAY_SHORT = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.BIG_ENDIAN);
    public static final VarHandle BYTE_ARRAY_INT = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN);
    public static final VarHandle BYTE_ARRAY_LONG = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);

    private ByteArrayUtils() {
    }

    public static long bytesToLong(final byte[] buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        BytesUtils.checkBounds(buf, offset, LONG_BYTE_LENGTH);

        return (long) BYTE_ARRAY_LONG.get(buf, offset);
    }

    public static int writeLong(final long value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        BytesUtils.checkBounds(buf, offset, LONG_BYTE_LENGTH);

        BYTE_ARRAY_LONG.set(buf, offset, value);
        return offset + LONG_BYTE_LENGTH;
    }

    public static int bytesToInt(final byte[] buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        BytesUtils.checkBounds(buf, offset, INT_BYTE_LENGTH);

        return (int) BYTE_ARRAY_INT.get(buf, offset);
    }

    public static int writeInt(final int value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        BytesUtils.checkBounds(buf, offset, INT_BYTE_LENGTH);

        BYTE_ARRAY_INT.set(buf, offset, value);

        return offset + INT_BYTE_LENGTH;
    }

    public static short bytesToShort(final byte[] buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        BytesUtils.checkBounds(buf, offset, SHORT_BYTE_LENGTH);

        return (short) BYTE_ARRAY_SHORT.get(buf, offset);
    }

    public static int writeShort(final short value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        BytesUtils.checkBounds(buf, offset, SHORT_BYTE_LENGTH);

        BYTE_ARRAY_SHORT.set(buf, offset, value);

        return offset + SHORT_BYTE_LENGTH;
    }

    /**
     * Compares two byte arrays starting from the specified offset.
     *
     * @param bytes1 the first byte array to compare
     * @param bytes2 the second byte array to compare
     * @param offset the starting position in both arrays for the comparison
     * @return a negative integer, zero, or a positive integer as the first array is less than,
     *         equal to, or greater than the second array
     * @throws NullPointerException if either {@code bytes1} or {@code bytes2} is {@code null}
     * @throws IndexOutOfBoundsException if the offset is out of bounds for either array
     */
    public static int compare(byte[] bytes1, byte[] bytes2, int offset) {
        if (bytes1 == null) {
            throw new NullPointerException("bytes1");
        }
        if (bytes2 == null) {
            throw new NullPointerException("bytes2");
        }
        return Arrays.compare(bytes1, offset, bytes1.length, bytes2, offset, bytes2.length);
    }

}