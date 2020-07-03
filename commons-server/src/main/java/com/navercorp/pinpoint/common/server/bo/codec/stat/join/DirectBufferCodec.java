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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;
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
@Component("joinDirectBufferCodec")
public class DirectBufferCodec implements ApplicationStatCodec {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public DirectBufferCodec(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "agentStatDataPointCodec");
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinDirectBufferBoList) {
        if (CollectionUtils.isEmpty(joinDirectBufferBoList)) {
            throw new IllegalArgumentException("directBufferBoList must not be empty");
        }

        final int numValues = joinDirectBufferBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        JoinLongFieldStrategyAnalyzer.Builder directCountAnalyzerBuilder = new JoinLongFieldStrategyAnalyzer.Builder();
        JoinLongFieldStrategyAnalyzer.Builder directMemoryUsedAnalyzerBuilder = new JoinLongFieldStrategyAnalyzer.Builder();
        JoinLongFieldStrategyAnalyzer.Builder mappedCountAnalyzerBuilder = new JoinLongFieldStrategyAnalyzer.Builder();
        JoinLongFieldStrategyAnalyzer.Builder mappedMemoryUsedAnalyzerBuilder = new JoinLongFieldStrategyAnalyzer.Builder();

        for (JoinStatBo joinStatBo : joinDirectBufferBoList) {
            JoinDirectBufferBo joinDirectBufferBo = (JoinDirectBufferBo) joinStatBo;
            timestamps.add(joinDirectBufferBo.getTimestamp());
            directCountAnalyzerBuilder.addValue(joinDirectBufferBo.getDirectCountJoinValue());
            directMemoryUsedAnalyzerBuilder.addValue(joinDirectBufferBo.getDirectMemoryUsedJoinValue());
            mappedCountAnalyzerBuilder.addValue(joinDirectBufferBo.getMappedCountJoinValue());
            mappedMemoryUsedAnalyzerBuilder.addValue(joinDirectBufferBo.getMappedMemoryUsedJoinValue());
        }
        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer
                , directCountAnalyzerBuilder.build()
                , directMemoryUsedAnalyzerBuilder.build()
                , mappedCountAnalyzerBuilder.build()
                , mappedMemoryUsedAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer
            , JoinLongFieldStrategyAnalyzer directCountAnalyzer
            , JoinLongFieldStrategyAnalyzer directMemoryUsedAnalyzer
            , JoinLongFieldStrategyAnalyzer mappedCountAnalyzer
            , JoinLongFieldStrategyAnalyzer mappedMemoryUsedAnalyzer) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();


        byte[] codes = directCountAnalyzer.getBestStrategy().getCodes();
        for (byte code : codes) {
            headerEncoder.addCode(code);
        }
        codes = directMemoryUsedAnalyzer.getBestStrategy().getCodes();
        for (byte code : codes) {
            headerEncoder.addCode(code);
        }
        codes = mappedCountAnalyzer.getBestStrategy().getCodes();
        for (byte code : codes) {
            headerEncoder.addCode(code);
        }
        codes = mappedMemoryUsedAnalyzer.getBestStrategy().getCodes();
        for (byte code : codes) {
            headerEncoder.addCode(code);
        }

        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);
        // encode values
        this.codec.encodeValues(valueBuffer, directCountAnalyzer.getBestStrategy(), directCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, directMemoryUsedAnalyzer.getBestStrategy(), directMemoryUsedAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, mappedCountAnalyzer.getBestStrategy(), mappedCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, mappedMemoryUsedAnalyzer.getBestStrategy(), mappedMemoryUsedAnalyzer.getValues());
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

        JoinLongFieldEncodingStrategy directCountEncodingStrategy = JoinLongFieldEncodingStrategy.getFromCode(headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode());
        JoinLongFieldEncodingStrategy directMemoryUsedEncodingStrategy = JoinLongFieldEncodingStrategy.getFromCode(headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode());
        JoinLongFieldEncodingStrategy mappedCountEncodingStrategy = JoinLongFieldEncodingStrategy.getFromCode(headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode());
        JoinLongFieldEncodingStrategy mappedMemoryUsedEncodingStrategy = JoinLongFieldEncodingStrategy.getFromCode(headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode(), headerDecoder.getCode());

        // decode values
        List<JoinLongFieldBo> directCountList = this.codec.decodeValues(valueBuffer, directCountEncodingStrategy, numValues);
        List<JoinLongFieldBo> directMemoryUsedList = this.codec.decodeValues(valueBuffer, directMemoryUsedEncodingStrategy, numValues);
        List<JoinLongFieldBo> mappedCountList = this.codec.decodeValues(valueBuffer, mappedCountEncodingStrategy, numValues);
        List<JoinLongFieldBo> mappedMemoryUsedList = this.codec.decodeValues(valueBuffer, mappedMemoryUsedEncodingStrategy, numValues);

        List<JoinStatBo> joinDirectBufferBoList = new ArrayList<JoinStatBo>(numValues);
        for (int i = 0; i < numValues; i++) {
            JoinDirectBufferBo joinDirectBufferBo = new JoinDirectBufferBo();
            joinDirectBufferBo.setId(id);
            joinDirectBufferBo.setTimestamp(timestamps.get(i));

            joinDirectBufferBo.setDirectCountJoinValue(directCountList.get(i));
            joinDirectBufferBo.setDirectMemoryUsedJoinValue(directMemoryUsedList.get(i));
            joinDirectBufferBo.setMappedCountJoinValue(mappedCountList.get(i));
            joinDirectBufferBo.setMappedMemoryUsedJoinValue(mappedMemoryUsedList.get(i));

            joinDirectBufferBoList.add(joinDirectBufferBo);
        }
        return joinDirectBufferBoList;
    }
}
