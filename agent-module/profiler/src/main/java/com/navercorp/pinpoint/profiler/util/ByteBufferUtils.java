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

package com.navercorp.pinpoint.profiler.util;

import java.nio.ByteBuffer;

/**
 * @author Taejin Koo
 */
public class ByteBufferUtils {

    public static ByteBuffer createByteBuffer(int capacity) {
        return ByteBuffer.allocate(capacity);
    }

    public static void putShort(ByteBuffer byteBuffer, short value) {
        byteBuffer.put((byte) (value >> 8));
        byteBuffer.put((byte) (value));
    }

    public static int getRemaining(ByteBuffer[] byteBuffers) {

        if (com.navercorp.pinpoint.common.util.ArrayUtils.isEmpty(byteBuffers)) {
            return 0;
        }

        int remaining = 0;
        for (ByteBuffer byteBuffer : byteBuffers) {
            remaining += byteBuffer.remaining();
        }

        return remaining;
    }

}
