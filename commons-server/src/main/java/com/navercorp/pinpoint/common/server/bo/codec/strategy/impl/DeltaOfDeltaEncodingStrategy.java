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
import com.navercorp.pinpoint.common.server.bo.codec.ArithmeticOperation;
import com.navercorp.pinpoint.common.server.bo.codec.TypedBufferHandler;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public abstract class DeltaOfDeltaEncodingStrategy<T extends Number> implements EncodingStrategy<T> {

    private static final byte CODE = 3;

    protected final TypedBufferHandler<T> bufferHandler;
    protected final ArithmeticOperation<T> operation;

    protected DeltaOfDeltaEncodingStrategy(TypedBufferHandler<T> bufferHandler, ArithmeticOperation<T> operation) {
        this.bufferHandler = bufferHandler;
        this.operation = operation;
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    public static class Unsigned<T extends Number> extends DeltaOfDeltaEncodingStrategy<T> {

        public Unsigned(TypedBufferHandler<T> bufferHandler, ArithmeticOperation<T> operation) {
            super(bufferHandler, operation);
        }

        @Override
        public void encodeValues(Buffer buffer, List<T> values) {
            if (values.isEmpty()) {
                return;
            }
            T initialValue = values.get(0);
            this.bufferHandler.putV(buffer, initialValue);
            T previousValue = initialValue;
            T previousDelta = this.operation.zero();
            // skip first value as this value is stored without compression
            for (int i = 1; i < values.size(); ++i) {
                T value = values.get(i);
                T delta = this.operation.diff(value, previousValue);
                this.bufferHandler.putSV(buffer, this.operation.diff(delta, previousDelta));
                previousValue = value;
                previousDelta = delta;
            }
        }

        @Override
        public List<T> decodeValues(Buffer buffer, int numValues) {
            if (numValues < 1) {
                return Collections.emptyList();
            }
            List<T> values = new ArrayList<T>(numValues);
            T initialValue = this.bufferHandler.readV(buffer);
            values.add(initialValue);
            T previousValue = initialValue;
            T previousDelta = this.operation.zero();
            // loop through numValues - 1 as the first value is simply read from buffer
            for (int i = 0; i < numValues - 1; ++i) {
                T delta = this.operation.add(previousDelta, this.bufferHandler.readSV(buffer));
                T value = this.operation.add(previousValue, delta);
                values.add(value);
                previousValue = value;
                previousDelta = delta;
            }
            return values;
        }
    }
}
