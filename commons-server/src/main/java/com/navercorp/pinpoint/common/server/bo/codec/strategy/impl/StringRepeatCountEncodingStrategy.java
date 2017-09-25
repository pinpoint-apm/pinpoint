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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class StringRepeatCountEncodingStrategy implements EncodingStrategy<String> {

    private static final byte CODE = 11;

    private final StringTypedBufferHandler bufferHandler;

    public StringRepeatCountEncodingStrategy(StringTypedBufferHandler bufferHandler) {
        this.bufferHandler = bufferHandler;
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public void encodeValues(Buffer buffer, List<String> values) {
        StringReference previousValueReference = null;
        int count = 0;
        for (String value : values) {
            if (previousValueReference == null || !StringUtils.equals(value, previousValueReference.get())) {
                if (previousValueReference != null) {
                    buffer.putVInt(count);
                    this.bufferHandler.put(buffer, previousValueReference.get());
                }
                previousValueReference = new StringReference(value);
                count = 1;
            } else {
                count++;
            }
        }
        if (count > 0 && previousValueReference != null) {
            buffer.putVInt(count);
            this.bufferHandler.put(buffer, previousValueReference.get());
        }
    }

    @Override
    public List<String> decodeValues(Buffer buffer, int numValues) {
        List<String> values = new ArrayList<String>(numValues);
        int totalCount = 0;
        while (totalCount < numValues) {
            int count = buffer.readVInt();
            String value = this.bufferHandler.read(buffer);
            for (int i = 0; i < count; i++) {
                values.add(value);
                totalCount++;
            }
        }
        return values;
    }

    private static class StringReference {

        private final String value;

        public StringReference(String value) {
            this.value = value;
        }

        public String get() {
            return value;
        }

    }

}
