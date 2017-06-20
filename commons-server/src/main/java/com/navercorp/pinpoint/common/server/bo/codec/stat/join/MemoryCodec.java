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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Component("joinMemoryCodec")
public class MemoryCodec implements ApplicationStatCodec {
    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public MemoryCodec(AgentStatDataPointCodec codec) {
        Assert.notNull(codec, "agentStatDataPointCodec must not be null");
        this.codec = codec;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinMemoryBoList) {
        if (CollectionUtils.isEmpty(joinMemoryBoList)) {
            throw new IllegalArgumentException("MemoryBoList must not be empty");
        }

        final int numValues = joinMemoryBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder heapUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minHeapUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minHeapAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxHeapUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxHeapAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder nonHeapUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minNonHeapUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minNonHeapAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxNonHeapUsedAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxNonHeapAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        for (JoinStatBo joinStatBo : joinMemoryBoList) {
            JoinMemoryBo joinMemoryBo = (JoinMemoryBo) joinStatBo;
            timestamps.add(joinMemoryBo.getTimestamp());
            heapUsedAnalyzerBuilder.addValue(joinMemoryBo.getHeapUsed());
            minHeapUsedAnalyzerBuilder.addValue(joinMemoryBo.getMinHeapUsed());
            minHeapAgentIdAnalyzerBuilder.addValue(joinMemoryBo.getMinHeapAgentId());
            maxHeapUsedAnalyzerBuilder.addValue(joinMemoryBo.getMaxHeapUsed());
            maxHeapAgentIdAnalyzerBuilder.addValue(joinMemoryBo.getMaxHeapAgentId());
            nonHeapUsedAnalyzerBuilder.addValue(joinMemoryBo.getNonHeapUsed());
            minNonHeapUsedAnalyzerBuilder.addValue(joinMemoryBo.getMinNonHeapUsed());
            minNonHeapAgentIdAnalyzerBuilder.addValue(joinMemoryBo.getMinNonHeapAgentId());
            maxNonHeapUsedAnalyzerBuilder.addValue(joinMemoryBo.getMaxNonHeapUsed());
            maxNonHeapAgentIdAnalyzerBuilder.addValue(joinMemoryBo.getMaxNonHeapAgentId());
        }

        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer, heapUsedAnalyzerBuilder.build(), minHeapUsedAnalyzerBuilder.build(), minHeapAgentIdAnalyzerBuilder.build(), maxHeapUsedAnalyzerBuilder.build(), maxHeapAgentIdAnalyzerBuilder.build(), nonHeapUsedAnalyzerBuilder.build(), minNonHeapUsedAnalyzerBuilder.build(), minNonHeapAgentIdAnalyzerBuilder.build(), maxNonHeapUsedAnalyzerBuilder.build(), maxNonHeapAgentIdAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer,
                                  StrategyAnalyzer<Long> heapUsedStrategyAnalyzer,
                                  StrategyAnalyzer<Long> minHeapUsedStrategyAnalyzer,
                                  StrategyAnalyzer<String> minHeapAgentIdStrategyAnalyzer,
                                  StrategyAnalyzer<Long> maxHeapUsedStrategyAnalyzer,
                                  StrategyAnalyzer<String> maxHeapAgentIdStrategyAnalyzer,
                                  StrategyAnalyzer<Long> nonHeapUsedStrategyAnalyzer,
                                  StrategyAnalyzer<Long> minNonHeapUsedStrategyAnalyzer,
                                  StrategyAnalyzer<String> minNonHeapAgentIdStrategyAnalyzer,
                                  StrategyAnalyzer<Long> maxNonHeapUsedStrategyAnalyzer,
                                  StrategyAnalyzer<String> maxNonHeapAgentIdStrategyAnalyzer) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(heapUsedStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minHeapUsedStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minHeapAgentIdStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxHeapUsedStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxHeapAgentIdStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(nonHeapUsedStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minNonHeapUsedStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minNonHeapAgentIdStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxNonHeapUsedStrategyAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxNonHeapAgentIdStrategyAnalyzer.getBestStrategy().getCode());
        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);
        // encode values
        this.codec.encodeValues(valueBuffer, heapUsedStrategyAnalyzer.getBestStrategy(), heapUsedStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minHeapUsedStrategyAnalyzer.getBestStrategy(), minHeapUsedStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minHeapAgentIdStrategyAnalyzer.getBestStrategy(), minHeapAgentIdStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxHeapUsedStrategyAnalyzer.getBestStrategy(), maxHeapUsedStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxHeapAgentIdStrategyAnalyzer.getBestStrategy(), maxHeapAgentIdStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, nonHeapUsedStrategyAnalyzer.getBestStrategy(), nonHeapUsedStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minNonHeapUsedStrategyAnalyzer.getBestStrategy(), minNonHeapUsedStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minNonHeapAgentIdStrategyAnalyzer.getBestStrategy(), minNonHeapAgentIdStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxNonHeapUsedStrategyAnalyzer.getBestStrategy(), maxNonHeapUsedStrategyAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxNonHeapAgentIdStrategyAnalyzer.getBestStrategy(), maxNonHeapAgentIdStrategyAnalyzer.getValues());
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
        EncodingStrategy<Long> heapUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> minHeapUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minHeapAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> maxHeapUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxHeapAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> nonHeapUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> minNonHeapUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minNonHeapAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> maxNonHeapUsedEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxNonHeapAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        // decode values
        List<Long> heapUsedList = this.codec.decodeValues(valueBuffer, heapUsedEncodingStrategy, numValues);
        List<Long> minHeapUsedList = this.codec.decodeValues(valueBuffer, minHeapUsedEncodingStrategy, numValues);
        List<String> minHeapAgentIdLIst = this.codec.decodeValues(valueBuffer, minHeapAgentIdEncodingStrategy, numValues);
        List<Long> maxHeapUsedList = this.codec.decodeValues(valueBuffer, maxHeapUsedEncodingStrategy, numValues);
        List<String> maxHeapAgentIdList = this.codec.decodeValues(valueBuffer, maxHeapAgentIdEncodingStrategy, numValues);
        List<Long> nonHeapUsedList = this.codec.decodeValues(valueBuffer, nonHeapUsedEncodingStrategy, numValues);
        List<Long> minNonHeapUsedList = this.codec.decodeValues(valueBuffer, minNonHeapUsedEncodingStrategy, numValues);
        List<String> minNonHeapAgentIdList = this.codec.decodeValues(valueBuffer, minNonHeapAgentIdEncodingStrategy, numValues);
        List<Long> maxNonHeapUsedList = this.codec.decodeValues(valueBuffer, maxNonHeapUsedEncodingStrategy, numValues);
        List<String> maxNonHeapAGentidList = this.codec.decodeValues(valueBuffer, maxNonHeapAgentIdEncodingStrategy, numValues);

        List<JoinStatBo> joinCpuLoadBoList = new ArrayList<JoinStatBo>(numValues);
        for (int i = 0; i < numValues; ++i) {
            JoinMemoryBo joinMemoryBo = new JoinMemoryBo();
            joinMemoryBo.setId(id);
            joinMemoryBo.setTimestamp(timestamps.get(i));
            joinMemoryBo.setHeapUsed(heapUsedList.get(i));
            joinMemoryBo.setMinHeapUsed(minHeapUsedList.get(i));
            joinMemoryBo.setMinHeapAgentId(minHeapAgentIdLIst.get(i));
            joinMemoryBo.setMaxHeapUsed(maxHeapUsedList.get(i));
            joinMemoryBo.setMaxHeapAgentId(maxHeapAgentIdList.get(i));
            joinMemoryBo.setNonHeapUsed(nonHeapUsedList.get(i));
            joinMemoryBo.setMinNonHeapUsed(minNonHeapUsedList.get(i));
            joinMemoryBo.setMinNonHeapAgentId(minNonHeapAgentIdList.get(i));
            joinMemoryBo.setMaxNonHeapUsed(maxNonHeapUsedList.get(i));
            joinMemoryBo.setMaxNonHeapAgentId(maxNonHeapAGentidList.get(i));
            joinCpuLoadBoList.add(joinMemoryBo);
        }

        return joinCpuLoadBoList;
    }
}
