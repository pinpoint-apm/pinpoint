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

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class AgentStatDecoder<T extends AgentStatDataPoint> {

    private final AgentStatCodec<T>[] codecs;

    @SuppressWarnings("unchecked")
    public AgentStatDecoder(List<AgentStatCodec<T>> codecs) {
        Objects.requireNonNull(codecs, "codecs");
        this.codecs = codecs.toArray(new AgentStatCodec[0]);
    }

    public long decodeQualifier(Buffer qualifierBuffer) {
        return qualifierBuffer.readVLong();
    }

    public List<T> decodeValue(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
        byte version = valueBuffer.readByte();
        for (AgentStatCodec<T> codec : this.codecs) {
            if (version == codec.getVersion()) {
                return codec.decodeValues(valueBuffer, decodingContext);
            }
        }
        throw new IllegalArgumentException("Unknown version : " + version);
    }
}
