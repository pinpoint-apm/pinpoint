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
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Component("transactionCodecV2")
public class TransactionCodecV2 extends AgentStatCodecV2<TransactionBo> {

    @Autowired
    public TransactionCodecV2(AgentStatDataPointCodec codec) {
        super(new TransactionFactory(codec));
    }


    private static class TransactionFactory implements CodecFactory<TransactionBo> {

        private final AgentStatDataPointCodec codec;

        private TransactionFactory(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public AgentStatDataPointCodec getCodec() {
            return codec;
        }

        @Override
        public CodecEncoder<TransactionBo> createCodecEncoder() {
            return new TransactionCodecEncoder(codec);
        }

        @Override
        public CodecDecoder<TransactionBo> createCodecDecoder() {
            return new TransactionCodecDecoder(codec);
        }
    }

    public static class TransactionCodecEncoder implements AgentStatCodec.CodecEncoder<TransactionBo> {

        private final AgentStatDataPointCodec codec;
        private final UnsignedLongEncodingStrategy.Analyzer.Builder collectIntervalAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder sampledNewCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder sampledContinuationCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder unsampledNewCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder unsampledContinuationCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder skippedNewCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder skippedContinuationCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();

        public TransactionCodecEncoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void addValue(TransactionBo transactionBo) {
            collectIntervalAnalyzerBuilder.addValue(transactionBo.getCollectInterval());
            sampledNewCountAnalyzerBuilder.addValue(transactionBo.getSampledNewCount());
            sampledContinuationCountAnalyzerBuilder.addValue(transactionBo.getSampledContinuationCount());
            unsampledNewCountAnalyzerBuilder.addValue(transactionBo.getUnsampledNewCount());
            unsampledContinuationCountAnalyzerBuilder.addValue(transactionBo.getUnsampledContinuationCount());
            skippedNewCountAnalyzerBuilder.addValue(transactionBo.getSkippedNewSkipCount());
            skippedContinuationCountAnalyzerBuilder.addValue(transactionBo.getSkippedContinuationCount());
        }

        @Override
        public void encode(Buffer valueBuffer) {
            StrategyAnalyzer<Long> collectIntervalStrategyAnalyzer = collectIntervalAnalyzerBuilder.build();
            StrategyAnalyzer<Long> sampledNewCountStrategyAnalyzer = sampledNewCountAnalyzerBuilder.build();
            StrategyAnalyzer<Long> sampledContinuationCountStrategyAnalyzer = sampledContinuationCountAnalyzerBuilder.build();
            StrategyAnalyzer<Long> unsampledNewCountStrategyAnalyzer = unsampledNewCountAnalyzerBuilder.build();
            StrategyAnalyzer<Long> unsampledContinuationCountStrategyAnalyzer = unsampledContinuationCountAnalyzerBuilder.build();
            StrategyAnalyzer<Long> skippedNewCountStrategyAnalyzer = skippedNewCountAnalyzerBuilder.build();
            StrategyAnalyzer<Long> skippedContinuationCountStrategyAnalyzer = skippedContinuationCountAnalyzerBuilder.build();

            // encode header
            AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
            headerEncoder.addCode(collectIntervalStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(sampledNewCountStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(sampledContinuationCountStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(unsampledNewCountStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(unsampledContinuationCountStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(skippedNewCountStrategyAnalyzer.getBestStrategy().getCode());
            headerEncoder.addCode(skippedContinuationCountStrategyAnalyzer.getBestStrategy().getCode());

            final byte[] header = headerEncoder.getHeader();
            valueBuffer.putPrefixedBytes(header);
            // encode values
            this.codec.encodeValues(valueBuffer, collectIntervalStrategyAnalyzer.getBestStrategy(), collectIntervalStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, sampledNewCountStrategyAnalyzer.getBestStrategy(), sampledNewCountStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, sampledContinuationCountStrategyAnalyzer.getBestStrategy(), sampledContinuationCountStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, unsampledNewCountStrategyAnalyzer.getBestStrategy(), unsampledNewCountStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, unsampledContinuationCountStrategyAnalyzer.getBestStrategy(), unsampledContinuationCountStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, skippedNewCountStrategyAnalyzer.getBestStrategy(), skippedNewCountStrategyAnalyzer.getValues());
            this.codec.encodeValues(valueBuffer, skippedContinuationCountStrategyAnalyzer.getBestStrategy(), skippedContinuationCountStrategyAnalyzer.getValues());
        }

    }

    public static class TransactionCodecDecoder implements AgentStatCodec.CodecDecoder<TransactionBo> {

        private final AgentStatDataPointCodec codec;
        private List<Long> collectIntervals;
        private List<Long> sampledNewCounts;
        private List<Long> sampledContinuationCounts;
        private List<Long> unsampledNewCounts;
        private List<Long> unsampledContinuationCounts;
        private List<Long> skippedNewCounts;
        private List<Long> skippedContinuationCounts;

        public TransactionCodecDecoder(AgentStatDataPointCodec codec) {
            this.codec = Objects.requireNonNull(codec, "codec");
        }

        @Override
        public void decode(Buffer valueBuffer, AgentStatHeaderDecoder headerDecoder, int valueSize) {
            EncodingStrategy<Long> collectIntervalEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> sampledNewCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> sampledContinuationCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> unsampledNewCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> unsampledContinuationCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());

            EncodingStrategy<Long> skippedNewCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
            EncodingStrategy<Long> skippedContinuationCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());

            // decode values
            this.collectIntervals = this.codec.decodeValues(valueBuffer, collectIntervalEncodingStrategy, valueSize);
            this.sampledNewCounts = this.codec.decodeValues(valueBuffer, sampledNewCountEncodingStrategy, valueSize);
            this.sampledContinuationCounts = this.codec.decodeValues(valueBuffer, sampledContinuationCountEncodingStrategy, valueSize);
            this.unsampledNewCounts = this.codec.decodeValues(valueBuffer, unsampledNewCountEncodingStrategy, valueSize);
            this.unsampledContinuationCounts = this.codec.decodeValues(valueBuffer, unsampledContinuationCountEncodingStrategy, valueSize);
            if (valueBuffer.remaining() > 0) {
                // Since 1.8.5-SNAPSHOT
                this.skippedNewCounts = this.codec.decodeValues(valueBuffer, skippedNewCountEncodingStrategy, valueSize);
                this.skippedContinuationCounts = this.codec.decodeValues(valueBuffer, skippedContinuationCountEncodingStrategy, valueSize);
            }
        }

        @Override
        public TransactionBo getValue(int index) {
            TransactionBo transactionBo = new TransactionBo();
            transactionBo.setCollectInterval(collectIntervals.get(index));
            transactionBo.setSampledNewCount(sampledNewCounts.get(index));
            transactionBo.setSampledContinuationCount(sampledContinuationCounts.get(index));
            transactionBo.setUnsampledNewCount(unsampledNewCounts.get(index));
            transactionBo.setUnsampledContinuationCount(unsampledContinuationCounts.get(index));
            if (skippedNewCounts != null) {
                transactionBo.setSkippedNewSkipCount(skippedNewCounts.get(index));
            }
            if (skippedContinuationCounts != null) {
                transactionBo.setSkippedContinuationCount(skippedContinuationCounts.get(index));
            }
            return transactionBo;
        }

    }
}
