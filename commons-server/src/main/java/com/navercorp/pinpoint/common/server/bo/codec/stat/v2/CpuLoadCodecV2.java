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
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Component("cpuLoadCodecV2")
public class CpuLoadCodecV2 extends AgentStatCodecV2<CpuLoadBo> {

    @Autowired
    public CpuLoadCodecV2(AgentStatDataPointCodec codec) {
        super(new CpuLoadCodecFactory(codec));
    }


    private static class CpuLoadCodecFactory implements CodecFactory<CpuLoadBo> {

        private final AgentStatDataPointCodec codec;

        private CpuLoadCodecFactory(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public AgentStatDataPointCodec getCodec() {
            return codec;
        }

        @Override
        public CodecEncoder<CpuLoadBo> createCodecEncoder() {
            return new CpuLoadCodecEncoder(codec);
        }

        @Override
        public CodecDecoder<CpuLoadBo> createCodecDecoder() {
            return new CpuLoadCodecDecoder(codec);
        }
    }

    public static class CpuLoadCodecEncoder implements AgentStatCodec.CodecEncoder<CpuLoadBo> {

        private final AgentStatDataPointCodec codec;
        private final UnsignedLongEncodingStrategy.Analyzer.Builder jvmCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder systemCpuLoadAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();

        public CpuLoadCodecEncoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void addValue(CpuLoadBo cpuLoadBo) {
            jvmCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(cpuLoadBo.getJvmCpuLoad()));
            systemCpuLoadAnalyzerBuilder.addValue(AgentStatUtils.convertDoubleToLong(cpuLoadBo.getSystemCpuLoad()));
        }

        @Override
        public void encode(Buffer valueBuffer) {
            StrategyAnalyzer<Long> jvmCpuLoadStrategyAnalyzer = jvmCpuLoadAnalyzerBuilder.build();
            StrategyAnalyzer<Long> systemCpuLoadStrategyAnalyzer = systemCpuLoadAnalyzerBuilder.build();

            // encode header
            AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
            headerEncoder.addCode(jvmCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(systemCpuLoadStrategyAnalyzer.getBestStrategy().getCode());
            final byte[] header = headerEncoder.getHeader();
            valueBuffer.putPrefixedBytes(header);
            // encode values
            this.codec.encodeValues(valueBuffer, jvmCpuLoadStrategyAnalyzer.getBestStrategy(), jvmCpuLoadStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, systemCpuLoadStrategyAnalyzer.getBestStrategy(), systemCpuLoadStrategyAnalyzer.getValues());
        }

    }

    public static class CpuLoadCodecDecoder implements AgentStatCodec.CodecDecoder<CpuLoadBo> {

        private final AgentStatDataPointCodec codec;

        private List<Long> jvmCpuLoads;
        private List<Long> systemCpuLoads;

        public CpuLoadCodecDecoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            EncodingStrategy<Long> jvmCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> systemCpuLoadEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            // decode values
            this.jvmCpuLoads = this.codec.decodeValues(valueBuffer, jvmCpuLoadEncodingStrategy, valueSize);
            this.systemCpuLoads = this.codec.decodeValues(valueBuffer, systemCpuLoadEncodingStrategy, valueSize);
        }

        @Override
        public CpuLoadBo getValue(int index) {
            CpuLoadBo cpuLoadBo = new CpuLoadBo();
            cpuLoadBo.setJvmCpuLoad(AgentStatUtils.convertLongToDouble(jvmCpuLoads.get(index)));
            cpuLoadBo.setSystemCpuLoad(AgentStatUtils.convertLongToDouble(systemCpuLoads.get(index)));
            return cpuLoadBo;
        }

    }

}
