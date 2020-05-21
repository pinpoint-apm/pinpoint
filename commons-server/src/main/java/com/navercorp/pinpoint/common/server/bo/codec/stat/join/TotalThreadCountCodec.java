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
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedIntegerEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.stat.v2.TotalThreadCountCodecV2;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTotalThreadCountBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("joinTotalThreadCountCodec")
public class TotalThreadCountCodec implements ApplicationStatCodec {
    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public TotalThreadCountCodec(AgentStatDataPointCodec codec) { this.codec = codec; }

    @Override
    public byte getVersion() { return VERSION; }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinTotalThreadCountBoList) {
        if (CollectionUtils.isEmpty(joinTotalThreadCountBoList)) {
            throw new IllegalArgumentException("joinTotalThreadCountBoList must not be empty");
        }
        final int numValues = joinTotalThreadCountBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder avgTotalThreadCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minTotalThreadCountAnalyzerBuilder =  new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minTotalThreadCountIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxTotalThreadCountAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxTotalThreadCountIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        for (JoinStatBo joinStatBo : joinTotalThreadCountBoList) {
            JoinTotalThreadCountBo joinTotalThreadCountBo = (JoinTotalThreadCountBo) joinStatBo;
            timestamps.add(joinTotalThreadCountBo.getTimestamp());
            avgTotalThreadCountAnalyzerBuilder.addValue(joinTotalThreadCountBo.getAvgTotalThreadCount());
            minTotalThreadCountAnalyzerBuilder.addValue(joinTotalThreadCountBo.getMinTotalThreadCount());
            minTotalThreadCountIdAnalyzerBuilder.addValue(joinTotalThreadCountBo.getMinTotalThreadCountAgentId());
            maxTotalThreadCountAnalyzerBuilder.addValue(joinTotalThreadCountBo.getMaxTotalThreadCount());
            maxTotalThreadCountIdAnalyzerBuilder.addValue(joinTotalThreadCountBo.getMaxTotalThreadCountAgentId());

        }
        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer, avgTotalThreadCountAnalyzerBuilder.build(), minTotalThreadCountAnalyzerBuilder.build(),
                minTotalThreadCountIdAnalyzerBuilder.build(), maxTotalThreadCountAnalyzerBuilder.build(), maxTotalThreadCountIdAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer,
                                  StrategyAnalyzer<Long> avgTotalThreadCountAnalyzer,
                                  StrategyAnalyzer<Long> minTotalThreadCountAnalyzer,
                                  StrategyAnalyzer<String> minTotalThreadCountIdAnalyzer,
                                  StrategyAnalyzer<Long> maxTotalThreadCountAnalyzer,
                                  StrategyAnalyzer<String> maxTotalThreadCountIdAnalyzer) {
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(avgTotalThreadCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minTotalThreadCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minTotalThreadCountIdAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxTotalThreadCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxTotalThreadCountIdAnalyzer.getBestStrategy().getCode());

        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);

        this.codec.encodeValues(valueBuffer, avgTotalThreadCountAnalyzer.getBestStrategy(), avgTotalThreadCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minTotalThreadCountAnalyzer.getBestStrategy(), minTotalThreadCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minTotalThreadCountIdAnalyzer.getBestStrategy(), minTotalThreadCountIdAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxTotalThreadCountAnalyzer.getBestStrategy(), maxTotalThreadCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxTotalThreadCountIdAnalyzer.getBestStrategy(), maxTotalThreadCountIdAnalyzer.getValues());
    }

    @Override
    public List<JoinStatBo> decodeValues(Buffer valueBuffer, ApplicationStatDecodingContext decodingContext) {
        final String id = decodingContext.getApplicationId();
        final long baseTimestamp = decodingContext.getBaseTimestamp();
        final long timestampDelta = decodingContext.getTimestampDelta();
        final long initialTimestamp = baseTimestamp + timestampDelta;

        int numValues = valueBuffer.readVInt();
        List<Long> timestampList = this.codec.decodeTimestamps(initialTimestamp, valueBuffer, numValues);

        final byte[] header = valueBuffer.readPrefixedBytes();
        AgentStatHeaderDecoder headerDecoder = new BitCountingHeaderDecoder(header);
        EncodingStrategy<Long> avgTotalThreadCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> minTotalThreadCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minTotalThreadCountIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> maxTotalThreadCountEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxTotalThreadCountIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        List<Long> avgTotalThreadCounts = this.codec.decodeValues(valueBuffer, avgTotalThreadCountEncodingStrategy, numValues);
        List<Long> minTotalThreadCounts = this.codec.decodeValues(valueBuffer, minTotalThreadCountEncodingStrategy, numValues);
        List<String> minTotalThreadCountsAgentIds = this.codec.decodeValues(valueBuffer, minTotalThreadCountIdEncodingStrategy, numValues);
        List<Long> maxTotalThreadCounts = this.codec.decodeValues(valueBuffer, maxTotalThreadCountEncodingStrategy, numValues);
        List<String> maxTotalThreadCountsAgentIds = this.codec.decodeValues(valueBuffer, maxTotalThreadCountIdEncodingStrategy, numValues);

        List<JoinStatBo> joinTotalThreadCountBoList = new ArrayList<JoinStatBo>();
        for (int i = 0 ; i < numValues ; i++) {
            JoinTotalThreadCountBo joinTotalThreadCountBo = new JoinTotalThreadCountBo();
            joinTotalThreadCountBo.setId(id);
            joinTotalThreadCountBo.setTimestamp(timestampList.get(i));
            joinTotalThreadCountBo.setAvgTotalThreadCount(avgTotalThreadCounts.get(i));
            joinTotalThreadCountBo.setMinTotalThreadCount(minTotalThreadCounts.get(i));
            joinTotalThreadCountBo.setMinTotalThreadCountAgentId(minTotalThreadCountsAgentIds.get(i));
            joinTotalThreadCountBo.setMaxTotalThreadCount(maxTotalThreadCounts.get(i));
            joinTotalThreadCountBo.setMaxTotalThreadCountAgentId(maxTotalThreadCountsAgentIds.get(i));
            joinTotalThreadCountBoList.add(joinTotalThreadCountBo);
        }

        return joinTotalThreadCountBoList;
    }
}
