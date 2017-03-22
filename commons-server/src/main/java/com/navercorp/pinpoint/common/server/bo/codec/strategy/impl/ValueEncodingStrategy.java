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

package com.navercorp.pinpoint.common.server.bo.codec.strategy.impl;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.TypedBufferHandler;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public abstract class ValueEncodingStrategy<T extends Number> implements EncodingStrategy<T> {

    private static final byte CODE = 0;

    protected final TypedBufferHandler<T> bufferHandler;

    protected ValueEncodingStrategy(TypedBufferHandler<T> bufferHandler) {
        this.bufferHandler = bufferHandler;
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    public static class Unsigned<T extends Number> extends ValueEncodingStrategy<T> {

        public Unsigned(TypedBufferHandler<T> bufferHandler) {
            super(bufferHandler);
        }

        @Override
        public void encodeValues(Buffer buffer, List<T> values) {
            for (T value : values) {
                this.bufferHandler.putV(buffer, value);
            }
        }

        @Override
        public List<T> decodeValues(Buffer buffer, int numValues) {
            List<T> values = new ArrayList<T>(numValues);
            for (int i = 0; i < numValues; ++i) {
                values.add(this.bufferHandler.readV(buffer));
            }
            return values;
        }
    }
}
