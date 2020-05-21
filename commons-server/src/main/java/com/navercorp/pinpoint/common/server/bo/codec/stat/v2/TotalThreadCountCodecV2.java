/*
 * Copyright 2020 Naver Corp.
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
import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component("totalThreadCountCodecV2")
public class TotalThreadCountCodecV2 extends AgentStatCodecV2<TotalThreadCountBo> {

    @Autowired
    public TotalThreadCountCodecV2(AgentStatDataPointCodec codec) {
        super(new TotalThreadCountCodecV2.TotalThreadCountCodecFactory(codec));
    }

    private static class TotalThreadCountCodecFactory implements CodecFactory<TotalThreadCountBo> {

        private final AgentStatDataPointCodec codec;

        private TotalThreadCountCodecFactory(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec);
        }

        @Override
        public AgentStatDataPointCodec getCodec() { return codec; }

        @Override
        public CodecEncoder<TotalThreadCountBo> createCodecEncoder() {
            return new TotalThreadCountEncoder(codec);
        }

        @Override
        public CodecDecoder<TotalThreadCountBo> createCodecDecoder() {
            return new TotalThreadCountDecoder(codec);
        }
    }

    private static class TotalThreadCountEncoder implements AgentStatCodec.CodecEncoder<TotalThreadCountBo> {
        private final AgentStatDataPointCodec codec;
        private final UnsignedLongEncodingStrategy.Analyzer.Builder totalThreadCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();

        public TotalThreadCountEncoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void addValue(TotalThreadCountBo agentStatDataPoint) {
            totalThreadCountAnalyzerBuilder.addValue(agentStatDataPoint.getTotalThreadCount());
        }

        @Override
        public void encode(Buffer valueBuffer) {
            StrategyAnalyzer<Long> totalThreadCountStrategyAnalyzer = totalThreadCountAnalyzerBuilder.build();

            // encode header
            AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
                headerEncoder.addCode(totalThreadCountStrategyAnalyzer.getBestStrategy().getCode());
            final byte[] header = headerEncoder.getHeader();
                valueBuffer.putPrefixedBytes(header);
            // encode values
                this.codec.encodeValues(valueBuffer, totalThreadCountStrategyAnalyzer.getBestStrategy(), totalThreadCountStrategyAnalyzer.getValues());

        }
    }

    private static class TotalThreadCountDecoder implements AgentStatCodec.CodecDecoder<TotalThreadCountBo> {
        private final AgentStatDataPointCodec codec;
        private List<Long> totalThreadCounts;

        public TotalThreadCountDecoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            EncodingStrategy<Long> totalThreadCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            // decode values
            this.totalThreadCounts = this.codec.decodeValues(valueBuffer, totalThreadCountEncodingStrategy, valueSize);
        }

        @Override
        public TotalThreadCountBo getValue(int index) {
            TotalThreadCountBo totalThreadCountBo = new TotalThreadCountBo();
            totalThreadCountBo.setTotalThreadCount(totalThreadCounts.get(index));
            return totalThreadCountBo;
        }
    }

}
