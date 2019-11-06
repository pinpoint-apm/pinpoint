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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Component("joinResponseTimeCodec")
public class ResponseTimeCodec implements ApplicationStatCodec {
    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public ResponseTimeCodec(AgentStatDataPointCodec codec) {
        this.codec = Objects.requireNonNull(codec, "codec");
    }


    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinResponseTimeBoList) {
        if (CollectionUtils.isEmpty(joinResponseTimeBoList)) {
            throw new IllegalArgumentException("joinResponseTimeBoList must not be empty");
        }

        final int numValues = joinResponseTimeBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder avgAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minAvgAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minAvgAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxAvgAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxAvgAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        for (JoinStatBo joinStatBo : joinResponseTimeBoList) {
            JoinResponseTimeBo joinResponseTimeBo = (JoinResponseTimeBo) joinStatBo;
            timestamps.add(joinResponseTimeBo.getTimestamp());
            avgAnalyzerBuilder.addValue(joinResponseTimeBo.getAvg());
            minAvgAnalyzerBuilder.addValue(joinResponseTimeBo.getMinAvg());
            minAvgAgentIdAnalyzerBuilder.addValue(joinResponseTimeBo.getMinAvgAgentId());
            maxAvgAnalyzerBuilder.addValue(joinResponseTimeBo.getMaxAvg());
            maxAvgAgentIdAnalyzerBuilder.addValue(joinResponseTimeBo.getMaxAvgAgentId());
        }

        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer, avgAnalyzerBuilder.build(), minAvgAnalyzerBuilder.build(), minAvgAgentIdAnalyzerBuilder.build(), maxAvgAnalyzerBuilder.build(), maxAvgAgentIdAnalyzerBuilder.build());
    }

    private void encodeDataPoints(Buffer valueBuffer, StrategyAnalyzer<Long> avgAnalyzer, StrategyAnalyzer<Long> minAvgAnalyzer, StrategyAnalyzer<String> minAvgAgentIdAnalyzer, StrategyAnalyzer<Long> maxAvgAnalyzer, StrategyAnalyzer<String> maxAvgAgentIdAnalyzer) {
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(avgAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minAvgAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minAvgAgentIdAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxAvgAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxAvgAgentIdAnalyzer.getBestStrategy().getCode());
        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);

        this.codec.encodeValues(valueBuffer, avgAnalyzer.getBestStrategy(), avgAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minAvgAnalyzer.getBestStrategy(), minAvgAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minAvgAgentIdAnalyzer.getBestStrategy(), minAvgAgentIdAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxAvgAnalyzer.getBestStrategy(), maxAvgAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxAvgAgentIdAnalyzer.getBestStrategy(), maxAvgAgentIdAnalyzer.getValues());
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
        EncodingStrategy<Long> avgEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> minAvgEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minAvgAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long> maxAvgEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxAvgAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        List<Long> avgList = this.codec.decodeValues(valueBuffer, avgEncodingStrategy, numValues);
        List<Long> minAvgList = this.codec.decodeValues(valueBuffer, minAvgEncodingStrategy, numValues);
        List<String> minAvgAgentIdList = this.codec.decodeValues(valueBuffer, minAvgAgentIdEncodingStrategy, numValues);
        List<Long> maxAvgList = this.codec.decodeValues(valueBuffer, maxAvgEncodingStrategy, numValues);
        List<String> maxAvgAgentIdList = this.codec.decodeValues(valueBuffer, maxAvgAgentIdEncodingStrategy, numValues);

        List<JoinStatBo> joinResponseTimeBoList = new ArrayList<JoinStatBo>();
        for (int i = 0 ; i < numValues ; i++) {
            JoinResponseTimeBo joinResponseTimeBo = new JoinResponseTimeBo();
            joinResponseTimeBo.setId(id);
            joinResponseTimeBo.setTimestamp(timestampList.get(i));
            joinResponseTimeBo.setAvg(avgList.get(i));
            joinResponseTimeBo.setMinAvg(minAvgList.get(i));
            joinResponseTimeBo.setMinAvgAgentId(minAvgAgentIdList.get(i));
            joinResponseTimeBo.setMaxAvg(maxAvgList.get(i));
            joinResponseTimeBo.setMaxAvgAgentId(maxAvgAgentIdList.get(i));
            joinResponseTimeBoList.add(joinResponseTimeBo);
        }

        return joinResponseTimeBoList;
    }
}
