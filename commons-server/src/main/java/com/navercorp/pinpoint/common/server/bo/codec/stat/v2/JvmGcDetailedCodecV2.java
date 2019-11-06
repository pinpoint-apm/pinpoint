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
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.CodecFactory;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Component("jvmGcDetailedCodecV2")
public class JvmGcDetailedCodecV2 extends AgentStatCodecV2<JvmGcDetailedBo> {

    @Autowired
    public JvmGcDetailedCodecV2(AgentStatDataPointCodec codec) {
        super(new JvmGcDetailedCodecFactory(codec));
    }


    private static class JvmGcDetailedCodecFactory implements CodecFactory<JvmGcDetailedBo> {

        private final AgentStatDataPointCodec codec;

        private JvmGcDetailedCodecFactory(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public AgentStatDataPointCodec getCodec() {
            return codec;
        }

        @Override
        public CodecEncoder<JvmGcDetailedBo> createCodecEncoder() {
            return new JvmGcDetailedCodecEncoder(codec);
        }

        @Override
        public CodecDecoder<JvmGcDetailedBo> createCodecDecoder() {
            return new JvmGcDetailedCodecDecoder(codec);
        }
    }

    public static class JvmGcDetailedCodecEncoder implements AgentStatCodec.CodecEncoder<JvmGcDetailedBo> {

        private final AgentStatDataPointCodec codec;
        private final UnsignedLongEncodingStrategy.Analyzer.Builder gcNewCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder gcNewTimeAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder codeCacheUsedStrategyAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder newGenUsedStrategyAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder oldGenUsedStrategyAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder survivorSpaceUsedStrategyAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder permGenUsedStrategyAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder metaspaceUsedStrategyAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();

        public JvmGcDetailedCodecEncoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void addValue(JvmGcDetailedBo jvmGcDetailedBo) {
            gcNewCountAnalyzerBuilder.addValue(jvmGcDetailedBo.getGcNewCount());
            gcNewTimeAnalyzerBuilder.addValue(jvmGcDetailedBo.getGcNewTime());
            codeCacheUsedStrategyAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(jvmGcDetailedBo.getCodeCacheUsed()));
            newGenUsedStrategyAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(jvmGcDetailedBo.getNewGenUsed()));
            oldGenUsedStrategyAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(jvmGcDetailedBo.getOldGenUsed()));
            survivorSpaceUsedStrategyAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(jvmGcDetailedBo.getSurvivorSpaceUsed()));
            permGenUsedStrategyAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(jvmGcDetailedBo.getPermGenUsed()));
            metaspaceUsedStrategyAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(jvmGcDetailedBo.getMetaspaceUsed()));
        }

        @Override
        public void encode(Buffer valueBuffer) {
            StrategyAnalyzer<Long> gcNewCountStrategyAnalyzer = gcNewCountAnalyzerBuilder.build();
            StrategyAnalyzer<Long> gcNewTimeStrategyAnalyzer = gcNewTimeAnalyzerBuilder.build();
            StrategyAnalyzer<Long> codeCacheUsedStrategyAnalyzer = codeCacheUsedStrategyAnalyzerBuilder.build();
            StrategyAnalyzer<Long> newGenUsedStrategyAnalyzer = newGenUsedStrategyAnalyzerBuilder.build();
            StrategyAnalyzer<Long> oldGenUsedStrategyAnalyzer = oldGenUsedStrategyAnalyzerBuilder.build();
            StrategyAnalyzer<Long> survivorSpaceUsedStrategyAnalyzer = survivorSpaceUsedStrategyAnalyzerBuilder.build();
            StrategyAnalyzer<Long> permGenUsedStrategyAnalyzer = permGenUsedStrategyAnalyzerBuilder.build();
            StrategyAnalyzer<Long> metaspaceUsedStrategyAnalyzer = metaspaceUsedStrategyAnalyzerBuilder.build();
            // encode header
            AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
            headerEncoder.addCode(gcNewCountStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(gcNewTimeStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(codeCacheUsedStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(newGenUsedStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(oldGenUsedStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(survivorSpaceUsedStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(permGenUsedStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(metaspaceUsedStrategyAnalyzer.getBestStrategy().getCode());
            final byte[] header = headerEncoder.getHeader();
            valueBuffer.putPrefixedBytes(header);
            // encode values
            this.codec.encodeValues(valueBuffer, gcNewCountStrategyAnalyzer.getBestStrategy(), gcNewCountStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, gcNewTimeStrategyAnalyzer.getBestStrategy(), gcNewTimeStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, codeCacheUsedStrategyAnalyzer.getBestStrategy(), codeCacheUsedStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, newGenUsedStrategyAnalyzer.getBestStrategy(), newGenUsedStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, oldGenUsedStrategyAnalyzer.getBestStrategy(), oldGenUsedStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, survivorSpaceUsedStrategyAnalyzer.getBestStrategy(), survivorSpaceUsedStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, permGenUsedStrategyAnalyzer.getBestStrategy(), permGenUsedStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, metaspaceUsedStrategyAnalyzer.getBestStrategy(), metaspaceUsedStrategyAnalyzer.getValues());
        }

    }

    public static class JvmGcDetailedCodecDecoder implements AgentStatCodec.CodecDecoder<JvmGcDetailedBo> {

        private final AgentStatDataPointCodec codec;
        private List<Long> gcNewCounts;
        private List<Long> gcNewTimes;
        private List<Long> codeCacheUseds;
        private List<Long> newGenUseds;
        private List<Long> oldGenUseds;
        private List<Long> survivorSpaceUseds;
        private List<Long> permGenUseds;
        private List<Long> metaspaceUseds;

        public JvmGcDetailedCodecDecoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            EncodingStrategy<Long> gcNewCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> gcNewTimeEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> codeCacheUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> newGenUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> oldGenUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> survivorSpaceUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> permGenUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> metaspaceUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            // decode values
            this.gcNewCounts = this.codec.decodeValues(valueBuffer, gcNewCountEncodingStrategy, valueSize);
            this.gcNewTimes = this.codec.decodeValues(valueBuffer, gcNewTimeEncodingStrategy, valueSize);
            this.codeCacheUseds = this.codec.decodeValues(valueBuffer, codeCacheUsedEncodingStrategy, valueSize);
            this.newGenUseds = this.codec.decodeValues(valueBuffer, newGenUsedEncodingStrategy, valueSize);
            this.oldGenUseds = this.codec.decodeValues(valueBuffer, oldGenUsedEncodingStrategy, valueSize);
            this.survivorSpaceUseds = this.codec.decodeValues(valueBuffer, survivorSpaceUsedEncodingStrategy, valueSize);
            this.permGenUseds = this.codec.decodeValues(valueBuffer, permGenUsedEncodingStrategy, valueSize);
            this.metaspaceUseds = this.codec.decodeValues(valueBuffer, metaspaceUsedEncodingStrategy, valueSize);
        }

        @Override
        public JvmGcDetailedBo getValue(int index) {
            JvmGcDetailedBo jvmGcDetailedBo = new JvmGcDetailedBo();
            jvmGcDetailedBo.setGcNewCount(gcNewCounts.get(index));
            jvmGcDetailedBo.setGcNewTime(gcNewTimes.get(index));
            jvmGcDetailedBo.setCodeCacheUsed(AgentStatUtils.convertLongToDouble(codeCacheUseds.get(index)));
            jvmGcDetailedBo.setNewGenUsed(AgentStatUtils.convertLongToDouble(newGenUseds.get(index)));
            jvmGcDetailedBo.setOldGenUsed(AgentStatUtils.convertLongToDouble(oldGenUseds.get(index)));
            jvmGcDetailedBo.setSurvivorSpaceUsed(AgentStatUtils.convertLongToDouble(survivorSpaceUseds.get(index)));
            jvmGcDetailedBo.setPermGenUsed(AgentStatUtils.convertLongToDouble(permGenUseds.get(index)));
            jvmGcDetailedBo.setMetaspaceUsed(AgentStatUtils.convertLongToDouble(metaspaceUseds.get(index)));
            return jvmGcDetailedBo;
        }

    }

}
