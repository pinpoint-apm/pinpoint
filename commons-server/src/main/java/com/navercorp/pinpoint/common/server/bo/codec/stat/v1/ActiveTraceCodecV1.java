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
import com.navercorp.pinpoint.common.server.bo.codec.stat.v1.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v1.strategy.UnsignedIntegerEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v1.strategy.UnsignedShortEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
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
@Component
public class ActiveTraceCodecV1 implements AgentStatCodec<ActiveTraceBo> {

    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    private final HeaderCodecV1<Short> shortHeaderCodec;

    private final HeaderCodecV1<Integer> integerHeaderCodec;

    @Autowired
    public ActiveTraceCodecV1(AgentStatDataPointCodec codec, HeaderCodecV1<Short> shortHeaderCodec, HeaderCodecV1<Integer> integerHeaderCodec) {
        Assert.notNull(codec, "agentStatDataPointCodec must not be null");
        Assert.notNull(shortHeaderCodec, "shortHeaderCodec must not be null");
        Assert.notNull(integerHeaderCodec, "integerHeaderCodec must not be null");
        this.codec = codec;
        this.shortHeaderCodec = shortHeaderCodec;
        this.integerHeaderCodec = integerHeaderCodec;
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

        List<Long> timestamps = new ArrayList<>(numValues);
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
        int header = 0;
        int position = 0;
        header = this.shortHeaderCodec.encodeHeader(header, position, versionStrategyAnalyzer.getBestStrategy());
        position += this.shortHeaderCodec.getHeaderBitSize();
        header = this.integerHeaderCodec.encodeHeader(header, position, schemaTypeStrategyAnalyzer.getBestStrategy());
        position += this.integerHeaderCodec.getHeaderBitSize();
        header = this.integerHeaderCodec.encodeHeader(header, position, fastTraceCountsStrategyAnalyzer.getBestStrategy());
        position += this.integerHeaderCodec.getHeaderBitSize();
        header = this.integerHeaderCodec.encodeHeader(header, position, normalTraceCountsStrategyAnalyzer.getBestStrategy());
        position += this.integerHeaderCodec.getHeaderBitSize();
        header = this.integerHeaderCodec.encodeHeader(header, position, slowTraceCountsStrategyAnalyzer.getBestStrategy());
        position += this.integerHeaderCodec.getHeaderBitSize();
        header = this.integerHeaderCodec.encodeHeader(header, position, verySlowTraceCountsStrategyAnalyzer.getBestStrategy());
        valueBuffer.putVInt(header);
        // encode values
        this.codec.encodeValues(valueBuffer, versionStrategyAnalyzer.getBestStrategy(), versionStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, schemaTypeStrategyAnalyzer.getBestStrategy(), schemaTypeStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, fastTraceCountsStrategyAnalyzer.getBestStrategy(), fastTraceCountsStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, normalTraceCountsStrategyAnalyzer.getBestStrategy(), normalTraceCountsStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, slowTraceCountsStrategyAnalyzer.getBestStrategy(), slowTraceCountsStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, verySlowTraceCountsStrategyAnalyzer.getBestStrategy(), verySlowTraceCountsStrategyAnalyzer.getValues());
    }

    @Override
    public List<ActiveTraceBo> decodeValues(Buffer valueBuffer, long initialTimestamp) {
        int numValues = valueBuffer.readVInt();

        List<Long> timestamps = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        // decode headers
        int header = valueBuffer.readVInt();
        int position = 0;
        EncodingStrategy<Short> versionEncodingStrategy = this.shortHeaderCodec.decodeHeader(header, position);
        position += this.shortHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Integer> schemaTypeEncodingStrategy = this.integerHeaderCodec.decodeHeader(header, position);
        position += this.integerHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Integer> fastTraceCountsEncodingStrategy = this.integerHeaderCodec.decodeHeader(header, position);
        position += this.integerHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Integer> normalTraceCountsEncodingStrategy = this.integerHeaderCodec.decodeHeader(header, position);
        position += this.integerHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Integer> slowTraceCountsEncodingStrategy = this.integerHeaderCodec.decodeHeader(header, position);
        position += this.integerHeaderCodec.getHeaderBitSize();
        EncodingStrategy<Integer> verySlowTraceCountsEncodingStrategy = this.integerHeaderCodec.decodeHeader(header, position);
        // decode values
        List<Short> versions = this.codec.decodeValues(valueBuffer, versionEncodingStrategy, numValues);
        List<Integer> schemaTypes = this.codec.decodeValues(valueBuffer, schemaTypeEncodingStrategy, numValues);
        List<Integer> fastTraceCounts = this.codec.decodeValues(valueBuffer, fastTraceCountsEncodingStrategy, numValues);
        List<Integer> normalTraceCounts = this.codec.decodeValues(valueBuffer, normalTraceCountsEncodingStrategy, numValues);
        List<Integer> slowTraceCounts = this.codec.decodeValues(valueBuffer, slowTraceCountsEncodingStrategy, numValues);
        List<Integer> verySlowTraceCounts = this.codec.decodeValues(valueBuffer, verySlowTraceCountsEncodingStrategy, numValues);

        List<ActiveTraceBo> activeTraceBos = new ArrayList<>(numValues);
        for (int i = 0; i < numValues; ++i) {
            ActiveTraceBo activeTraceBo = new ActiveTraceBo();
            activeTraceBo.setTimestamp(timestamps.get(i));
            activeTraceBo.setVersion(versions.get(i));
            activeTraceBo.setHistogramSchemaType(schemaTypes.get(i));
            Map<SlotType, Integer> activeTraceCounts = new HashMap<>();
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
