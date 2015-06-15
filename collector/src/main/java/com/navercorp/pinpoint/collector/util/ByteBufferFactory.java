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

package com.navercorp.pinpoint.collector.util;

import java.nio.ByteBuffer;

/**
 * @author emeroad
 */
public class ByteBufferFactory implements ObjectPoolFactory<ByteBuffer> {

    public static final int UDP_MAX_PACKET_LENGTH = 65507;

    private final int bufferLength;

    public ByteBufferFactory() {
        this(UDP_MAX_PACKET_LENGTH);
    }

    public ByteBufferFactory(int bufferLength) {
        if (bufferLength < 0 ) {
            throw new IllegalArgumentException("negative bufferLength:" + bufferLength);
        }
        this.bufferLength = bufferLength;
    }

    @Override
    public ByteBuffer create() {
        return ByteBuffer.allocate(bufferLength);
    }

    @Override
    public void beforeReturn(ByteBuffer packet) {
        packet.clear();
    }
}
