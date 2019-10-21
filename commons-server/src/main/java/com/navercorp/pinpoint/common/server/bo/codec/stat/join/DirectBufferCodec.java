/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.common.server.bo.codec.stat.join;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StringEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Roy Kim
 */
@Component("joinDirectBufferCodec")
public class DirectBufferCodec implements ApplicationStatCodec {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public DirectBufferCodec(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "agentStatDataPointCodec");
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinDirectBufferBoList) {
        if (CollectionUtils.isEmpty(joinDirectBufferBoList)) {
            throw new IllegalArgumentException("directBufferBoList must not be empty");
        }

        final int numValues = joinDirectBufferBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder avgDirectCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minDirectCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minDirectCountAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxDirectCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxDirectCountAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        UnsignedLongEncodingStrategy.Analyzer.Builder avgDirectMemoryUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minDirectMemoryUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minDirectMemoryUsedAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxDirectMemoryUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxDirectMemoryUsedAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        UnsignedLongEncodingStrategy.Analyzer.Builder avgMappedCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minMappedCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minMappedCountAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxMappedCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxMappedCountAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        UnsignedLongEncodingStrategy.Analyzer.Builder avgMappedMemoryUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minMappedMemoryUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minMappedMemoryUsedAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxMappedMemoryUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxMappedMemoryUsedAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        for (JoinStatBo joinStatBo : joinDirectBufferBoList) {
            JoinDirectBufferBo joinDirectBufferBo = (JoinDirectBufferBo) joinStatBo;
            timestamps.add(joinDirectBufferBo.getTimestamp());
            avgDirectCountAnalyzerBuilder.addValue(joinDirectBufferBo.getAvgDirectCount());
            minDirectCountAnalyzerBuilder.addValue(joinDirectBufferBo.getMinDirectCount());
            minDirectCountAgentIdAnalyzerBuilder.addValue(joinDirectBufferBo.getMinDirectCountAgentId());
            maxDirectCountAnalyzerBuilder.addValue(joinDirectBufferBo.getMaxDirectCount());
            maxDirectCountAgentIdAnalyzerBuilder.addValue(joinDirectBufferBo.getMaxDirectCountAgentId());

            avgDirectMemoryUsedAnalyzerBuilder.addValue(joinDirectBufferBo.getAvgMappedMemoryUsed());
            minDirectMemoryUsedAnalyzerBuilder.addValue(joinDirectBufferBo.getMinDirectMemoryUsed());
            minDirectMemoryUsedAgentIdAnalyzerBuilder.addValue(joinDirectBufferBo.getMinDirectMemoryUsedAgentId());
            maxDirectMemoryUsedAnalyzerBuilder.addValue(joinDirectBufferBo.getMaxDirectMemoryUsed());
            maxDirectMemoryUsedAgentIdAnalyzerBuilder.addValue(joinDirectBufferBo.getMaxDirectMemoryUsedAgentId());

            avgMappedCountAnalyzerBuilder.addValue(joinDirectBufferBo.getAvgMappedCount());
            minMappedCountAnalyzerBuilder.addValue(joinDirectBufferBo.getMinMappedCount());
            minMappedCountAgentIdAnalyzerBuilder.addValue(joinDirectBufferBo.getMinMappedCountAgentId());
            maxMappedCountAnalyzerBuilder.addValue(joinDirectBufferBo.getMaxMappedCount());
            maxMappedCountAgentIdAnalyzerBuilder.addValue(joinDirectBufferBo.getMaxMappedCountAgentId());

            avgMappedMemoryUsedAnalyzerBuilder.addValue(joinDirectBufferBo.getAvgMappedMemoryUsed());
            minMappedMemoryUsedAnalyzerBuilder.addValue(joinDirectBufferBo.getMinMappedMemoryUsed());
            minMappedMemoryUsedAgentIdAnalyzerBuilder.addValue(joinDirectBufferBo.getMinMappedMemoryUsedAgentId());
            maxMappedMemoryUsedAnalyzerBuilder.addValue(joinDirectBufferBo.getMaxMappedMemoryUsed());
            maxMappedMemoryUsedAgentIdAnalyzerBuilder.addValue(joinDirectBufferBo.getMaxMappedMemoryUsedAgentId());


        }
        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer
                , avgDirectCountAnalyzerBuilder.build()
                , minDirectCountAnalyzerBuilder.build()
                , minDirectCountAgentIdAnalyzerBuilder.build()
                , maxDirectCountAnalyzerBuilder.build()
                , maxDirectCountAgentIdAnalyzerBuilder.build()

                , avgDirectMemoryUsedAnalyzerBuilder.build()
                , minDirectMemoryUsedAnalyzerBuilder.build()
                , minDirectMemoryUsedAgentIdAnalyzerBuilder.build()
                , maxDirectMemoryUsedAnalyzerBuilder.build()
                , maxDirectMemoryUsedAgentIdAnalyzerBuilder.build()

                , avgMappedCountAnalyzerBuilder.build()
                , minMappedCountAnalyzerBuilder.build()
                , minMappedCountAgentIdAnalyzerBuilder.build()
                , maxMappedCountAnalyzerBuilder.build()
                , maxMappedCountAgentIdAnalyzerBuilder.build()

                , avgMappedMemoryUsedAnalyzerBuilder.build()
                , minMappedMemoryUsedAnalyzerBuilder.build()
                , minMappedMemoryUsedAgentIdAnalyzerBuilder.build()
                , maxMappedMemoryUsedAnalyzerBuilder.build()
                , maxMappedMemoryUsedAgentIdAnalyzerBuilder.build()
        );
    }

    private void encodeDataPoints(Buffer valueBuffer
            , StrategyAnalyzer<Long> avgDirectCountAnalyzer
            , StrategyAnalyzer<Long> minDirectCountAnalyzer
            , StrategyAnalyzer<String> minDirectCountAgentIdAnalyzer
            , StrategyAnalyzer<Long> maxDirectCountAnalyzer
            , StrategyAnalyzer<String> maxDirectCountAgentIdAnalyzer

            , StrategyAnalyzer<Long> avgDirectMemoryUsedAnalyzer
            , StrategyAnalyzer<Long> minDirectMemoryUsedAnalyzer
            , StrategyAnalyzer<String> minDirectMemoryUsedAgentIdAnalyzer
            , StrategyAnalyzer<Long> maxDirectMemoryUsedAnalyzer
            , StrategyAnalyzer<String> maxDirectMemoryUsedAgentIdAnalyzer

            , StrategyAnalyzer<Long> avgMappedCountAnalyzer
            , StrategyAnalyzer<Long> minMappedCountAnalyzer
            , StrategyAnalyzer<String> minMappedCountAgentIdAnalyzer
            , StrategyAnalyzer<Long> maxMappedCountAnalyzer
            , StrategyAnalyzer<String> maxMappedCountAgentIdAnalyzer

            , StrategyAnalyzer<Long> avgMappedMemoryUsedAnalyzer
            , StrategyAnalyzer<Long> minMappedMemoryUsedAnalyzer
            , StrategyAnalyzer<String> minMappedMemoryUsedAgentIdAnalyzer
            , StrategyAnalyzer<Long> maxMappedMemoryUsedAnalyzer
            , StrategyAnalyzer<String> maxMappedMemoryUsedAgentIdAnalyzer
    ) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(avgDirectCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minDirectCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minDirectCountAgentIdAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxDirectCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxDirectCountAgentIdAnalyzer.getBestStrategy().getCode());

        headerEncoder.addCode(avgDirectMemoryUsedAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minDirectMemoryUsedAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minDirectMemoryUsedAgentIdAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxDirectMemoryUsedAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxDirectMemoryUsedAgentIdAnalyzer.getBestStrategy().getCode());

        headerEncoder.addCode(avgMappedCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minMappedCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minMappedCountAgentIdAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxMappedCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxMappedCountAgentIdAnalyzer.getBestStrategy().getCode());

        headerEncoder.addCode(avgMappedMemoryUsedAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minMappedMemoryUsedAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minMappedMemoryUsedAgentIdAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxMappedMemoryUsedAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxMappedMemoryUsedAgentIdAnalyzer.getBestStrategy().getCode());


        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);
        // encode values
        this.codec.encodeValues(valueBuffer, avgDirectCountAnalyzer.getBestStrategy(), avgDirectCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minDirectCountAnalyzer.getBestStrategy(), minDirectCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minDirectCountAgentIdAnalyzer.getBestStrategy(), minDirectCountAgentIdAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxDirectCountAnalyzer.getBestStrategy(), maxDirectCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxDirectCountAgentIdAnalyzer.getBestStrategy(), maxDirectCountAgentIdAnalyzer.getValues());

        this.codec.encodeValues(valueBuffer, avgDirectMemoryUsedAnalyzer.getBestStrategy(), avgDirectMemoryUsedAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minDirectMemoryUsedAnalyzer.getBestStrategy(), minDirectMemoryUsedAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minDirectMemoryUsedAgentIdAnalyzer.getBestStrategy(), minDirectMemoryUsedAgentIdAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxDirectMemoryUsedAnalyzer.getBestStrategy(), maxDirectMemoryUsedAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxDirectMemoryUsedAgentIdAnalyzer.getBestStrategy(), maxDirectMemoryUsedAgentIdAnalyzer.getValues());

        this.codec.encodeValues(valueBuffer, avgMappedCountAnalyzer.getBestStrategy(), avgMappedCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minMappedCountAnalyzer.getBestStrategy(), minMappedCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minMappedCountAgentIdAnalyzer.getBestStrategy(), minMappedCountAgentIdAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxMappedCountAnalyzer.getBestStrategy(), maxMappedCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxMappedCountAgentIdAnalyzer.getBestStrategy(), maxMappedCountAgentIdAnalyzer.getValues());

        this.codec.encodeValues(valueBuffer, avgMappedMemoryUsedAnalyzer.getBestStrategy(), avgMappedMemoryUsedAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minMappedMemoryUsedAnalyzer.getBestStrategy(), minMappedMemoryUsedAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minMappedMemoryUsedAgentIdAnalyzer.getBestStrategy(), minMappedMemoryUsedAgentIdAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxMappedMemoryUsedAnalyzer.getBestStrategy(), maxMappedMemoryUsedAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxMappedMemoryUsedAgentIdAnalyzer.getBestStrategy(), maxMappedMemoryUsedAgentIdAnalyzer.getValues());

    }

    @Override
    public List<JoinStatBo> decodeValues(Buffer valueBuffer, ApplicationStatDecodingContext decodingContext) {
        final String id = decodingContext.getApplicationId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        int numValues = valueBuffer.readVInt();
        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);
        EncodingStrategy<Long>   avgDirectCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long>   minDirectCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minDirectCountAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long>   maxDirectCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxDirectCountAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        EncodingStrategy<Long>   avgDirectMemoryUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long>   minDirectMemoryUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minDirectMemoryUsedAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long>   maxDirectMemoryUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxDirectMemoryUsedAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        EncodingStrategy<Long>   avgMappedCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long>   minMappedCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minMappedCountAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long>   maxMappedCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxMappedCountAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        EncodingStrategy<Long>   avgMappedMemoryUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long>   minMappedMemoryUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minMappedMemoryUsedAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long>   maxMappedMemoryUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxMappedMemoryUsedAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        // decode values
        List<Long>   avgDirectCounts = this.codec.decodeValues(valueBuffer, avgDirectCountEncodingStrategy, numValues);
        List<Long>   minDirectCounts = this.codec.decodeValues(valueBuffer, minDirectCountEncodingStrategy, numValues);
        List<String> minDirectCountAgentIds = this.codec.decodeValues(valueBuffer, minDirectCountAgentIdEncodingStrategy, numValues);
        List<Long>   maxDirectCounts = this.codec.decodeValues(valueBuffer, maxDirectCountEncodingStrategy, numValues);
        List<String> maxDirectCountAgentIds = this.codec.decodeValues(valueBuffer, maxDirectCountAgentIdEncodingStrategy, numValues);

        List<Long>   avgDirectMemoryUseds = this.codec.decodeValues(valueBuffer, avgDirectMemoryUsedEncodingStrategy, numValues);
        List<Long>   minDirectMemoryUseds = this.codec.decodeValues(valueBuffer, minDirectMemoryUsedEncodingStrategy, numValues);
        List<String> minDirectMemoryUsedAgentIds = this.codec.decodeValues(valueBuffer, minDirectMemoryUsedAgentIdEncodingStrategy, numValues);
        List<Long>   maxDirectMemoryUseds = this.codec.decodeValues(valueBuffer, maxDirectMemoryUsedEncodingStrategy, numValues);
        List<String> maxDirectMemoryUsedAgentIds = this.codec.decodeValues(valueBuffer, maxDirectMemoryUsedAgentIdEncodingStrategy, numValues);

        List<Long>   avgMappedCounts = this.codec.decodeValues(valueBuffer, avgMappedCountEncodingStrategy, numValues);
        List<Long>   minMappedCounts = this.codec.decodeValues(valueBuffer, minMappedCountEncodingStrategy, numValues);
        List<String> minMappedCountAgentIds = this.codec.decodeValues(valueBuffer, minMappedCountAgentIdEncodingStrategy, numValues);
        List<Long>   maxMappedCounts = this.codec.decodeValues(valueBuffer, maxMappedCountEncodingStrategy, numValues);
        List<String> maxMappedCountAgentIds = this.codec.decodeValues(valueBuffer, maxMappedCountAgentIdEncodingStrategy, numValues);

        List<Long>   avgMappedMemoryUseds = this.codec.decodeValues(valueBuffer, avgMappedMemoryUsedEncodingStrategy, numValues);
        List<Long>   minMappedMemoryUseds = this.codec.decodeValues(valueBuffer, minMappedMemoryUsedEncodingStrategy, numValues);
        List<String> minMappedMemoryUsedAgentIds = this.codec.decodeValues(valueBuffer, minMappedMemoryUsedAgentIdEncodingStrategy, numValues);
        List<Long>   maxMappedMemoryUseds = this.codec.decodeValues(valueBuffer, maxMappedMemoryUsedEncodingStrategy, numValues);
        List<String> maxMappedMemoryUsedAgentIds = this.codec.decodeValues(valueBuffer, maxMappedMemoryUsedAgentIdEncodingStrategy, numValues);

        List<JoinStatBo> joinDirectBufferBoList = new ArrayList<JoinStatBo>(numValues);
        for (int i = 0; i < numValues; i++) {
            JoinDirectBufferBo joinDirectBufferBo = new JoinDirectBufferBo();
            joinDirectBufferBo.setId(id);
            joinDirectBufferBo.setTimestamp(timestamps.get(i));

            joinDirectBufferBo.setAvgDirectCount(avgDirectCounts.get(i));
            joinDirectBufferBo.setMinDirectCount(minDirectCounts.get(i));
            joinDirectBufferBo.setMinDirectCountAgentId(minDirectCountAgentIds.get(i));
            joinDirectBufferBo.setMaxDirectCount(maxDirectCounts.get(i));
            joinDirectBufferBo.setMaxDirectCountAgentId(maxDirectCountAgentIds.get(i));

            joinDirectBufferBo.setAvgDirectMemoryUsed(avgDirectMemoryUseds.get(i));
            joinDirectBufferBo.setMinDirectMemoryUsed(minDirectMemoryUseds.get(i));
            joinDirectBufferBo.setMinDirectMemoryUsedAgentId(minDirectMemoryUsedAgentIds.get(i));
            joinDirectBufferBo.setMaxDirectMemoryUsed(maxDirectMemoryUseds.get(i));
            joinDirectBufferBo.setMaxDirectMemoryUsedAgentId(maxDirectMemoryUsedAgentIds.get(i));

            joinDirectBufferBo.setAvgMappedCount(avgMappedCounts.get(i));
            joinDirectBufferBo.setMinMappedCount(minMappedCounts.get(i));
            joinDirectBufferBo.setMinMappedCountAgentId(minMappedCountAgentIds.get(i));
            joinDirectBufferBo.setMaxMappedCount(maxMappedCounts.get(i));
            joinDirectBufferBo.setMaxMappedCountAgentId(maxMappedCountAgentIds.get(i));

            joinDirectBufferBo.setAvgMappedMemoryUsed(avgMappedMemoryUseds.get(i));
            joinDirectBufferBo.setMinMappedMemoryUsed(minMappedMemoryUseds.get(i));
            joinDirectBufferBo.setMinMappedMemoryUsedAgentId(minMappedMemoryUsedAgentIds.get(i));
            joinDirectBufferBo.setMaxMappedMemoryUsed(maxMappedMemoryUseds.get(i));
            joinDirectBufferBo.setMaxMappedMemoryUsedAgentId(maxMappedMemoryUsedAgentIds.get(i));

            joinDirectBufferBoList.add(joinDirectBufferBo);
        }
        return joinDirectBufferBoList;
    }
}
