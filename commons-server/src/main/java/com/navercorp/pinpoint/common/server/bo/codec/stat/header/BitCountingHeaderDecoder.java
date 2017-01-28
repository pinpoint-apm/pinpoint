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
 * Bit-counting implementation of {@link AgentStatHeaderDecoder} that decodes
 * each code by counting the number of set bits until the next unset bit.
 * If all bits are used up, the subsequent {@link #getCode()} invocations will
 * simply return 0.
 *
 * <p>For example, given a header of 100110, {@link #getCode()} will return 1,
 * 0, 2, and 0s afterwards.</p>
 *
 * @author HyunGil Jeong
 * @see BitCountingHeaderDecoder
 */
public class BitCountingHeaderDecoder implements AgentStatHeaderDecoder {

    private static final int NUM_BITS_PER_BYTE = 8;

    private final BitSet headerBitSet;
    private int position = 0;

    public BitCountingHeaderDecoder(byte[] header) {
        headerBitSet = new BitSet();
        // strictly follows JDK 7's BitSet.valueOf(byte[])
        for (int i = 0; i < header.length * NUM_BITS_PER_BYTE; i++) {
            byte currentBits = header[i / NUM_BITS_PER_BYTE];
            int bitMask = 1 << (i % NUM_BITS_PER_BYTE);
            if ((currentBits & bitMask) > 0) {
                headerBitSet.set(i);
            }
        }
        // use below when using JDK 7+
//        this.headerBitSet = BitSet.valueOf(header);
    }

    @Override
    public int getCode() {
        int fromIndex = this.position;
        int toIndex = this.headerBitSet.nextClearBit(this.position);
        int numBitsSet = this.headerBitSet.get(fromIndex, toIndex).cardinality();
        this.position = toIndex + 1;
        return numBitsSet;
    }
}
