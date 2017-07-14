/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec;

import com.navercorp.pinpoint.common.buffer.Buffer;

/**
 * @author HyunGil Jeong
 */
public interface TypedBufferHandler<T extends Number> {
    void put(Buffer buffer, T value);
    void putV(Buffer buffer, T value);
    void putSV(Buffer buffer, T value);
    T read(Buffer buffer);
    T readV(Buffer buffer);
    T readSV(Buffer buffer);

    TypedBufferHandler<Short> SHORT_BUFFER_HANDLER = new TypedBufferHandler<Short>() {

        @Override
        public void put(Buffer buffer, Short value) {
            buffer.putShort(value);
        }

        @Override
        public void putV(Buffer buffer, Short value) {
            buffer.putShort(value);
        }

        @Override
        public void putSV(Buffer buffer, Short value) {
            buffer.putShort(value);
        }

        @Override
        public Short read(Buffer buffer) {
            return buffer.readShort();
        }

        @Override
        public Short readV(Buffer buffer) {
            return buffer.readShort();
        }

        @Override
        public Short readSV(Buffer buffer) {
            return buffer.readShort();
        }
    };

    TypedBufferHandler<Integer> INTEGER_BUFFER_HANDLER = new TypedBufferHandler<Integer>() {

        @Override
        public void put(Buffer buffer, Integer value) {
            buffer.putInt(value);
        }

        @Override
        public void putV(Buffer buffer, Integer value) {
            buffer.putVInt(value);
        }

        @Override
        public void putSV(Buffer buffer, Integer value) {
            buffer.putSVInt(value);
        }

        @Override
        public Integer read(Buffer buffer) {
            return buffer.readInt();
        }

        @Override
        public Integer readV(Buffer buffer) {
            return buffer.readVInt();
        }

        @Override
        public Integer readSV(Buffer buffer) {
            return buffer.readSVInt();
        }
    };

    TypedBufferHandler<Long> LONG_BUFFER_HANDLER = new TypedBufferHandler<Long>() {

        @Override
        public void put(Buffer buffer, Long value) {
            buffer.putLong(value);
        }

        @Override
        public void putV(Buffer buffer, Long value) {
            buffer.putVLong(value);
        }

        @Override
        public void putSV(Buffer buffer, Long value) {
            buffer.putSVLong(value);
        }

        @Override
        public Long read(Buffer buffer) {
            return buffer.readLong();
        }

        @Override
        public Long readV(Buffer buffer) {
            return buffer.readVLong();
        }

        @Override
        public Long readSV(Buffer buffer) {
            return buffer.readSVLong();
        }
    };
}
