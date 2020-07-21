/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.codec.metric;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntCounter;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongCounter;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.CodecFactory;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v2.AgentStatCodecV2;
import com.navercorp.pinpoint.common.server.bo.metric.CustomMetricType;
import com.navercorp.pinpoint.common.server.bo.metric.EachCustomMetricBo;
import com.navercorp.pinpoint.common.server.bo.metric.FieldDescriptor;
import com.navercorp.pinpoint.common.server.bo.metric.FieldDescriptors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class CustomMetricCodec extends AgentStatCodecV2<EachCustomMetricBo> {

    public CustomMetricCodec(AgentStatDataPointCodec codec, CustomMetricType customMetricType) {
        super(new CustomMetricCodecFactory(codec, customMetricType));
    }

    private static class CustomMetricCodecFactory implements CodecFactory<EachCustomMetricBo> {

        private final AgentStatDataPointCodec codec;
        private final CustomMetricType customMetricType;

        private CustomMetricCodecFactory(AgentStatDataPointCodec codec, CustomMetricType customMetricType) {
            this.codec = Objects.requireNonNull(codec, "codec");
            this.customMetricType = Objects.requireNonNull(customMetricType, "customMetricType");
        }

        @Override
        public AgentStatDataPointCodec getCodec() {
            return codec;
        }

        @Override
        public CodecEncoder<EachCustomMetricBo> createCodecEncoder() {
            return new Encoder(codec, customMetricType);
        }

        @Override
        public CodecDecoder<EachCustomMetricBo> createCodecDecoder() {
            return new Decoder(codec, customMetricType);
        }
    }

    public static class Encoder implements AgentStatCodec.CodecEncoder<EachCustomMetricBo> {

        private final AgentStatDataPointCodec codec;

        private final List<CustomMetricStrategyAnalyzerBuilder> customMetricStrategyAnalyzerBuilderList = new ArrayList<>();

        public Encoder(AgentStatDataPointCodec codec, CustomMetricType customMetricType) {
            this.codec = Objects.requireNonNull(codec, "codec");
            Objects.requireNonNull(customMetricType, "customMetricType");

            final FieldDescriptors fieldDescriptors = customMetricType.getFieldDescriptors();
            for (FieldDescriptor fieldDescriptor : fieldDescriptors.getAll()) {
                if (fieldDescriptor.getType() == IntCounter.class) {
                    IntCounterStrategyAnalyzerBuilder intCounterStrategyAnalyzerBuilder = new IntCounterStrategyAnalyzerBuilder(fieldDescriptor.getName());
                    customMetricStrategyAnalyzerBuilderList.add(intCounterStrategyAnalyzerBuilder);
                } else if (fieldDescriptor.getType() == LongCounter.class) {
                    LongCounterStrategyAnalyzerBuilder longCounterStrategyAnalyzerBuilder = new LongCounterStrategyAnalyzerBuilder(fieldDescriptor.getName());
                    customMetricStrategyAnalyzerBuilderList.add(longCounterStrategyAnalyzerBuilder);
                }
            }
        }

        @Override
        public void addValue(EachCustomMetricBo eachCustomMetricBo) {
            for (CustomMetricStrategyAnalyzerBuilder customMetricStrategyAnalyzerBuilder : customMetricStrategyAnalyzerBuilderList) {
                customMetricStrategyAnalyzerBuilder.addValue(eachCustomMetricBo);
            }
        }

        @Override
        public void encode(Buffer valueBuffer) {
            // encode header
            AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();

            List<StrategyAnalyzer> strategyAnalyzerList = new ArrayList<>(customMetricStrategyAnalyzerBuilderList.size());
            for (CustomMetricStrategyAnalyzerBuilder customMetricStrategyAnalyzerBuilder : customMetricStrategyAnalyzerBuilderList) {
                strategyAnalyzerList.add(customMetricStrategyAnalyzerBuilder.build());
            }

            for (StrategyAnalyzer strategyAnalyzer : strategyAnalyzerList) {
                headerEncoder.addCode(strategyAnalyzer.getBestStrategy().getCode());
            }

            final byte[] header = headerEncoder.getHeader();
            valueBuffer.putPrefixedBytes(header);

            for (StrategyAnalyzer strategyAnalyzer : strategyAnalyzerList) {
                codec.encodeValues(valueBuffer, strategyAnalyzer.getBestStrategy(), strategyAnalyzer.getValues());
            }
        }
    }

    public static class Decoder implements AgentStatCodec.CodecDecoder<EachCustomMetricBo> {

        private final AgentStatDataPointCodec codec;
        private final CustomMetricType customMetricType;

        public Decoder(AgentStatDataPointCodec codec, CustomMetricType customMetricType) {
            this.codec = Objects.requireNonNull(codec, "codec");
            this.customMetricType = Objects.requireNonNull(customMetricType, "customMetricType");
            // TO DO
        }

        @Override
        public void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            // TO DO
        }

        @Override
        public EachCustomMetricBo getValue(int index) {
            // TO DO
            EachCustomMetricBo agentCustomMetricBo = new EachCustomMetricBo(customMetricType.getAgentStatType());
            return agentCustomMetricBo;
        }

    }

}


