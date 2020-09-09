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
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.stat.ContainerBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Component("containerCodecV2")
public class ContainerCodecV2 extends AgentStatCodecV2<ContainerBo> {

    @Autowired
    public ContainerCodecV2(AgentStatDataPointCodec codec) {
        super(new ContainerCodecFactory(codec));
    }

    private static class ContainerCodecFactory implements CodecFactory<ContainerBo> {

        private final AgentStatDataPointCodec codec;

        private ContainerCodecFactory(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public AgentStatDataPointCodec getCodec() {
            return codec;
        }

        @Override
        public CodecEncoder<ContainerBo> createCodecEncoder() {
            return new ContainerCodecEncoder(codec);
        }

        @Override
        public CodecDecoder<ContainerBo> createCodecDecoder() {
            return new ContainerCodecDecoder(codec);
        }
    }

    public static class ContainerCodecEncoder implements CodecEncoder<ContainerBo> {

        private final AgentStatDataPointCodec codec;
        private final UnsignedLongEncodingStrategy.Analyzer.Builder userCpuUsageAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder systemCpuUsageAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder memoryMaxAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder memoryUsageAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();


        public ContainerCodecEncoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void addValue(ContainerBo containerBo) {
            userCpuUsageAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(containerBo.getUserCpuUsage()));
            systemCpuUsageAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(containerBo.getSystemCpuUsage()));
            memoryMaxAnalyzerBuilder.addValue(containerBo.getMemoryMax());
            memoryUsageAnalyzerBuilder.addValue(containerBo.getMemoryUsage());
        }

        @Override
        public void encode(Buffer valueBuffer) {
            StrategyAnalyzer<Long> userCpuUsageStrategyAnalyzer = userCpuUsageAnalyzerBuilder.build();
            StrategyAnalyzer<Long> systemCpuUsageStrategyAnalyzer = systemCpuUsageAnalyzerBuilder.build();
            StrategyAnalyzer<Long> memoryMaxStrategyAnalyzer = memoryMaxAnalyzerBuilder.build();
            StrategyAnalyzer<Long> memoryUsageStrategyAnalyzer = memoryUsageAnalyzerBuilder.build();


            // encode header
            AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
            headerEncoder.addCode(userCpuUsageStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(systemCpuUsageStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(memoryMaxStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(memoryUsageStrategyAnalyzer.getBestStrategy().getCode());

            final byte[] header = headerEncoder.getHeader();
            valueBuffer.putPrefixedBytes(header);
            // encode values
            this.codec.encodeValues(valueBuffer, userCpuUsageStrategyAnalyzer.getBestStrategy(), userCpuUsageStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, systemCpuUsageStrategyAnalyzer.getBestStrategy(), systemCpuUsageStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, memoryMaxStrategyAnalyzer.getBestStrategy(), memoryMaxStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, memoryUsageStrategyAnalyzer.getBestStrategy(), memoryUsageStrategyAnalyzer.getValues());
        }

    }

    public static class ContainerCodecDecoder implements CodecDecoder<ContainerBo> {

        private final AgentStatDataPointCodec codec;

        private List<Long> userCpuUsage;
        private List<Long> systemCpuUsage;
        private List<Long> memoryMax;
        private List<Long> memoryUsage;

        public ContainerCodecDecoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            EncodingStrategy<Long> userCpuUsageEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> systemCpuUsageEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> memoryMaxEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> memoryUsageEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            // decode values
            this.userCpuUsage = this.codec.decodeValues(valueBuffer, userCpuUsageEncodingStrategy, valueSize);
            this.systemCpuUsage = this.codec.decodeValues(valueBuffer, systemCpuUsageEncodingStrategy, valueSize);
            this.memoryMax = this.codec.decodeValues(valueBuffer, memoryMaxEncodingStrategy, valueSize);
            this.memoryUsage = this.codec.decodeValues(valueBuffer, memoryUsageEncodingStrategy, valueSize);
        }

        @Override
        public ContainerBo getValue(int index) {
            ContainerBo containerBo = new ContainerBo();
            containerBo.setUserCpuUsage(AgentStatUtils.convertLongToDouble(userCpuUsage.get(index)));
            containerBo.setSystemCpuUsage(AgentStatUtils.convertLongToDouble(systemCpuUsage.get(index)));
            containerBo.setMemoryMax(memoryMax.get(index));
            containerBo.setMemoryUsage(memoryUsage.get(index));
            return containerBo;
        }

    }
}
