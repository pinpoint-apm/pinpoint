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

package com.navercorp.pinpoint.common.server.bo.codec.stat.header;

import java.util.BitSet;

/**
 * Bit-counting implementation of {@link AgentStatHeaderEncoder} that encodes
 * each code by setting the number of bits equal to it followed by a 0.
 *
 * <p>For example, codes {1, 0, 2} will be encoded as 100110.
 *
 * <p>This implementation is best suited for very small code values (ideally
 * those that take less than a few bits to encode). It is very inefficient for
 * values larger than this, and other implementations should be used in place
 * of this.
 *
 * @author HyunGil Jeong
 * @see BitCountingHeaderDecoder
 */
public class BitCountingHeaderEncoder implements AgentStatHeaderEncoder {

    private static final int NUM_BITS_PER_BYTE = 8;

    private final BitSet headerBitSet = new BitSet();
    private int position = 0;

    @Override
    public void addCode(int code) {
        if (code < 0) {
            throw new IllegalArgumentException("code must be positive");
        }
        int fromIndex = this.position;
        int toIndex = this.position + code;
        this.headerBitSet.set(fromIndex, toIndex);
        this.position = toIndex + 1;
    }

    @Override
    public byte[] getHeader() {
        if (position == 0) {
            return new byte[0];
        }
        // strictly follows JDK 7's BitSet.toByteArray()
        int len = (headerBitSet.length() + (NUM_BITS_PER_BYTE - 1)) / NUM_BITS_PER_BYTE;
        byte[] header = new byte[len];
        for (int i = 0; i < len * NUM_BITS_PER_BYTE; ++i) {
            int index = i / NUM_BITS_PER_BYTE;
            int bitMask = (headerBitSet.get(i) ? 1 : 0) << (i % NUM_BITS_PER_BYTE);
            header[index] |= bitMask;
        }
        return header;
        // use below when using JDK 7+
//        return this.headerBitSet.toByteArray();
    }
}
