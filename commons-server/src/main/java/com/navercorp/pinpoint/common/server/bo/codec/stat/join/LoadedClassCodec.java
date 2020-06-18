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
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("joinLoadedClassCodec")
public class LoadedClassCodec implements ApplicationStatCodec {
    private static final byte VERSION = 1;

    private final AgentStatDataPointCodec codec;

    public LoadedClassCodec(AgentStatDataPointCodec codec) { this.codec = codec; }

    @Override
    public byte getVersion() { return VERSION; }

    @Override
    public void encodeValues(Buffer valueBuffer, List<JoinStatBo> joinStatBoList) {
        if (CollectionUtils.isEmpty(joinStatBoList)) {
            throw new IllegalArgumentException("directBufferBoList must not be empty");
        }

        final int numValues = joinStatBoList.size();
        valueBuffer.putVInt(numValues);
        List<Long> timestamps = new ArrayList<Long>(numValues);
        UnsignedLongEncodingStrategy.Analyzer.Builder avgLoadedClassAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minLoadedClassAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minLoadedClassAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxLoadedClassAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxLoadedClassAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        UnsignedLongEncodingStrategy.Analyzer.Builder avgUnloadedClassAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder minUnloadedClassAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder minUnloadedClassAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        UnsignedLongEncodingStrategy.Analyzer.Builder maxUnloadedClassAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        StringEncodingStrategy.Analyzer.Builder maxUnloadedClassAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        for (JoinStatBo joinStatBo : joinStatBoList) {
            JoinLoadedClassBo joinLoadedClassBo = (JoinLoadedClassBo) joinStatBo;
            timestamps.add(joinLoadedClassBo.getTimestamp());
            avgLoadedClassAnalyzerBuilder.addValue(joinLoadedClassBo.getAvgLoadedClass());
            minLoadedClassAnalyzerBuilder.addValue(joinLoadedClassBo.getMinLoadedClass());
            minLoadedClassAgentIdAnalyzerBuilder.addValue(joinLoadedClassBo.getMinLoadedClassAgentId());
            maxLoadedClassAnalyzerBuilder.addValue(joinLoadedClassBo.getMaxLoadedClass());
            maxLoadedClassAgentIdAnalyzerBuilder.addValue(joinLoadedClassBo.getMaxLoadedClassAgentId());

            avgUnloadedClassAnalyzerBuilder.addValue(joinLoadedClassBo.getAvgUnloadedClass());
            minUnloadedClassAnalyzerBuilder.addValue(joinLoadedClassBo.getMinUnloadedClass());
            minUnloadedClassAgentIdAnalyzerBuilder.addValue(joinLoadedClassBo.getMinUnloadedClassAgentId());
            maxUnloadedClassAnalyzerBuilder.addValue(joinLoadedClassBo.getMaxUnloadedClass());
            maxUnloadedClassAgentIdAnalyzerBuilder.addValue(joinLoadedClassBo.getMaxUnloadedClassAgentId());
        }

        codec.encodeTimestamps(valueBuffer, timestamps);

        encodeDataPoints(valueBuffer
                , avgLoadedClassAnalyzerBuilder.build()
                , minLoadedClassAnalyzerBuilder.build()
                , minLoadedClassAgentIdAnalyzerBuilder.build()
                , maxLoadedClassAnalyzerBuilder.build()
                , maxLoadedClassAgentIdAnalyzerBuilder.build()

                , avgUnloadedClassAnalyzerBuilder.build()
                , minUnloadedClassAnalyzerBuilder.build()
                , minUnloadedClassAgentIdAnalyzerBuilder.build()
                , maxUnloadedClassAnalyzerBuilder.build()
                , maxUnloadedClassAgentIdAnalyzerBuilder.build()
        );
    }

    private void encodeDataPoints(Buffer valueBuffer,
                                  StrategyAnalyzer<Long> avgLoadedClassAnalyzerBuilder,
                                  StrategyAnalyzer<Long> minLoadedClassAnalyzerBuilder,
                                  StrategyAnalyzer<String> minLoadedClassAgentIdAnalyzerBuilder,
                                  StrategyAnalyzer<Long> maxLoadedClassAnalyzerBuilder,
                                  StrategyAnalyzer<String> maxLoadedClassAgentIdAnalyzerBuilder,
                                  StrategyAnalyzer<Long> avgUnloadedClassAnalyzerBuilder,
                                  StrategyAnalyzer<Long> minUnloadedClassAnalyzerBuilder,
                                  StrategyAnalyzer<String> minUnloadedClassAgentIdAnalyzerBuilder,
                                  StrategyAnalyzer<Long> maxUnloadedClassAnalyzerBuilder,
                                  StrategyAnalyzer<String> maxUnloadedClassAgentIdAnalyzerBuilder) {
        // encode header
        AgentStatHeaderEncoder headerEncoder = new BitCountingHeaderEncoder();
        headerEncoder.addCode(avgLoadedClassAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(minLoadedClassAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(minLoadedClassAgentIdAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(maxLoadedClassAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(maxLoadedClassAgentIdAnalyzerBuilder.getBestStrategy().getCode());

        headerEncoder.addCode(avgUnloadedClassAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(minUnloadedClassAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(minUnloadedClassAgentIdAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(maxUnloadedClassAnalyzerBuilder.getBestStrategy().getCode());
        headerEncoder.addCode(maxUnloadedClassAgentIdAnalyzerBuilder.getBestStrategy().getCode());

        final byte[] header = headerEncoder.getHeader();
        valueBuffer.putPrefixedBytes(header);
        // encode values
        this.codec.encodeValues(valueBuffer, avgLoadedClassAnalyzerBuilder.getBestStrategy(), avgLoadedClassAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, minLoadedClassAnalyzerBuilder.getBestStrategy(), minLoadedClassAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, minLoadedClassAgentIdAnalyzerBuilder.getBestStrategy(), minLoadedClassAgentIdAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, maxLoadedClassAnalyzerBuilder.getBestStrategy(), maxLoadedClassAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, maxLoadedClassAgentIdAnalyzerBuilder.getBestStrategy(), maxLoadedClassAgentIdAnalyzerBuilder.getValues());

        this.codec.encodeValues(valueBuffer, avgUnloadedClassAnalyzerBuilder.getBestStrategy(), avgUnloadedClassAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, minUnloadedClassAnalyzerBuilder.getBestStrategy(), minUnloadedClassAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, minUnloadedClassAgentIdAnalyzerBuilder.getBestStrategy(), minUnloadedClassAgentIdAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, maxUnloadedClassAnalyzerBuilder.getBestStrategy(), maxUnloadedClassAnalyzerBuilder.getValues());
        this.codec.encodeValues(valueBuffer, maxUnloadedClassAgentIdAnalyzerBuilder.getBestStrategy(), maxUnloadedClassAgentIdAnalyzerBuilder.getValues());
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
        EncodingStrategy<Long> avgLoadedClassEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long>   minLoadedClassEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minLoadedClassAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long>   maxLoadedClassEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxLoadedClassAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        EncodingStrategy<Long> avgUnloadedClassEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long>   minUnloadedClassEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> minUnloadedClassAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<Long>   maxUnloadedClassEncodingStrategy = UnsignedLongEncodingStrategy.getFromCode(headerDecoder.getCode());
        EncodingStrategy<String> maxUnloadedClassAgentIdEncodingStrategy = StringEncodingStrategy.getFromCode(headerDecoder.getCode());

        // decode values
        List<Long>   avgLoadedClass = this.codec.decodeValues(valueBuffer, avgLoadedClassEncodingStrategy, numValues);
        List<Long>   minLoadedClass = this.codec.decodeValues(valueBuffer, minLoadedClassEncodingStrategy, numValues);
        List<String> minLoadedClassAgentIds = this.codec.decodeValues(valueBuffer, minLoadedClassAgentIdEncodingStrategy, numValues);
        List<Long>   maxLoadedClass = this.codec.decodeValues(valueBuffer, maxLoadedClassEncodingStrategy, numValues);
        List<String> maxLoadedClassAgentIds = this.codec.decodeValues(valueBuffer, maxLoadedClassAgentIdEncodingStrategy, numValues);

        List<Long>   avgUnloadedClass = this.codec.decodeValues(valueBuffer, avgUnloadedClassEncodingStrategy, numValues);
        List<Long>   minUnloadedClass = this.codec.decodeValues(valueBuffer, minUnloadedClassEncodingStrategy, numValues);
        List<String> minUnloadedClassAgentIds = this.codec.decodeValues(valueBuffer, minUnloadedClassAgentIdEncodingStrategy, numValues);
        List<Long>   maxUnloadedClass = this.codec.decodeValues(valueBuffer, maxUnloadedClassEncodingStrategy, numValues);
        List<String> maxUnloadedClassAgentIds = this.codec.decodeValues(valueBuffer, maxUnloadedClassAgentIdEncodingStrategy, numValues);

        List<JoinStatBo> joinLoadedClassBoList = new ArrayList<JoinStatBo>(numValues);
        for (int i = 0; i < numValues; i++) {
            JoinLoadedClassBo joinLoadedClassBo = new JoinLoadedClassBo();
            joinLoadedClassBo.setId(id);
            joinLoadedClassBo.setTimestamp(timestamps.get(i));

            joinLoadedClassBo.setAvgLoadedClass(avgLoadedClass.get(i));
            joinLoadedClassBo.setMinLoadedClass(minLoadedClass.get(i));
            joinLoadedClassBo.setMinLoadedClassAgentId(minLoadedClassAgentIds.get(i));
            joinLoadedClassBo.setMaxLoadedClass(maxLoadedClass.get(i));
            joinLoadedClassBo.setMaxLoadedClassAgentId(maxLoadedClassAgentIds.get(i));

            joinLoadedClassBo.setAvgUnloadedClass(avgUnloadedClass.get(i));
            joinLoadedClassBo.setMinUnloadedClass(minUnloadedClass.get(i));
            joinLoadedClassBo.setMinUnloadedClassAgentId(minUnloadedClassAgentIds.get(i));
            joinLoadedClassBo.setMaxUnloadedClass(maxUnloadedClass.get(i));
            joinLoadedClassBo.setMaxUnloadedClassAgentId(maxUnloadedClassAgentIds.get(i));

            joinLoadedClassBoList.add(joinLoadedClassBo);
        }
        return joinLoadedClassBoList;
    }
}
