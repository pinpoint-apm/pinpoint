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
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedIntegerEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Component("deadlockCodecV2")
public class DeadlockCodecV2 extends AgentStatCodecV2<DeadlockThreadCountBo> {

    @Autowired
    public DeadlockCodecV2(AgentStatDataPointCodec codec) {
        super(new DeadlockCodecFactory(codec));
    }


    private static class DeadlockCodecFactory implements CodecFactory<DeadlockThreadCountBo> {

        private final AgentStatDataPointCodec codec;

        private DeadlockCodecFactory(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public AgentStatDataPointCodec getCodec() {
            return codec;
        }

        @Override
        public CodecEncoder<DeadlockThreadCountBo> createCodecEncoder() {
            return new DeadlockCodecEncoder(codec);
        }

        @Override
        public CodecDecoder<DeadlockThreadCountBo> createCodecDecoder() {
            return new DeadlockCodecDecoder(codec);
        }
    }

    private static class DeadlockCodecEncoder implements AgentStatCodec.CodecEncoder<DeadlockThreadCountBo> {

        private final AgentStatDataPointCodec codec;
        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder deadlockedThreadCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();

        private DeadlockCodecEncoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void addValue(DeadlockThreadCountBo deadlockThreadCountBo) {
            deadlockedThreadCountAnalyzerBuilder.addValue(deadlockThreadCountBo.getDeadlockedThreadCount());
        }

        @Override
        public void encode(Buffer valueBuffer) {
            StrategyAnalyzer<Integer> deadlockedThreadIdAnalyzer = deadlockedThreadCountAnalyzerBuilder.build();

            // encode header
            AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
            headerEncoder.addCode(deadlockedThreadIdAnalyzer.getBestStrategy().getCode());

            final byte[] header = headerEncoder.getHeader();
            valueBuffer.putPrefixedBytes(header);

            // encode values
            this.codec.encodeValues(valueBuffer, deadlockedThreadIdAnalyzer.getBestStrategy(), deadlockedThreadIdAnalyzer.getValues());
        }

    }

    private static class DeadlockCodecDecoder implements AgentStatCodec.CodecDecoder<DeadlockThreadCountBo> {

        private final AgentStatDataPointCodec codec;

        private List<Integer> deadlockedThreadCountList;

        public DeadlockCodecDecoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            EncodingStrategy<Integer> deadlockedThreadCountEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());

            // decode values
            this.deadlockedThreadCountList = codec.decodeValues(valueBuffer, deadlockedThreadCountEncodingStrategy, valueSize);
        }

        @Override
        public DeadlockThreadCountBo getValue(int index) {
            DeadlockThreadCountBo deadlockThreadCountBo = new DeadlockThreadCountBo();
            deadlockThreadCountBo.setDeadlockedThreadCount(deadlockedThreadCountList.get(index));
            return deadlockThreadCountBo;
        }

    }

}
