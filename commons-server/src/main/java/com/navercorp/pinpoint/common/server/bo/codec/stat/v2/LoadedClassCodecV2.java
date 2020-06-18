/*
 * Copyright 2020 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.CodecFactory;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component("loadedClassCodecV2")
public class LoadedClassCodecV2 extends AgentStatCodecV2<LoadedClassBo> {
    @Autowired
    public LoadedClassCodecV2(AgentStatDataPointCodec codec) {
        super(new LoadedClassCodecV2.LoadedClassCodecFactory(codec));
    }

    private static class LoadedClassCodecFactory implements CodecFactory<LoadedClassBo> {
        private final AgentStatDataPointCodec codec;

        public LoadedClassCodecFactory(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public AgentStatDataPointCodec getCodec() { return codec; }

        @Override
        public CodecEncoder<LoadedClassBo> createCodecEncoder() {
            return new LoadedClassEncoder(codec);
        }

        @Override
        public CodecDecoder<LoadedClassBo> createCodecDecoder() {
            return new LoadedClassDecoder(codec);
        }
    }

    private static class LoadedClassEncoder implements CodecEncoder<LoadedClassBo> {
        private final AgentStatDataPointCodec codec;
        private final UnsignedLongEncodingStrategy.Analyzer.Builder loadedClassAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder unloadedClassAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();

        public LoadedClassEncoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void addValue(LoadedClassBo agentStatDataPoint) {
            loadedClassAnalyzerBuilder.addValue(agentStatDataPoint.getLoadedClassCount());
            unloadedClassAnalyzerBuilder.addValue(agentStatDataPoint.getUnloadedClassCount());
        }

        @Override
        public void encode(Buffer valueBuffer) {
            StrategyAnalyzer<Long> loadedClassStrategyAnalyzer = loadedClassAnalyzerBuilder.build();
            StrategyAnalyzer<Long> unloadedClassStrategyAnalyzer = unloadedClassAnalyzerBuilder.build();

            // encode header
            AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
            headerEncoder.addCode(loadedClassStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(unloadedClassStrategyAnalyzer.getBestStrategy().getCode());

            final byte[] header = headerEncoder.getHeader();
            valueBuffer.putPrefixedBytes(header);
            // encode values
            this.codec.encodeValues(valueBuffer, loadedClassStrategyAnalyzer.getBestStrategy(), loadedClassStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, unloadedClassStrategyAnalyzer.getBestStrategy(), unloadedClassStrategyAnalyzer.getValues());

        }
    }

    private static class LoadedClassDecoder implements CodecDecoder<LoadedClassBo> {
        private final AgentStatDataPointCodec codec;

        private List<Long> loadedClassCount;
        private List<Long> unloadedClassCount;
        public LoadedClassDecoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            EncodingStrategy<Long> loadedClassEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> unloadedClassEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            // decode values
            this.loadedClassCount = this.codec.decodeValues(valueBuffer, loadedClassEncodingStrategy, valueSize);
            this.unloadedClassCount = this.codec.decodeValues(valueBuffer, unloadedClassEncodingStrategy, valueSize);
        }

        @Override
        public LoadedClassBo getValue(int index) {
            LoadedClassBo loadedClassBo = new LoadedClassBo();
            loadedClassBo.setLoadedClassCount(loadedClassCount.get(index));
            loadedClassBo.setUnloadedClassCount(unloadedClassCount.get(index));
            return loadedClassBo;
        }
    }
}
