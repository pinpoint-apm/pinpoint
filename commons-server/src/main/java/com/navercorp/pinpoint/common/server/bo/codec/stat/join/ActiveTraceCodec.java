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
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.*;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Component("joinActiveTraceCodec")
public class ActiveTraceCodec implements ApplicationStatCodec {
    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    @Autowired
    public ActiveTraceCodec(AgentStatDataPointCodec codec) {
        this.codec = codec;
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinActiveTraceBoList) {
        if (CollectionUtils.isEmpty(joinActiveTraceBoList)) {
            throw new IllegalArgumentException("JoinActiveTraceBoList must not be empty");
        }

        final int numValues = joinActiveTraceBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedShortEncodingStrategy.Analyzer.Builder versionAnalyzerBuilder = new UnsignedShortEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder schemaTypeAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder totalCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder minTotalCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minTotalCountAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedIntegerEncodingStrategy.Analyzer.Builder maxTotalCountAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxTotalCountAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        for (JoinStatBo joinStatBo : joinActiveTraceBoList) {
            JoinActiveTraceBo joinActiveTraceBo = (JoinActiveTraceBo) joinStatBo;
            timestamps.add(joinActiveTraceBo.getTimestamp());
            versionAnalyzerBuilder.addValue(joinActiveTraceBo.getVersion());
            schemaTypeAnalyzerBuilder.addValue(joinActiveTraceBo.getHistogramSchemaType());
            totalCountAnalyzerBuilder.addValue(joinActiveTraceBo.getTotalCount());
            minTotalCountAnalyzerBuilder.addValue(joinActiveTraceBo.getMinTotalCount());
            minTotalCountAgentIdAnalyzerBuilder.addValue(joinActiveTraceBo.getMinTotalCountAgentId());
            maxTotalCountAnalyzerBuilder.addValue(joinActiveTraceBo.getMaxTotalCount());
            maxTotalCountAgentIdAnalyzerBuilder.addValue(joinActiveTraceBo.getMaxTotalCountAgentId());
        }

        codec.encodeTimestamps(valueBuffer, timestamps);
        encodeDataPoints(valueBuffer, versionAnalyzerBuilder.build(), schemaTypeAnalyzerBuilder.build(), totalCountAnalyzerBuilder.build(), minTotalCountAnalyzerBuilder.build(), minTotalCountAgentIdAnalyzerBuilder.build(), maxTotalCountAnalyzerBuilder.build(), maxTotalCountAgentIdAnalyzerBuilder.build());

    }

    private void encodeDataPoints(Buffer valueBuffer, StrategyAnalyzer<Short> versionAnalyzer, StrategyAnalyzer<Integer> schemaTypeAnalyzer, StrategyAnalyzer<Integer> totalCountAnalyzer, StrategyAnalyzer<Integer> minTotalCountAnalyzer, StrategyAnalyzer<String> minTotalCountAgentIdAnalyzer, StrategyAnalyzer<Integer> maxTotalCountAnalyzer, StrategyAnalyzer<String> maxTotalCountAgentIdAnalyzer) {
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(versionAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(schemaTypeAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(totalCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minTotalCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(minTotalCountAgentIdAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxTotalCountAnalyzer.getBestStrategy().getCode());
        headerEncoder.addCode(maxTotalCountAgentIdAnalyzer.getBestStrategy().getCode());
        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);

        this.codec.encodeValues(valueBuffer, versionAnalyzer.getBestStrategy(), versionAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, schemaTypeAnalyzer.getBestStrategy(), schemaTypeAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, totalCountAnalyzer.getBestStrategy(), totalCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minTotalCountAnalyzer.getBestStrategy(), minTotalCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, minTotalCountAgentIdAnalyzer.getBestStrategy(), minTotalCountAgentIdAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxTotalCountAnalyzer.getBestStrategy(), maxTotalCountAnalyzer.getValues());
        this.codec.encodeValues(valueBuffer, maxTotalCountAgentIdAnalyzer.getBestStrategy(), maxTotalCountAgentIdAnalyzer.getValues());
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
        EncodingStrategy<Short> versionEncodingStrategy = UnsignedShortEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> schemaTypeEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> totalCountEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> minTotalCountEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minTotalCountAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Integer> maxTotalCountEncodingStrategy = UnsignedIntegerEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxTotalCountAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        List<Short> versionList = this.codec.decodeValues(valueBuffer, versionEncodingStrategy, numValues);
        List<Integer> schemaTypeList = this.codec.decodeValues(valueBuffer, schemaTypeEncodingStrategy, numValues);
        List<Integer> totalCountList = this.codec.decodeValues(valueBuffer, totalCountEncodingStrategy, numValues);
        List<Integer> minTotalCountList = this.codec.decodeValues(valueBuffer, minTotalCountEncodingStrategy, numValues);
        List<String> minTotalCountAgentIdList = this.codec.decodeValues(valueBuffer, minTotalCountAgentIdEncodingStrategy, numValues);
        List<Integer> maxTotalCountList = this.codec.decodeValues(valueBuffer, maxTotalCountEncodingStrategy, numValues);
        List<String> maxTotalCountAgentIdList = this.codec.decodeValues(valueBuffer, maxTotalCountAgentIdEncodingStrategy, numValues);

        List<JoinStatBo> joinActiveTraceBoList = new ArrayList<JoinStatBo>();
        for (int i = 0 ; i < numValues ; i++) {
            JoinActiveTraceBo joinActiveTraceBo = new JoinActiveTraceBo();
            joinActiveTraceBo.setId(id);
            joinActiveTraceBo.setVersion(versionList.get(i));
            joinActiveTraceBo.setTimestamp(timestampList.get(i));
            joinActiveTraceBo.setHistogramSchemaType(schemaTypeList.get(i));
            joinActiveTraceBo.setTotalCount(totalCountList.get(i));
            joinActiveTraceBo.setMinTotalCount(minTotalCountList.get(i));
            joinActiveTraceBo.setMinTotalCountAgentId(minTotalCountAgentIdList.get(i));
            joinActiveTraceBo.setMaxTotalCount(maxTotalCountList.get(i));
            joinActiveTraceBo.setMaxTotalCountAgentId(maxTotalCountAgentIdList.get(i));
            joinActiveTraceBoList.add(joinActiveTraceBo);
        }

        return joinActiveTraceBoList;
    }
}
