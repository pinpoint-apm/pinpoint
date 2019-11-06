/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Taejin Koo
 */
public class DirectByteBufferFactory implements ByteBufferFactory {

    @Override
    public ByteBuffer getBuffer(int capacity) {
        return getBuffer(DEFAULT_BYTE_ORDER, capacity);
    }

    @Override
    public ByteBuffer getBuffer(ByteOrder endianness, int capacity) {
        return ByteBuffer.allocateDirect(capacity).order(endianness);
    }

}
