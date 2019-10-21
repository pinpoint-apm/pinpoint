/*
 * Copyright 2018 Naver Corp.
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
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author Roy Kim
 */
@Component("directBufferCodecV2")
public class DirectBufferCodecV2 extends AgentStatCodecV2<DirectBufferBo> {

    @Autowired
    public DirectBufferCodecV2(AgentStatDataPointCodec codec) {
        super(new DirectBufferCodecFactory(codec));
    }


    private static class DirectBufferCodecFactory implements CodecFactory<DirectBufferBo> {

        private final AgentStatDataPointCodec codec;

        private DirectBufferCodecFactory(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public AgentStatDataPointCodec getCodec() {
            return codec;
        }

        @Override
        public CodecEncoder<DirectBufferBo> createCodecEncoder() {
            return new DirectBufferCodecEncoder(codec);
        }

        @Override
        public CodecDecoder<DirectBufferBo> createCodecDecoder() {
            return new DirectBufferCodecDecoder(codec);
        }
    }

    public static class DirectBufferCodecEncoder implements CodecEncoder<DirectBufferBo> {

        private final AgentStatDataPointCodec codec;
        private final UnsignedLongEncodingStrategy.Analyzer.Builder directCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder directMemoryUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder mappedCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder mappedMemoryUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();


        public DirectBufferCodecEncoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void addValue(DirectBufferBo directBufferBo) {
            directCountAnalyzerBuilder.addValue(directBufferBo.getDirectCount());
            directMemoryUsedAnalyzerBuilder.addValue(directBufferBo.getDirectMemoryUsed());
            mappedCountAnalyzerBuilder.addValue(directBufferBo.getMappedCount());
            mappedMemoryUsedAnalyzerBuilder.addValue(directBufferBo.getMappedMemoryUsed());
        }

        @Override
        public void encode(Buffer valueBuffer) {
            StrategyAnalyzer<Long> directCountStrategyAnalyzer = directCountAnalyzerBuilder.build();
            StrategyAnalyzer<Long> directMemoryUsedStrategyAnalyzer = directMemoryUsedAnalyzerBuilder.build();
            StrategyAnalyzer<Long> mappedCountStrategyAnalyzer = mappedCountAnalyzerBuilder.build();
            StrategyAnalyzer<Long> mappedMemoryUsedStrategyAnalyzer = mappedMemoryUsedAnalyzerBuilder.build();


            // encode header
            AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
            headerEncoder.addCode(directCountStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(directMemoryUsedStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(mappedCountStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(mappedMemoryUsedStrategyAnalyzer.getBestStrategy().getCode());

            final byte[] header = headerEncoder.getHeader();
            valueBuffer.putPrefixedBytes(header);
            // encode values
            this.codec.encodeValues(valueBuffer, directCountStrategyAnalyzer.getBestStrategy(), directCountStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, directMemoryUsedStrategyAnalyzer.getBestStrategy(), directMemoryUsedStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, mappedCountStrategyAnalyzer.getBestStrategy(), mappedCountStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, mappedMemoryUsedStrategyAnalyzer.getBestStrategy(), mappedMemoryUsedStrategyAnalyzer.getValues());
        }

    }

    public static class DirectBufferCodecDecoder implements CodecDecoder<DirectBufferBo> {

        private final AgentStatDataPointCodec codec;

        private List<Long> directCount;
        private List<Long> directMemoryUsed;
        private List<Long> mappedCount;
        private List<Long> mappedMemoryUsed;

        public DirectBufferCodecDecoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            EncodingStrategy<Long> directCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> directMemoryUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> mappedCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> mappedMemoryUsedCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            // decode values
            this.directCount = this.codec.decodeValues(valueBuffer, directCountEncodingStrategy, valueSize);
            this.directMemoryUsed = this.codec.decodeValues(valueBuffer, directMemoryUsedEncodingStrategy, valueSize);
            this.mappedCount = this.codec.decodeValues(valueBuffer, mappedCountEncodingStrategy, valueSize);
            this.mappedMemoryUsed = this.codec.decodeValues(valueBuffer, mappedMemoryUsedCountEncodingStrategy, valueSize);
        }

        @Override
        public DirectBufferBo getValue(int index) {
            DirectBufferBo directBufferBo = new DirectBufferBo();
            directBufferBo.setDirectCount(directCount.get(index));
            directBufferBo.setDirectMemoryUsed(directMemoryUsed.get(index));
            directBufferBo.setMappedCount(mappedCount.get(index));
            directBufferBo.setMappedMemoryUsed(mappedMemoryUsed.get(index));
            return directBufferBo;
        }

    }

}
