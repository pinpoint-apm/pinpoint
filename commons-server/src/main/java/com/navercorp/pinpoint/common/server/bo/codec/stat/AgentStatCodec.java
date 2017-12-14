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
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public interface AgentStatCodec<T> {

    byte getVersion();


    void encodeValues(Buffer valueBuffer, List<T> agentStatDataPoints);

    List<T> decodeValues(Buffer valueBuffer, AgentStatDecodingContext decodingContext);


    interface CodecEncoder<T> {

        void addValue(T agentStatDataPoint);

        void encode(Buffer valueBuffer);

    }

    interface CodecDecoder<T> {

        void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize);

        T getValue(int index);

    }

}
