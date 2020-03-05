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

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v2.JvmGcCodecV2;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Component("jvmGcCodecV1")
public class JvmGcCodecV1 implements AgentStatCodec<JvmGcBo> {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public JvmGcCodecV1(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JvmGcBo> jvmGcBos) {
        if (CollectionUtils.isEmpty(jvmGcBos)) {
            throw new IllegalArgumentException("jvmGcBos must not be empty");
        }
        final int gcTypeCode = jvmGcBos.get(0).getGcType().getTypeCode();
        valueBuffer.putVInt(gcTypeCode);
        final int numValues = jvmGcBos.size();
        valueBuffer.putVInt(numValues);

        List<Long> timestamps = new ArrayList<Long>(numValues);
        JvmGcCodecV2.JvmGcCodecEncoder encoder = new JvmGcCodecV2.JvmGcCodecEncoder(codec);

        for (JvmGcBo jvmGcBo : jvmGcBos) {
            timestamps.add(jvmGcBo.getTimestamp());
            encoder.addValue(jvmGcBo);
        }

        this.codec.encodeTimestamps(valueBuffer, timestamps);
        encoder.encode(valueBuffer);
    }

    @Override
    public List<JvmGcBo> decodeValues(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
        final String agentId = decodingContext.getAgentId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        final JvmGcType gcType = JvmGcType.getTypeByCode(valueBuffer.readVInt());
        int numValues = valueBuffer.readVInt();
        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);


        // decode headers
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);

        JvmGcCodecV2.JvmGcCodecDecoder decoder = new JvmGcCodecV2.JvmGcCodecDecoder(codec);
        decoder.decode(valueBuffer, headerDecoder, numValues);

        List<JvmGcBo> jvmGcBos = new ArrayList<JvmGcBo>(numValues);
        for (int i = 0; i < numValues; i++) {
            JvmGcBo jvmGcBo = decoder.getValue(i);
            jvmGcBo.setAgentId(agentId);
            jvmGcBo.setTimestamp(timestamps.get(i));
            jvmGcBo.setGcType(gcType);
            jvmGcBos.add(jvmGcBo);
        }
        return jvmGcBos;
    }
}
