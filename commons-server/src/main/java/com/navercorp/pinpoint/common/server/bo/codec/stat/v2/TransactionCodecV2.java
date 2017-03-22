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
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
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
@Component("transactionCodecV2")
public class TransactionCodecV2 implements AgentStatCodec<TransactionBo> {

    private static final byte VERSION = 2;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public TransactionCodecV2(AgentStatDataPointCodec codec) {
        Assert.notNull(codec, "agentStatDataPointCodec must not be null");
        this.codec = codec;
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

        List<Long> startTimestamps = new ArrayList<Long>(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder collectIntervalAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder sampledNewCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder sampledContinuationCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder unsampledNewCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder unsampledContinuationCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        for (TransactionBo transactionBo : transactionBos) {
            startTimestamps.add(transactionBo.getStartTimestamp());
            timestamps.add(transactionBo.getTimestamp());
            collectIntervalAnalyzerBuilder.addValue(transactionBo.getCollectInterval());
            sampledNewCountAnalyzerBuilder.addValue(transactionBo.getSampledNewCount());
            sampledContinuationCountAnalyzerBuilder.addValue(transactionBo.getSampledContinuationCount());
            unsampledNewCountAnalyzerBuilder.addValue(transactionBo.getUnsampledNewCount());
            unsampledContinuationCountAnalyzerBuilder.addValue(transactionBo.getUnsampledContinuationCount());
        }
        this.codec.encodeValues(valueBuffer, UnsignedLongEncodingStrategy.REPEAT_COUNT, startTimestamps);
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
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(collectIntervalStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(sampledNewCountStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(sampledContinuationCountStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(unsampledNewCountStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(unsampledContinuationCountStrategyAnalyzer.getBestStrategy().getCode());
        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);
        // encode values
        this.codec.encodeValues(valueBuffer, collectIntervalStrategyAnalyzer.getBestStrategy(), collectIntervalStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, sampledNewCountStrategyAnalyzer.getBestStrategy(), sampledNewCountStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, sampledContinuationCountStrategyAnalyzer.getBestStrategy(), sampledContinuationCountStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, unsampledNewCountStrategyAnalyzer.getBestStrategy(), unsampledNewCountStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, unsampledContinuationCountStrategyAnalyzer.getBestStrategy(), unsampledContinuationCountStrategyAnalyzer.getValues());
    }

    @Override
    public List<TransactionBo> decodeValues(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
        final String agentId = decodingContext.getAgentId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        int numValues = valueBuffer.readVInt();
        List<Long> startTimestamps = this.codec.decodeValues(valueBuffer, UnsignedLongEncodingStrategy.REPEAT_COUNT, numValues);
        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);
        EncodingStrategy<Long> collectIntervalEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> sampledNewCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> sampledContinuationCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> unsampledNewCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> unsampledContinuationCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        // decode values
        List<Long> collectIntervals = this.codec.decodeValues(valueBuffer, collectIntervalEncodingStrategy, numValues);
        List<Long> sampledNewCounts = this.codec.decodeValues(valueBuffer, sampledNewCountEncodingStrategy, numValues);
        List<Long> sampledContinuationCounts = this.codec.decodeValues(valueBuffer, sampledContinuationCountEncodingStrategy, numValues);
        List<Long> unsampledNewCounts = this.codec.decodeValues(valueBuffer, unsampledNewCountEncodingStrategy, numValues);
        List<Long> unsampledContinuationCounts = this.codec.decodeValues(valueBuffer, unsampledContinuationCountEncodingStrategy, numValues);

        List<TransactionBo> transactionBos = new ArrayList<TransactionBo>(numValues);
        for (int i = 0; i < numValues; ++i) {
            TransactionBo transactionBo = new TransactionBo();
            transactionBo.setAgentId(agentId);
            transactionBo.setStartTimestamp(startTimestamps.get(i));
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
