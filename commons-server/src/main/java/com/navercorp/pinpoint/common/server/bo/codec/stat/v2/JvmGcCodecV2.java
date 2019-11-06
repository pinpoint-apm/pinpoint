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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v2;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
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
@Component("jvmGcCodecV2")
public class JvmGcCodecV2 implements AgentStatCodec<JvmGcBo> {

    private static final byte VERSION = 2;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public JvmGcCodecV2(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "agentStatDataPointCodec");
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

        List<Long> startTimestamps = new ArrayList<>(numValues);
        List<Long> timestamps = new ArrayList<>(numValues);
        JvmGcCodecEncoder jvmGcCodecEncoder = new JvmGcCodecEncoder(codec);
        for (JvmGcBo jvmGcBo : jvmGcBos) {
            startTimestamps.add(jvmGcBo.getStartTimestamp());
            timestamps.add(jvmGcBo.getTimestamp());
            jvmGcCodecEncoder.addValue(jvmGcBo);
        }

        this.codec.encodeValues(valueBuffer, UnsignedLongEncodingStrategy.REPEAT_COUNT, startTimestamps);
        this.codec.encodeTimestamps(valueBuffer, timestamps);
        jvmGcCodecEncoder.encode(valueBuffer);
    }

    @Override
    public List<JvmGcBo> decodeValues(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
        final String agentId = decodingContext.getAgentId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        final JvmGcType gcType = JvmGcType.getTypeByCode(valueBuffer.readVInt());
        int numValues = valueBuffer.readVInt();
        List<Long> startTimestamps = this.codec.decodeValues(valueBuffer, UnsignedLongEncodingStrategy.REPEAT_COUNT, numValues);
        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);

        JvmGcCodecDecoder decoder = new JvmGcCodecDecoder(codec);
        decoder.decode(valueBuffer, headerDecoder, numValues);

        List<JvmGcBo> jvmGcBos = new ArrayList<>(numValues);
        for (int i = 0; i < numValues; i++) {
            JvmGcBo jvmGcBo = decoder.getValue(i);
            jvmGcBo.setAgentId(agentId);
            jvmGcBo.setStartTimestamp(startTimestamps.get(i));
            jvmGcBo.setTimestamp(timestamps.get(i));
            jvmGcBo.setGcType(gcType);
            jvmGcBos.add(jvmGcBo);
        }
        return jvmGcBos;
    }

    public static class JvmGcCodecEncoder implements AgentStatCodec.CodecEncoder<JvmGcBo> {

        private final AgentStatDataPointCodec codec;
        UnsignedLongEncodingStrategy.Analyzer.Builder heapUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder heapMaxAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder nonHeapUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder nonHeapMaxAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder gcOldCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder gcOldTimeAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();

        public JvmGcCodecEncoder(AgentStatDataPointCodec codec) {
            this.codec = codec;
        }

        @Override
        public void addValue(JvmGcBo jvmGcBo) {
            heapUsedAnalyzerBuilder.addValue(jvmGcBo.getHeapUsed());
            heapMaxAnalyzerBuilder.addValue(jvmGcBo.getHeapMax());
            nonHeapUsedAnalyzerBuilder.addValue(jvmGcBo.getNonHeapUsed());
            nonHeapMaxAnalyzerBuilder.addValue(jvmGcBo.getNonHeapMax());
            gcOldCountAnalyzerBuilder.addValue(jvmGcBo.getGcOldCount());
            gcOldTimeAnalyzerBuilder.addValue(jvmGcBo.getGcOldTime());
        }

        @Override
        public void encode(Buffer valueBuffer) {
            StrategyAnalyzer<Long> heapUsedStrategyAnalyzer = heapUsedAnalyzerBuilder.build();
            StrategyAnalyzer<Long> heapMaxStrategyAnalyzer = heapMaxAnalyzerBuilder.build();
            StrategyAnalyzer<Long> nonHeapUsedStrategyAnalyzer = nonHeapUsedAnalyzerBuilder.build();
            StrategyAnalyzer<Long> nonHeapMaxStrategyAnalyzer = nonHeapMaxAnalyzerBuilder.build();
            StrategyAnalyzer<Long> gcOldCountStrategyAnalyzer = gcOldCountAnalyzerBuilder.build();
            StrategyAnalyzer<Long> gcOldTimeStrategyAnalyzer = gcOldTimeAnalyzerBuilder.build();
            // encode header
            AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
            headerEncoder.addCode(heapUsedStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(heapMaxStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(nonHeapUsedStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(nonHeapMaxStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(gcOldCountStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(gcOldTimeStrategyAnalyzer.getBestStrategy().getCode());
            final byte[] header = headerEncoder.getHeader();
            valueBuffer.putPrefixedBytes(header);
            // encode values
            this.codec.encodeValues(valueBuffer, heapUsedStrategyAnalyzer.getBestStrategy(), heapUsedStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, heapMaxStrategyAnalyzer.getBestStrategy(), heapMaxStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, nonHeapUsedStrategyAnalyzer.getBestStrategy(), nonHeapUsedStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, nonHeapMaxStrategyAnalyzer.getBestStrategy(), nonHeapMaxStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, gcOldCountStrategyAnalyzer.getBestStrategy(), gcOldCountStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, gcOldTimeStrategyAnalyzer.getBestStrategy(), gcOldTimeStrategyAnalyzer.getValues());
        }

    }

    public static class JvmGcCodecDecoder implements AgentStatCodec.CodecDecoder<JvmGcBo> {

        private final AgentStatDataPointCodec codec;
        private List<Long> heapUseds;
        private List<Long> heapMaxes;
        private List<Long> nonHeapUseds;
        private List<Long> nonHeapMaxes;
        private List<Long> gcOldCounts;
        private List<Long> gcOldTimes;

        public JvmGcCodecDecoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            EncodingStrategy<Long> heapUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> heapMaxEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> nonHeapUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> nonHeapMaxEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> gcOldCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> gcOldTimeEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            // decode values
            this.heapUseds = this.codec.decodeValues(valueBuffer, heapUsedEncodingStrategy, valueSize);
            this.heapMaxes = this.codec.decodeValues(valueBuffer, heapMaxEncodingStrategy, valueSize);
            this.nonHeapUseds = this.codec.decodeValues(valueBuffer, nonHeapUsedEncodingStrategy, valueSize);
            this.nonHeapMaxes = this.codec.decodeValues(valueBuffer, nonHeapMaxEncodingStrategy, valueSize);
            this.gcOldCounts = this.codec.decodeValues(valueBuffer, gcOldCountEncodingStrategy,  valueSize);
            this.gcOldTimes = this.codec.decodeValues(valueBuffer, gcOldTimeEncodingStrategy, valueSize);
        }

        @Override
        public JvmGcBo getValue(int index) {
            JvmGcBo jvmGcBo = new JvmGcBo();
            jvmGcBo.setHeapUsed(heapUseds.get(index));
            jvmGcBo.setHeapMax(heapMaxes.get(index));
            jvmGcBo.setNonHeapUsed(nonHeapUseds.get(index));
            jvmGcBo.setNonHeapMax(nonHeapMaxes.get(index));
            jvmGcBo.setGcOldCount(gcOldCounts.get(index));
            jvmGcBo.setGcOldTime(gcOldTimes.get(index));
            return jvmGcBo;
        }

    }


}
