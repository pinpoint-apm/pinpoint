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

package com.navercorp.pinpoint.common.server.bo.codec.stat.header;

import java.util.BitSet;

/**
 * JDK 7 implementation of {@link BitCountingHeaderDecoder}.
 *
 * @author HyunGil Jeong
 */
public class Jdk7BitCountingHeaderDecoder implements AgentStatHeaderDecoder {

    private final BitSet headerBitSet;
    private int position = 0;

    public Jdk7BitCountingHeaderDecoder(byte[] header) {
        headerBitSet = BitSet.valueOf(header);
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
