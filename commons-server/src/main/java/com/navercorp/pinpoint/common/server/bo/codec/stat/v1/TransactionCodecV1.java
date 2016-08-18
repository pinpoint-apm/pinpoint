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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v1;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v1.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v1.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Component("transactionCodecV1")
public class TransactionCodecV1 implements AgentStatCodec<TransactionBo> {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    private final HeaderCodecV1<Long> longHeaderCodec;

    @Autowired
    public TransactionCodecV1(AgentStatDataPointCodec codec, HeaderCodecV1<Long> longHeaderCodec) {
        Assert.notNull(codec, "agentStatDataPointCodec must not be null");
        Assert.notNull(longHeaderCodec, "longHeaderCodec must not be null");
        this.codec = codec;
        this.longHeaderCodec = longHeaderCodec;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<TransactionBo> transactionBos) {
        if (CollectionUtils.isEmpty(transactionBos)) {
            throw new IllegalArgumentException("transactionBos must not be empty");
        }
        final int numValues = transactionBos.size();
        valueBuffer.putVInt(numValues);

        List<Long> timestamps = new ArrayList<>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder collectIntervalAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder sampledNewCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder sampledContinuationCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder unsampledNewCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder unsampledContinuationCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        for (TransactionBo transactionBo : transactionBos) {
            timestamps.add(transactionBo.getTimestamp());
            collectIntervalAnalyzerBuilder.addValue(transactionBo.getCollectInterval());
            sampledNewCountAnalyzerBuilder.addValue(transactionBo.getSampledNewCount());
            sampledContinuationCountAnalyzerBuilder.addValue(transactionBo.getSampledContinuationCount());
            unsampledNewCountAnalyzerBuilder.addValue(transactionBo.getUnsampledNewCount());
            unsampledContinuationCountAnalyzerBuilder.addValue(transactionBo.getUnsampledContinuationCount());
        }
        this.codec.encodeTimestamps(valueBuffer, timestamps);
        this.encodeDataPoints(
                valueBuffer,
                collectIntervalAnalyzerBuilder.build(),
                sampledNewCountAnalyzerBuilder.build(),
                sampledContinuationCountAnalyzerBuilder.build(),
                unsampledNewCountAnalyzerBuilder.build(),
                unsampledContinuationCountAnalyzerBuilder.build());
    }

    private void encodeDataPoints(
            Buffer valueBuffer,
            StrategyAnalyzer<Long> collectIntervalStrategyAnalyzer,
            StrategyAnalyzer<Long> sampledNewCountStrategyAnalyzer,
            StrategyAnalyzer<Long> sampledContinuationCountStrategyAnalyzer,
            StrategyAnalyzer<Long> unsampledNewCountStrategyAnalyzer,
            StrategyAnalyzer<Long> unsampledContinuationCountStrategyAnalyzer) {
        // encode header
        int header = 0;
        int position = 0;
        header = this.longHeaderCodec.encodeHeader(header, position, collectIntervalStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, sampledNewCountStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, sampledContinuationCountStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, unsampledNewCountStrategyAnalyzer.getBestStrategy());
        position += this.longHeaderCodec.getHeaderBitSize();
        header = this.longHeaderCodec.encodeHeader(header, position, unsampledContinuationCountStrategyAnalyzer.getBestStrategy());
        valueBuffer.putVInt(header);
        // encode values
        this.codec.encodeValues(valueBuffer, collectIntervalStrategyAnalyzer.getBestStrategy(), collectIntervalStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, sampledNewCountStrategyAnalyzer.getBestStrategy(), sampledNewCountStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, sampledContinuationCountStrategyAnalyzer.getBestStrategy(), sampledContinuationCountStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, unsampledNewCountStrategyAnalyzer.getBestStrategy(), unsampledNewCountStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, unsampledContinuationCountStrategyAnalyzer.getBestStrategy(), unsampledContinuationCountStrategyAnalyzer.getValues());
    }

    @Override
    public List<TransactionBo> decodeValues(Buffer valueBuffer, long initialTimestamp) {
        int numValues = valueBuffer.readVInt();

        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        int header = valueBuffer.readVInt();
        int position = 0;
        EncodingStrategy<Long> collectIntervalEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> sampledNewCountEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> sampledContinuationCountEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> unsampledNewCountEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        position += this.longHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Long> unsampledContinuationCountEncodingStrategy = this.longHeaderCodec.decodeHeader(header, position);
        // decode values
        List<Long> collectIntervals = this.codec.decodeValues(valueBuffer, collectIntervalEncodingStrategy, numValues);
        List<Long> sampledNewCounts = this.codec.decodeValues(valueBuffer, sampledNewCountEncodingStrategy, numValues);
        List<Long> sampledContinuationCounts = this.codec.decodeValues(valueBuffer, sampledContinuationCountEncodingStrategy, numValues);
        List<Long> unsampledNewCounts = this.codec.decodeValues(valueBuffer, unsampledNewCountEncodingStrategy, numValues);
        List<Long> unsampledContinuationCounts = this.codec.decodeValues(valueBuffer, unsampledContinuationCountEncodingStrategy, numValues);

        List<TransactionBo> transactionBos = new ArrayList<>(numValues);
        for (int i = 0; i < numValues; ++i) {
            TransactionBo transactionBo = new TransactionBo();
            transactionBo.setTimestamp(timestamps.get(i));
            transactionBo.setCollectInterval(collectIntervals.get(i));
            transactionBo.setSampledNewCount(sampledNewCounts.get(i));
            transactionBo.setSampledContinuationCount(sampledContinuationCounts.get(i));
            transactionBo.setUnsampledNewCount(unsampledNewCounts.get(i));
            transactionBo.setUnsampledContinuationCount(unsampledContinuationCounts.get(i));
            transactionBos.add(transactionBo);
        }
        return transactionBos;
    }
}
