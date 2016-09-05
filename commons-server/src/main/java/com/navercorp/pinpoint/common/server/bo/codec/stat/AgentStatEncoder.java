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

package com.navercorp.pinpoint.common.server.bo.codec.stat;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class AgentStatEncoder<T extends AgentStatDataPoint> {

    private final AgentStatCodec<T> codec;

    public AgentStatEncoder(AgentStatCodec<T> codec) {
        this.codec = codec;
    }

    public ByteBuffer encodeQualifier(long timestampDelta) {
        // Variable-length encoding of 5 minutes (300000 ms) takes up max 3 bytes
        Buffer qualifierBuffer = new AutomaticBuffer(3);
        qualifierBuffer.putVLong(timestampDelta);
        return qualifierBuffer.wrapByteBuffer();
    }

    public ByteBuffer encodeValue(List<T> agentStatDataPoints) {
        Buffer valueBuffer = new AutomaticBuffer();
        valueBuffer.putByte(this.codec.getVersion());
        codec.encodeValues(valueBuffer, agentStatDataPoints);
        return valueBuffer.wrapByteBuffer();
    }
}
