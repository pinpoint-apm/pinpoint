/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.common.server.bo.codec.stat;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;

import java.util.List;

/**
 * @author minwoo.jung
 */
public abstract class ApplicationStatDecoder<T extends JoinStatBo> {

    private final List<ApplicationStatCodec<T>> codecs;

    public ApplicationStatDecoder(List<ApplicationStatCodec<T>> codecs) {
        this.codecs = codecs;
    }

    public long decodeQualifier(Buffer qualifierBuffer) {
        return qualifierBuffer.readVLong();
    }

    public List<T> decodeValue(Buffer valueBuffer, ApplicationStatDecodingContext decodingContext) {
        byte version = valueBuffer.readByte();
        for (ApplicationStatCodec<T> codec : this.codecs) {
            if (version == codec.getVersion()) {
                return codec.decodeValues(valueBuffer, decodingContext);
            }
        }
        throw new IllegalArgumentException("Unknown version : " + version);
    }
}
