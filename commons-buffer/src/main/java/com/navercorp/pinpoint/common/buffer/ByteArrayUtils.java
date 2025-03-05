/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.buffer;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

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
        checkBounds(buf, offset, LONG_BYTE_LENGTH);

        return (long) BYTE_ARRAY_LONG.get(buf, offset);
    }

    public static int writeLong(final long value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, LONG_BYTE_LENGTH);

        BYTE_ARRAY_LONG.set(buf, offset, value);
        return offset + LONG_BYTE_LENGTH;
    }

    public static int bytesToInt(final byte[] buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, INT_BYTE_LENGTH);

        return (int) BYTE_ARRAY_INT.get(buf, offset);
    }

    public static int writeInt(final int value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, INT_BYTE_LENGTH);
        BYTE_ARRAY_INT.set(buf, offset, value);

        return offset + INT_BYTE_LENGTH;
    }

    public static short bytesToShort(final byte[] buf, final int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, SHORT_BYTE_LENGTH);

        return (short) BYTE_ARRAY_SHORT.get(buf, offset);
    }

    public static int writeShort(final short value, final byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        checkBounds(buf, offset, SHORT_BYTE_LENGTH);

        BYTE_ARRAY_SHORT.set(buf, offset, value);

        return offset + SHORT_BYTE_LENGTH;
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
}