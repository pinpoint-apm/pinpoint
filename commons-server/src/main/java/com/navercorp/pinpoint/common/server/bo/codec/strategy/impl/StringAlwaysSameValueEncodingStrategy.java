/*
 * Copyright 2017 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.bo.codec.StringTypedBufferHandler;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class StringAlwaysSameValueEncodingStrategy implements EncodingStrategy<String> {

    private static final byte CODE = 12;

    private final StringTypedBufferHandler bufferHandler;

    public StringAlwaysSameValueEncodingStrategy(StringTypedBufferHandler bufferHandler) {
        this.bufferHandler = bufferHandler;
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public void encodeValues(Buffer buffer, List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            throw new IllegalArgumentException("values may not be empty");
        }

        String initialValue = values.get(0);
        for (String value : values) {
            if (!isEquals(initialValue, value)) {
                throw new IllegalArgumentException("values must be all same value");
            }
        }

        bufferHandler.put(buffer, initialValue);
    }

    public boolean isEquals(String string1, String string2) {
        if (string1 == null) {
            return string2 == null;
        }

        return string1.equals(string2);
    }

    @Override
    public List<String> decodeValues(Buffer buffer, int numValues) {
        List<String> values = new ArrayList<String>(numValues);

        String value = bufferHandler.read(buffer);
        for (int i = 0; i < numValues; i++) {
            values.add(value);
        }

        return values;
    }

}
