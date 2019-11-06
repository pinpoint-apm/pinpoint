/*
 * Copyright 2017 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Component("responseTimeCodecV2")
public class ResponseTimeCodecV2 extends AgentStatCodecV2<ResponseTimeBo> {

    @Autowired
    public ResponseTimeCodecV2(AgentStatDataPointCodec codec) {
        super(new ResponseTimeFactory(codec));
    }

    private static class ResponseTimeFactory implements CodecFactory<ResponseTimeBo> {

        private final AgentStatDataPointCodec codec;

        private ResponseTimeFactory(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public AgentStatDataPointCodec getCodec() {
            return codec;
        }

        @Override
        public CodecEncoder<ResponseTimeBo> createCodecEncoder() {
            return new ResponseTimeCodecEncoder(codec);
        }

        @Override
        public CodecDecoder<ResponseTimeBo> createCodecDecoder() {
            return new ResponseTimeCodecDecoder(codec);
        }
    }

    private static class ResponseTimeCodecEncoder implements AgentStatCodec.CodecEncoder<ResponseTimeBo> {

        private final AgentStatDataPointCodec codec;
        private final UnsignedLongEncodingStrategy.Analyzer.Builder avgAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder maxAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();

        public ResponseTimeCodecEncoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void addValue(ResponseTimeBo agentStatDataPoint) {
            avgAnalyzerBuilder.addValue(agentStatDataPoint.getAvg());
            maxAnalyzerBuilder.addValue(agentStatDataPoint.getMax());
        }

        @Override
        public void encode(Buffer valueBuffer) {
            StrategyAnalyzer<Long> avgStrategyAnalyzer = avgAnalyzerBuilder.build();
            StrategyAnalyzer<Long> maxStrategyAnalyzer = maxAnalyzerBuilder.build();

            // encode header
            AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
            headerEncoder.addCode(avgStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(maxStrategyAnalyzer.getBestStrategy().getCode());

            final byte[] header = headerEncoder.getHeader();
            valueBuffer.putPrefixedBytes(header);
            // encode values
            codec.encodeValues(valueBuffer, avgStrategyAnalyzer.getBestStrategy(), avgStrategyAnalyzer.getValues());
            codec.encodeValues(valueBuffer, maxStrategyAnalyzer.getBestStrategy(), maxStrategyAnalyzer.getValues());
        }

    }

    private static class ResponseTimeCodecDecoder implements AgentStatCodec.CodecDecoder<ResponseTimeBo> {

        private final AgentStatDataPointCodec codec;
        private List<Long> avgs;
        private List<Long> maxs;

        public ResponseTimeCodecDecoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            EncodingStrategy<Long> avgEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> maxEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());

            this.avgs = codec.decodeValues(valueBuffer, avgEncodingStrategy, valueSize);
            if (valueBuffer.hasRemaining()) {
                this.maxs = codec.decodeValues(valueBuffer, maxEncodingStrategy, valueSize);
            }
        }

        @Override
        public ResponseTimeBo getValue(int index) {
            ResponseTimeBo responseTimeBo = new ResponseTimeBo();
            responseTimeBo.setAvg(avgs.get(index));
            if (maxs != null) {
                responseTimeBo.setMax(maxs.get(index));
            }
            return responseTimeBo;
        }

    }

}
