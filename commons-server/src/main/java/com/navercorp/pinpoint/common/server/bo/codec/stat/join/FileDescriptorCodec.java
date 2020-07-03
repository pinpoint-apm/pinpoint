/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.common.server.bo.codec.stat.join;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.JoinLongFieldEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.JoinLongFieldStrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Roy Kim
 */
@Component("joinFileDescriptorCodec")
public class FileDescriptorCodec implements ApplicationStatCodec {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public FileDescriptorCodec(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "agentStatDataPointCodec");
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinFileDescriptorBoList) {
        if (CollectionUtils.isEmpty(joinFileDescriptorBoList)) {
            throw new IllegalArgumentException("fileDescriptorBoList must not be empty");
        }

        final int numValues = joinFileDescriptorBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        JoinLongFieldStrategyAnalyzer.Builder openFileDescriptorCountAnalyzerBuilder = new JoinLongFieldStrategyAnalyzer.Builder();

        for (JoinStatBo joinStatBo : joinFileDescriptorBoList) {
            JoinFileDescriptorBo joinFileDescriptorBo = (JoinFileDescriptorBo) joinStatBo;
            timestamps.add(joinFileDescriptorBo.getTimestamp());
            openFileDescriptorCountAnalyzerBuilder.addValue(joinFileDescriptorBo.getOpenFdCountJoinValue());
        }
        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer, openFileDescriptorCountAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer, JoinLongFieldStrategyAnalyzer openFileDescriptorCountAnalyzer) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();

        final byte[] codes = openFileDescriptorCountAnalyzer.getBestStrategy().getCodes();
        for (byte code : codes) {
            headerEncoder.addCode(code);
        }

        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);
        // encode values
        this.codec.encodeValues(valueBuffer, openFileDescriptorCountAnalyzer.getBestStrategy(), openFileDescriptorCountAnalyzer.getValues());
    }

    @Override
    public List<JoinStatBo> decodeValues(Buffer valueBuffer, ApplicationStatDecodingContext decodingContext) {
        final String id = decodingContext.getApplicationId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        int numValues = valueBuffer.readVInt();
        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);
        JoinLongFieldEncodingStrategy openFileDescriptorCountEncodingStrategy = JoinLongFieldEncodingStrategy.getFromCode(headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode());

        // decode values
        final List<JoinLongFieldBo> openFileDescriptorCounts = this.codec.decodeValues(valueBuffer, openFileDescriptorCountEncodingStrategy, numValues);

        List<JoinStatBo> joinFileDescriptorBoList = new ArrayList<JoinStatBo>(numValues);
        for (int i = 0; i < numValues; i++) {
            JoinFileDescriptorBo joinFileDescriptorBo = new JoinFileDescriptorBo();
            joinFileDescriptorBo.setId(id);
            joinFileDescriptorBo.setTimestamp(timestamps.get(i));
            joinFileDescriptorBo.setOpenFdCountJoinValue(openFileDescriptorCounts.get(i));
            joinFileDescriptorBoList.add(joinFileDescriptorBo);
        }
        return joinFileDescriptorBoList;
    }
}
