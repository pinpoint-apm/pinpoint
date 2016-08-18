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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v1;

import com.navercorp.pinpoint.common.server.bo.codec.stat.HeaderCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v1.strategy.UnsignedIntegerEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v1.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v1.strategy.UnsignedShortEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.util.BitFieldUtils;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
public abstract class HeaderCodecV1<T extends Number> implements HeaderCodec<T> {

    private static final int CODE_LENGTH_BIT_SIZE = 2;

    @Override
    public int getHeaderBitSize() {
        return CODE_LENGTH_BIT_SIZE;
    }

    @Override
    public int encodeHeader(int header, int position, EncodingStrategy<T> strategy) {
        int code = strategy.getCode();
        if (code < 0 || code > 3) {
            throw new IllegalArgumentException("code out of range (0~3)");
        }
        if (position == 0) {
            return header | code;
        } else {
            return header | (code << position);
        }
    }

    @Component
    public static class ShortHeaderCodecV1 extends HeaderCodecV1<Short> {
        @Override
        public EncodingStrategy<Short> decodeHeader(int header, int position) {
            int code = BitFieldUtils.getMultiBit(header, position, getHeaderBitSize());
            return UnsignedShortEncodingStrategy.getFromCode(code);
        }
    }

    @Component
    public static class IntegerHeaderCodecV1 extends HeaderCodecV1<Integer> {

        @Override
        public EncodingStrategy<Integer> decodeHeader(int header, int position) {
            int code = BitFieldUtils.getMultiBit(header, position, getHeaderBitSize());
            return UnsignedIntegerEncodingStrategy.getFromCode(code);
        }
    }

    @Component
    public static class LongHeaderCodecV1 extends HeaderCodecV1<Long> {

        @Override
        public EncodingStrategy<Long> decodeHeader(int header, int position) {
            int code = BitFieldUtils.getMultiBit(header, position, getHeaderBitSize());
            return UnsignedLongEncodingStrategy.getFromCode(code);
        }
    }
}
