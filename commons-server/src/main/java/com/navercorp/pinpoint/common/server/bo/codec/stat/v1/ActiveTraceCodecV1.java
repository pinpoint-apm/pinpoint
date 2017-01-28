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
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.AgentStatHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderDecoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.header.BitCountingHeaderEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedIntegerEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedShortEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.trace.SlotType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
@Component("activeTraceCodecV1")
public class ActiveTraceCodecV1 implements AgentStatCodec<ActiveTraceBo> {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public ActiveTraceCodecV1(AgentStatDataPointCodec codec) {
        Assert.notNull(codec, "agentStatDataPointCodec must not be null");
        this.codec = codec;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<ActiveTraceBo> activeTraceBos) {
        if (CollectionUtils.isEmpty(activeTraceBos)) {
            throw new IllegalArgumentException("activeTraceBos must not be empty");
        }
        final int numValues = activeTraceBos.size();
        valueBuffer.putVInt(numValues);

        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedShortEncodingStrategy.Analyzer.Builder versionAnalyzerBuilder = new UnsignedShortEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder schemaTypeAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder fastTraceCountsAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder normalTraceCountsAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder slowTraceCountsAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder verySlowTraceCountsAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        for (ActiveTraceBo activeTraceBo : activeTraceBos) {
            timestamps.add(activeTraceBo.getTimestamp());
            versionAnalyzerBuilder.addValue(activeTraceBo.getVersion());
            schemaTypeAnalyzerBuilder.addValue(activeTraceBo.getHistogramSchemaType());
            final Map<SlotType, Integer> activeTraceCounts = activeTraceBo.getActiveTraceCounts();
            fastTraceCountsAnalyzerBuilder.addValue(MapUtils.getIntValue(activeTraceCounts, SlotType.FAST, ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT));
            normalTraceCountsAnalyzerBuilder.addValue(MapUtils.getIntValue(activeTraceCounts, SlotType.NORMAL, ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT));
            slowTraceCountsAnalyzerBuilder.addValue(MapUtils.getIntValue(activeTraceCounts, SlotType.SLOW, ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT));
            verySlowTraceCountsAnalyzerBuilder.addValue(MapUtils.getIntValue(activeTraceCounts, SlotType.VERY_SLOW, ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT));
        }
        this.codec.encodeTimestamps(valueBuffer, timestamps);
        this.encodeDataPoints(
                valueBuffer,
                versionAnalyzerBuilder.build(),
                schemaTypeAnalyzerBuilder.build(),
                fastTraceCountsAnalyzerBuilder.build(),
                normalTraceCountsAnalyzerBuilder.build(),
                slowTraceCountsAnalyzerBuilder.build(),
                verySlowTraceCountsAnalyzerBuilder.build());
    }

    private void encodeDataPoints(
            Buffer valueBuffer,
            StrategyAnalyzer<Short> versionStrategyAnalyzer,
            StrategyAnalyzer<Integer> schemaTypeStrategyAnalyzer,
            StrategyAnalyzer<Integer> fastTraceCountsStrategyAnalyzer,
            StrategyAnalyzer<Integer> normalTraceCountsStrategyAnalyzer,
            StrategyAnalyzer<Integer> slowTraceCountsStrategyAnalyzer,
            StrategyAnalyzer<Integer> verySlowTraceCountsStrategyAnalyzer) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(versionStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(schemaTypeStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(fastTraceCountsStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(normalTraceCountsStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(slowTraceCountsStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(verySlowTraceCountsStrategyAnalyzer.getBestStrategy().getCode());
        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);
        // encode values
        this.codec.encodeValues(valueBuffer, versionStrategyAnalyzer.getBestStrategy(), versionStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, schemaTypeStrategyAnalyzer.getBestStrategy(), schemaTypeStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, fastTraceCountsStrategyAnalyzer.getBestStrategy(), fastTraceCountsStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, normalTraceCountsStrategyAnalyzer.getBestStrategy(), normalTraceCountsStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, slowTraceCountsStrategyAnalyzer.getBestStrategy(), slowTraceCountsStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, verySlowTraceCountsStrategyAnalyzer.getBestStrategy(), verySlowTraceCountsStrategyAnalyzer.getValues());
    }

    @Override
    public List<ActiveTraceBo> decodeValues(Buffer valueBuffer, AgentStatDecodingContext decodingContext) {
        final String agentId = decodingContext.getAgentId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        int numValues = valueBuffer.readVInt();
        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);
        EncodingStrategy<Short> versionEncodingStrategy = UnsignedShortEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> schemaTypeEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> fastTraceCountsEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> normalTraceCountsEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> slowTraceCountsEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> verySlowTraceCountsEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        // decode values
        List<Short> versions = this.codec.decodeValues(valueBuffer, versionEncodingStrategy, numValues);
        List<Integer> schemaTypes = this.codec.decodeValues(valueBuffer, schemaTypeEncodingStrategy, numValues);
        List<Integer> fastTraceCounts = this.codec.decodeValues(valueBuffer, fastTraceCountsEncodingStrategy, numValues);
        List<Integer> normalTraceCounts = this.codec.decodeValues(valueBuffer, normalTraceCountsEncodingStrategy, numValues);
        List<Integer> slowTraceCounts = this.codec.decodeValues(valueBuffer, slowTraceCountsEncodingStrategy, numValues);
        List<Integer> verySlowTraceCounts = this.codec.decodeValues(valueBuffer, verySlowTraceCountsEncodingStrategy, numValues);

        List<ActiveTraceBo> activeTraceBos = new ArrayList<ActiveTraceBo>(numValues);
        for (int i = 0; i < numValues; ++i) {
            ActiveTraceBo activeTraceBo = new ActiveTraceBo();
            activeTraceBo.setAgentId(agentId);
            activeTraceBo.setTimestamp(timestamps.get(i));
            activeTraceBo.setVersion(versions.get(i));
            activeTraceBo.setHistogramSchemaType(schemaTypes.get(i));
            Map<SlotType, Integer> activeTraceCounts = new HashMap<SlotType, Integer>();
            activeTraceCounts.put(SlotType.FAST, fastTraceCounts.get(i));
            activeTraceCounts.put(SlotType.NORMAL, normalTraceCounts.get(i));
            activeTraceCounts.put(SlotType.SLOW, slowTraceCounts.get(i));
            activeTraceCounts.put(SlotType.VERY_SLOW, verySlowTraceCounts.get(i));
            activeTraceBo.setActiveTraceCounts(activeTraceCounts);
            activeTraceBos.add(activeTraceBo);
        }
        return activeTraceBos;
    }
}
